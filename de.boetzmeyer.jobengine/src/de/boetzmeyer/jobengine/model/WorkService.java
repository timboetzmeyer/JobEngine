package de.boetzmeyer.jobengine.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.boetzmeyer.jobengine.NodeException;
import de.boetzmeyer.jobengine.WorkConstants;
import de.boetzmeyer.jobengine.source.file.ModelFiles;
import de.boetzmeyer.jobengine.system.Strings;
import de.boetzmeyer.jobstore.jobstore.ExecutionPlan;
import de.boetzmeyer.jobstore.jobstore.IServer;
import de.boetzmeyer.jobstore.jobstore.LogRecord;
import de.boetzmeyer.jobstore.jobstore.PlanExecution;
import de.boetzmeyer.jobstore.jobstore.PlanJob;
import de.boetzmeyer.jobstore.jobstore.PlanJobExecution;
import de.boetzmeyer.jobstore.jobstore.PlanJobLink;
import de.boetzmeyer.jobstore.jobstore.ServerFactory;
import de.boetzmeyer.jobstore.jobstore.Settings;

public final class WorkService {
	
	private final IServer jobStore;

	public WorkService(final String inJobStoreDir) {
		final String jobStoreFile = String.format("%s%s%s%s", inJobStoreDir, Character.toString(File.separatorChar), ModelFiles.DB_FILE_NAME, ModelFiles.DB_EXTENSION);
		final File file = new File(jobStoreFile);
		if (file.exists() && file.isFile()) {
			Settings.setLocaleDatabaseDir(inJobStoreDir);
			ServerFactory.setFileSource(true);
			jobStore = ServerFactory.create();
		} else {
			throw new NodeException(String.format("The job store database does not exist at file location '%s'", jobStoreFile));
		}
	}
	
	public WorkService(final String inDriverClass, final String inDatabaseConnectionUrl, final String inUser, final String inPassword) {
		Settings.setDatabaseConnectionURL(inDatabaseConnectionUrl);
		Settings.setUserName(inUser);
		Settings.setPassword(inPassword);
		Settings.setDriverClass(inDriverClass);
		ServerFactory.setFileSource(false);
		jobStore = ServerFactory.create();
	}
	
	public List<PlanJob> getPlanJobs() {
		return jobStore.listPlanJob();
	}
	
	public List<PlanJobLink> getJobLinks(final PlanJob inPlanJob) {
		final List<PlanJobLink> requiredJobs = jobStore.referencesPlanJobLinkByDestination(inPlanJob.getPrimaryKey());
		final List<PlanJobLink> planFree = new ArrayList<PlanJobLink>();
		for (PlanJobLink dependency : requiredJobs) {
			if (dependency != null) {
				if (dependency.getExecutionPlan() == 0L) {
					planFree.add(dependency);
				}
			}			
		}
		return planFree;
	}
	
	public List<PlanJobLink> getJobLinks(final ExecutionPlan inExecutionPlan) {
		return jobStore.referencesPlanJobLinkByExecutionPlan(inExecutionPlan.getPrimaryKey());
	}

	public List<PlanJobLink> getJobLinks(final PlanJob inPlanJob, List<PlanJobLink> inPlanJobLinks) {
		final List<PlanJobLink> dependencies = new ArrayList<PlanJobLink>();
		for (PlanJobLink edge : inPlanJobLinks) {
			final long destination = edge.getDestination();
			if (inPlanJob.getPrimaryKey() == destination) {
				dependencies.add(edge);
			}
		}
		return dependencies;
	}

	public ExecutionPlan findPlanByName(final String inPlanTitle) throws NodeException {
		final List<ExecutionPlan> plans = jobStore.listExecutionPlan();
		for (ExecutionPlan plan : plans) {
			if (plan.getTitle().equalsIgnoreCase(inPlanTitle)) {
				return plan;
			}
		}
		throw new NodeException(String.format("Execution plan '%s' can not be found in the job store database", inPlanTitle));
	}

	public ExecutionPlan findPlanByID(final long inPlanID) throws NodeException {
		final ExecutionPlan plan = jobStore.findByIDExecutionPlan(inPlanID);
		if (plan != null) {
			return plan;
		}
		throw new NodeException(String.format("Execution plan with ID '%s' can not be found in the job store database", Long.toString(inPlanID)));
	}
	
	public PlanJob[] getConfiguredParallelProcesses(final ExecutionPlan inExecutionPlan) {
		final List<PlanJobLink> planJobLinks = jobStore.referencesPlanJobLinkByExecutionPlan(inExecutionPlan.getPrimaryKey());
		final Set<PlanJob> planJobs = new HashSet<PlanJob>();
		for (PlanJobLink dependency : planJobLinks) {
			if (dependency != null) {
				final PlanJob source = dependency.getSourceRef();
				if (source != null) {
					planJobs.add(source);
				}
				final PlanJob destination = dependency.getDestinationRef();
				if (destination != null) {
					planJobs.add(destination);
				}
			}
		}		
		return planJobs.toArray(new PlanJob[planJobs.size()]);
	}

	public PlanJob[] calcRunnableJobs(final PlanJob[] inPlanJobs) {
		final Set<PlanJob> schedulableJobs = new HashSet<PlanJob>();
		for (int i = 0; i < inPlanJobs.length; i++) {
			final Set<PlanJob> requiredJobs = getRequiredJobs(inPlanJobs[i]);
			schedulableJobs.addAll(requiredJobs);
		}
		return schedulableJobs.toArray(new PlanJob[schedulableJobs.size()]);
	}

	public Set<PlanJob> getRequiredJobs(final PlanJob inPlanJob) {
		final Set<PlanJob> requiredJobs = new HashSet<PlanJob>();
		addRequiredJobs(requiredJobs, inPlanJob);
		return requiredJobs;
	}

	public void saveModel() {
		//jobStore.save(); TODO save
	}

	public List<PlanJobExecution> getRunJobs(final long inPlanExecutionID) {
		if (inPlanExecutionID != 0) {
			final List<PlanJobExecution> jobRuns = jobStore.referencesPlanJobExecutionByPlanExecution(inPlanExecutionID);
			PlanJobExecution.sortByFromDate(jobRuns, true);
			return jobRuns;
		}
		return new ArrayList<PlanJobExecution>();
	}
	
	private void addRequiredJobs(final Set<PlanJob> inRequiredJobs, final PlanJob inPlanJob) {
		if (inRequiredJobs.contains(inPlanJob) == false) {
			inRequiredJobs.add(inPlanJob);
			final List<PlanJobLink> inputs = jobStore.referencesPlanJobLinkBySource(inPlanJob.getPrimaryKey());
			for (PlanJobLink input : inputs) {
				if (input != null) {
					final PlanJob inputJob = input.getSourceRef();
					if (inputJob != null) {
						addRequiredJobs(inRequiredJobs, inputJob);
					}
				}
			}
		}
	}

	public PlanExecution getPlanExecution(final long inPlanExecutionID) {
		return jobStore.findByIDPlanExecution(inPlanExecutionID);
	}

	public List<LogRecord> getLogRecords(final PlanJobExecution inPlanJobExecution) {
		return jobStore.referencesLogRecordByPlanJobExecution(inPlanJobExecution.getPrimaryKey());
	}

	public List<LogRecord> getExecutionInfos(final long inPlanExecutionID) {
		return getLogRecords(WorkConstants.INFO, inPlanExecutionID, 0);
	}

	public List<LogRecord> getExecutionWarnings(final long inPlanExecutionID) {
		return getLogRecords(WorkConstants.WARNING, inPlanExecutionID, 0);
	}

	public List<LogRecord> getExecutionErrors(final long inPlanExecutionID, final int inConsideredLastPlanExecutions) {
		return getLogRecords(WorkConstants.ERROR, inPlanExecutionID, inConsideredLastPlanExecutions);
	}

	private List<LogRecord> getLogRecords(final String inLogLevel, final long inPlanExecutionID, final int inConsideredLastPlanExecutions) {
		final List<LogRecord> infos = new ArrayList<LogRecord>();
		final List<PlanJobExecution> historyJobs = getRunsOfLastPlanExecution(inPlanExecutionID, inConsideredLastPlanExecutions);
		final List<PlanJobExecution> runJobs = getRunJobs(inPlanExecutionID);
		runJobs.addAll(historyJobs);
		for (PlanJobExecution runJob : runJobs) {
			final List<LogRecord> logInfos = getLogRecords(runJob);
			for (LogRecord logInfo : logInfos) {
				final String infoTitle = logInfo.getLogLevel();
				if (inLogLevel.equalsIgnoreCase(infoTitle)) {
					infos.add(logInfo);
				}
			}
		}
		return infos;
	}

	private List<PlanJobExecution> getRunsOfLastPlanExecution(final long inPlanExecutionID, final int inConsideredLastPlanExecutions) {
		final List<PlanJobExecution> historicalRuns = new ArrayList<PlanJobExecution>();
		final List<PlanExecution> allPlanExecutions = jobStore.listPlanExecution();
		PlanExecution.sortByFromDate(allPlanExecutions, false);
		final List<PlanExecution> latestPlanExecutions = new ArrayList<PlanExecution>();
		for (int i = 0; i < allPlanExecutions.size(); i++) {
			final PlanExecution planExecution = allPlanExecutions.get(i);
			if (planExecution != null) {
				latestPlanExecutions.add(planExecution);
			}
			if (latestPlanExecutions.size() == inConsideredLastPlanExecutions) {
				break;
			}
		}
		for (PlanExecution planExecution : latestPlanExecutions) {
			if (planExecution.getPrimaryKey() != inPlanExecutionID) {
				historicalRuns.addAll(jobStore.referencesPlanJobExecutionByPlanExecution(planExecution.getPrimaryKey()));
			}
		}
		return historicalRuns;
	}

	public boolean hasErrorOccurred(final String inErrorConstant, final long inPlanExecutionID) {
		if (Strings.isNotEmpty(inErrorConstant)) {
			final List<LogRecord> allErrors = getExecutionErrors(inPlanExecutionID, 0);
			for (LogRecord error : allErrors) {
				if (error != null) {
					final String description = error.getDetails();
					if (description.equalsIgnoreCase(inErrorConstant)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public List<PlanJobExecution> getRuntimeHistory(final long inPlanJobID) {
		return this.jobStore.referencesPlanJobExecutionByPlanJob(inPlanJobID);
	}
}
