package de.boetzmeyer.jobengine;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentMap;

import de.boetzmeyer.jobengine.distributed.ApplicationPool;
import de.boetzmeyer.jobengine.distributed.ApplicationRequest;
import de.boetzmeyer.jobengine.distributed.RemoteApplication;
import de.boetzmeyer.jobengine.distributed.RemoteWork;
import de.boetzmeyer.jobengine.model.WorkService;
import de.boetzmeyer.jobengine.source.transfer.DBConfig;
import de.boetzmeyer.jobengine.system.Files;
import de.boetzmeyer.jobstore.jobstore.ExecutionPlan;
import de.boetzmeyer.jobstore.jobstore.JobStore;
import de.boetzmeyer.jobstore.jobstore.PlanExecution;
import de.boetzmeyer.jobstore.jobstore.PlanJob;
import de.boetzmeyer.jobstore.jobstore.PlanJobExecution;
import de.boetzmeyer.jobstore.jobstore.Settings;

final class NodeExecutor implements Runnable {
	private static final String FREE_DISK_SPACE = "FREE_DISK_SPACE (MB)";
	private static final String FINISHED_WITH_PROCESS_STATE_FORMAT = "FINISHED with NodeState [%s]";
	private static final String SCHEDULED = "SCHEDULED";

	private static final org.apache.commons.logging.Log sLog = org.apache.commons.logging.LogFactory.getLog(NodeExecutor.class);
	
	private final NodeSchedule nodeSchedule;
	private final NodeListener nodeListener;
	private final boolean simulationMode;
	private final PlanJob planJob;
	private final long planExecutionID;
	private final ExecutionPlan executionPlan;
	private final WorkService workService;
	private final ConcurrentMap<PlanJob, NodeStateInfo> nodeStates;
	private final ApplicationPool applicationPool;

	public NodeExecutor(final NodeListener inNodeListener, final boolean inSimulationMode, final PlanJob inPlanJob, final long inPlanExecutionID, final ExecutionPlan inExecutionPlan, 
			final WorkService inWorkService, final ConcurrentMap<PlanJob, NodeStateInfo> inNodeStates, final NodeSchedule inNodeSchedule, final ApplicationPool inPool) {
		nodeListener = inNodeListener;
		simulationMode = inSimulationMode;
		planJob = inPlanJob;
		planExecutionID = inPlanExecutionID;
		executionPlan = inExecutionPlan;
		workService = inWorkService;
		nodeStates = inNodeStates;
		nodeSchedule = inNodeSchedule;
		applicationPool = inPool;
	}

	@Override
	public void run() {
		final JobStore jobStore = JobStore.createEmpty();
		final PlanJobExecution planJobExecution = PlanJobExecution.generate();
		planJobExecution.setPlanExecution(planExecutionID);
		jobStore.addPlanJobExecution(planJobExecution);
		planJobExecution.setPlanJob(planJob.getPrimaryKey());
		final NodeLogger inLog = new NodeLogger(planJobExecution);
		final NodeStateInfo processStateInfo = nodeStates.get(planJob);
		try {
			final AbstractWork job = createJob(planJob, planJobExecution);
			job.setNodeListener(nodeListener);
			job.setSimulationMode(simulationMode);
			processStateInfo.updateState(NodeState.RUNNING);
			planJobExecution.setFromDate(new Date());
			planJobExecution.save();
			inLog.logInfo(SCHEDULED);
			final NodeContext nodeContext = new DefaultNodeContext(planJob, planJobExecution, workService, executionPlan, nodeStates, inLog);
			logFreeDiskSpace();
			
			final NodeState result = job.startJob(nodeContext);
			
			processStateInfo.updateState(result);
			logFreeDiskSpace();
			inLog.logInfo(String.format(FINISHED_WITH_PROCESS_STATE_FORMAT, result.toString()));
		} catch (final Throwable e) {
			processStateInfo.updateState(NodeState.SUCCEEDED);
			inLog.logError(e.getMessage());
			sLog.error("Error:", e);
		} finally {
			planJobExecution.setToDate(new Date());
			planJobExecution.save();
			final PlanExecution planExecution = workService.getPlanExecution(planExecutionID);
			if (planExecution != null) {
				planExecution.setToDate(new Date());
				planExecution.save();
			}
			if (nodeSchedule != null) {
				nodeSchedule.tryScheduling();
			}
		}
	}

	private void logFreeDiskSpace() {
		final Long freeDiskSpaceInMB = new Long((long) Files.sizeToMB(Files.getUsableDiskSpace()));
		final String time = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date());
		final String key = String.format("%s  %s", FREE_DISK_SPACE, time);
		PlanExecutionContext.logStatistics(key, freeDiskSpaceInMB, planExecutionID);
	}

	private AbstractWork createJob(final PlanJob inPlanJob, final PlanJobExecution inPlanJobExecution) 
			throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		final AbstractWork job;
		final RemoteApplication remoteEngine = applicationPool.acquire();
		if (remoteEngine != null) {
			final DBConfig dbConfig = new DBConfig(Settings.getServerName(), Settings.getPort(), Settings.getDriverClass(), Settings.getDriverProtocol(), 
					Settings.getUserName(), Settings.getPassword(), Settings.getDatabase());
			job = new RemoteWork(remoteEngine, new ApplicationRequest(dbConfig, executionPlan.getPrimaryKey(), planExecutionID, inPlanJobExecution.getPrimaryKey(), this.planJob.getPrimaryKey()));
		} else {
			final Class<?> jobClass = Class.forName(inPlanJob.getJobRef().getWorkerClass().trim());
			job = (AbstractWork) jobClass.newInstance();
		}
		job.setJobTitle(inPlanJob.getJobRef().getJobName());
		job.setJobDescription(inPlanJob.getJobRef().getDescription());
		return job;
	}

	public PlanJob getPlanJob() {
		return planJob;
	}

	@Override
	public int hashCode() {
		return planJob.hashCode();
	}

	@Override
	public boolean equals(final Object inObj) {
		if (inObj instanceof NodeExecutor) {
			final NodeExecutor nodeExecutor = (NodeExecutor) inObj;
			return nodeExecutor.planJob.equals(planJob);
		}
		return false;
	}

	@Override
	public String toString() {
		return planJob.toString();
	}
}
