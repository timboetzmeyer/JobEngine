package de.boetzmeyer.jobengine;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.boetzmeyer.jobengine.distributed.ApplicationPool;
import de.boetzmeyer.jobengine.model.WorkService;
import de.boetzmeyer.jobstore.jobstore.ExecutionPlan;
import de.boetzmeyer.jobstore.jobstore.PlanJob;
import de.boetzmeyer.jobstore.jobstore.PlanJobLink;


final class ParallelWorker implements NodeSchedule {
    private static final Log LOG = LogFactory.getLog(ParallelWorker.class);

	private final NodeListener nodeListener;
	private final boolean simulation;
	private final Shutdown shutdown;
	private final ExecutorService executorService;
	private final ConcurrentMap<PlanJob, NodeStateInfo> nodeStateInfos;
	private final WorkService workService;
	private final ExecutionPlan executionPlan;
	private final long planExecutionID;
	private final Set<PlanJob> alreadyScheduledNodes = new HashSet<PlanJob>();
	private final ApplicationPool applicationPool;

	public ParallelWorker(final NodeListener inNodeListener, final boolean inSimulation,
			final Shutdown inShutdown, final ExecutorService inExecutorService, 
			final ConcurrentMap<PlanJob, NodeStateInfo> inNodeStateInfos, final WorkService inWorkService, 
			final ExecutionPlan inExecutionPlan, final ApplicationPool inPool, final long inPlanExecutionID) {
		nodeListener = inNodeListener;
		simulation = inSimulation;
		shutdown = inShutdown;
		executorService = inExecutorService;
		nodeStateInfos = inNodeStateInfos;
		workService = inWorkService;
		executionPlan = inExecutionPlan;
		planExecutionID = inPlanExecutionID;
		applicationPool = inPool;
	}

	public void startPlanScheduling() {
		LOG.info("Plan execution was successfully started");
		final Runnable schedulerJob = new Runnable() {
			@Override
			public void run() {
				tryScheduling();
			}			
		};
		executorService.submit(schedulerJob);
	}

	@Override
	public boolean tryScheduling() {
		if ((shutdown.isShutDown() == false) && (PlanExecutionContext.isCancelled(planExecutionID) == false)) {
			final Scheduled scheduleResult = scheduleNodes();
			final boolean scheduled = scheduleResult.hasScheduledJobs(); 
			if (scheduled) {
				LOG.info(scheduleResult.toString());
			}
		    if (isPlanCompletelyExecuted()) {
		    	shutdown.shutdown();
		    }
		    return scheduled;
		}
		return false;
	}

	private boolean isPlanCompletelyExecuted() {
        for (NodeStateInfo nodeStateInfo : nodeStateInfos.values()) {
            final NodeState nodeState = nodeStateInfo.getState();
            final boolean executed = nodeState.equals(NodeState.SUCCEEDED) || nodeState.equals(NodeState.FAILED);
            if (executed == false) {
                return false;
            }
        }
        return true;
    }

	private Scheduled scheduleNodes() {
		final List<PlanJob> scheduledNodes = new ArrayList<PlanJob>();
		final List<PlanJob> runnableNodes = findRunnableNodes(executionPlan);
		for (PlanJob planJob : runnableNodes) {
			final NodeStateInfo nodeStateInfo = nodeStateInfos.get(planJob);
			if (nodeStateInfo.isSkipped()) {
				nodeStateInfo.updateState(NodeState.SUCCEEDED);
			} else {
				if (alreadyScheduledNodes.contains(planJob) == false) {
					scheduledNodes.add(planJob);
					alreadyScheduledNodes.add(planJob);
					final Runnable nodeExecutor = new NodeExecutor(nodeListener, simulation, planJob, planExecutionID, 
							executionPlan, workService, nodeStateInfos, this, applicationPool);
					executorService.submit(nodeExecutor);
				}
			}
		}
		return new Scheduled(scheduledNodes, new Date());
	}

	private List<PlanJob> findRunnableNodes(final ExecutionPlan inExecutionPlan) {
		final List<PlanJob> runnableNodes = new ArrayList<PlanJob>();
		for (Map.Entry<PlanJob, NodeStateInfo> entry : nodeStateInfos.entrySet()) {
			if (entry.getValue().getState() == NodeState.RUNNABLE) {
				if (isNodeRunnable(entry.getKey(), inExecutionPlan)) {
					runnableNodes.add(entry.getKey());
				}
			}
		}
		return runnableNodes;
	}

	private boolean isNodeRunnable(final PlanJob inPlanJob, final ExecutionPlan inExecutionPlan) {
		final List<PlanJobLink> requiredNodes;
		final List<PlanJobLink> planJobLinks = workService.getJobLinks(inExecutionPlan);
		if (planJobLinks.size() == 0) {
			requiredNodes = workService.getJobLinks(inPlanJob);
		} else {
			requiredNodes = workService.getJobLinks(inPlanJob, planJobLinks);
		}
		for (PlanJobLink planJobLink : requiredNodes) {
			final PlanJob planJob = planJobLink.getSourceRef();
			if (planJob != null) {
				final NodeStateInfo nodeStateInfo = nodeStateInfos.get(planJob);
				if (NodeState.SUCCEEDED != nodeStateInfo.getState()) {
					return false;
				}
			}    
		}
		return true;
	}	
}
