package de.boetzmeyer.jobengine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.boetzmeyer.jobengine.distributed.ApplicationPool;
import de.boetzmeyer.jobengine.distributed.ApplicationRequest;
import de.boetzmeyer.jobengine.model.WorkService;
import de.boetzmeyer.jobstore.jobstore.ExecutionPlan;
import de.boetzmeyer.jobstore.jobstore.PlanExecution;
import de.boetzmeyer.jobstore.jobstore.PlanJob;
import de.boetzmeyer.jobstore.jobstore.PlanJobLink;

public final class PlanConfiguration {
	private static final double MILLISECONDS_PER_SECOND = 1000.0;
	private static final Log LOG = LogFactory.getLog(PlanConfiguration.class);

	private final boolean simulation;
	private final NodeScheduler nodeScheduler;
	private final ApplicationPool applicationPool;
	private final WorkService workService;
	private final ConcurrentMap<PlanJob, NodeStateInfo> nodeStateInfos = new ConcurrentHashMap<PlanJob, NodeStateInfo>();
	private final ExecutionPlan executionPlan;

	public PlanConfiguration(final NodeListener inNodeListener, final boolean inSimulation,
			final int inThreadCount, final String inPlanTitle, final String inStoreDir)
			throws IllegalWorkStarterException {
		this(inNodeListener, inSimulation, inThreadCount, inPlanTitle, 
				new WorkService(inStoreDir), null, null);
	}

	public PlanConfiguration(final NodeListener inNodeListener, final boolean inSimulation,
			final int inThreadCount, final String inPlanTitle, final String inDriverClass,
			final String inDatabaseConnectionUrl, final String inUser, final String inPassword, 
			final ApplicationPool inPool, final ApplicationRequest inApplicationRequest) throws IllegalWorkStarterException {
		this(inNodeListener, inSimulation, inThreadCount, inPlanTitle, 
				new WorkService(inDriverClass, inDatabaseConnectionUrl, inUser, inPassword), inPool, inApplicationRequest);
	}

	private PlanConfiguration(final NodeListener inNodeListener, final boolean inSimulation, 
			final int inThreadCount, final String inPlanTitle, final WorkService inWorkService, 
			final ApplicationPool inPool, final ApplicationRequest inApplicationRequest)
			throws IllegalWorkStarterException {
		simulation = inSimulation;
		workService = inWorkService;
		if (inApplicationRequest != null) {
			executionPlan = workService.findPlanByID(inApplicationRequest.getExecutionPlanId());
		} else {
			executionPlan = workService.findPlanByName(inPlanTitle);
		}
		if (executionPlan == null) {
			throw new IllegalWorkStarterException(String.format("Execution plan [%s] unknown in the connected job store", inPlanTitle));
		}
		if (inPool != null) {
			applicationPool = inPool;
		} else {
			applicationPool = new ApplicationPool();
		}
		final PlanExecution planExecution = PlanExecution.generate();
		planExecution.save();
		PlanExecutionContext.init(inWorkService, inApplicationRequest, planExecution);
		nodeScheduler = new NodeScheduler(inNodeListener, simulation, inThreadCount, workService, executionPlan, nodeStateInfos, applicationPool, planExecution.getPrimaryKey());
	}

	public void executePlan(final String inUser) {
		initializeNodeStates();
		nodeScheduler.start(inUser);
	}

	private List<PlanJob> initializeNodeStates() {
		final List<PlanJob> runnableNodes = new ArrayList<PlanJob>();
		final HtmlLink link = new HtmlLink(executionPlan.getPrimaryKey(), executionPlan.getTitle());
		LOG.info(String.format("Start analysing execution plan [%s] ...", link.toString()));
		final long startTime = System.currentTimeMillis();
		final List<PlanJobLink> edges = workService.getJobLinks(executionPlan);
		if (edges.size() > 0) {
			final PlanJob[] runnableJobs = NodeCalculator.getJobs(workService, edges);
			updateRunnableNodes(runnableNodes, runnableJobs);
		}
		final long endTime = System.currentTimeMillis();
		final double duration = (endTime - startTime) / MILLISECONDS_PER_SECOND;
		LOG.info(String.format("Analysing of execution plan [%s] took %s seconds", link.toString(), Double.toString(duration)));
		return runnableNodes;
	}

	private void updateRunnableNodes(final List<PlanJob> inRunnableNodes, final PlanJob[] inRunnableJobs) {
		for (PlanJob planJob : workService.getPlanJobs()) {
			if (NodeCalculator.containsJob(planJob, inRunnableJobs)) {
				nodeStateInfos.put(planJob, NodeStateInfo.createFromState(NodeState.RUNNABLE));
				inRunnableNodes.add(planJob);
			} else {
				nodeStateInfos.put(planJob, NodeStateInfo.createAsSkipped());
			}
		}
	}

	@Override
	public String toString() {
		return LinkLogger.toString(simulation, nodeStateInfos);
	}
}
