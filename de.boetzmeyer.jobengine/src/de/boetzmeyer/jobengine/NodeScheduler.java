package de.boetzmeyer.jobengine;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.boetzmeyer.jobengine.distributed.ApplicationPool;
import de.boetzmeyer.jobengine.model.WorkService;
import de.boetzmeyer.jobengine.system.SystemUtils;
import de.boetzmeyer.jobstore.jobstore.ExecutionPlan;
import de.boetzmeyer.jobstore.jobstore.PlanExecution;
import de.boetzmeyer.jobstore.jobstore.PlanJob;

final class NodeScheduler {

	private final boolean simulationMode;
	private final Shutdown shutdown;
	private final WorkService workService;
	private final ExecutorService executorService;
	private final ConcurrentMap<PlanJob, NodeStateInfo> nodeStateInfos;
	private final ExecutionPlan executionPlan;
	private final long planExecutionID;
	private final ParallelWorker parallelWorker;

	public NodeScheduler(final NodeListener inNodeListener, final boolean inSimulationMode, final int inThreadCount, final WorkService inWorkService, 
			final ExecutionPlan inExecutionPlan, final ConcurrentMap<PlanJob, NodeStateInfo> inNodeStateInfos, 
			final ApplicationPool inPool, final long inPlanExecutionID) {
		simulationMode = inSimulationMode;
		if ((inThreadCount <= 0) || (inThreadCount > SystemUtils.getProcessorCount())) {
			executorService = Executors.newFixedThreadPool(SystemUtils.getProcessorCount());
		} else {
			executorService = Executors.newFixedThreadPool(inThreadCount);
		}
		workService = inWorkService;
		executionPlan = inExecutionPlan;
		planExecutionID = inPlanExecutionID;
		nodeStateInfos = inNodeStateInfos;
		shutdown = new Shutdown(simulationMode, workService, executionPlan, executorService, planExecutionID);
		parallelWorker = new ParallelWorker(inNodeListener, simulationMode, shutdown, executorService, 
				nodeStateInfos, workService, executionPlan, inPool, planExecutionID);
		PlanExecutionContext.initPlanExecution(simulationMode, executionPlan, inPlanExecutionID);
		shutdown.addShutdownHook();
	}

	public void start(final String inStartedByUser) {
		PlanExecutionContext.updatePlanExecution(inStartedByUser, executionPlan.getTitle(), planExecutionID);
		parallelWorker.startPlanScheduling();
	}
}
