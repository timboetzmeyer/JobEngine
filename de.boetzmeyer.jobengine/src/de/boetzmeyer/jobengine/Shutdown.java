package de.boetzmeyer.jobengine;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.boetzmeyer.jobengine.model.WorkService;
import de.boetzmeyer.jobstore.jobstore.ExecutionPlan;


final class Shutdown {
    private static final Log LOG = LogFactory.getLog(Shutdown.class);
	
	private final boolean simulationMode;
	private final WorkService workService;
	private final ExecutionPlan executionPlan;
	private final long planExecutionID;
	private final ExecutorService executorService;
	private final AtomicBoolean shutdown = new AtomicBoolean(false);
	private final AtomicBoolean shutdownAlreadyStarted = new AtomicBoolean(false);

	public Shutdown(final boolean inSimulation, final WorkService inWorkService, final ExecutionPlan inExecutionPlan, 
			final ExecutorService inExecutorService, final long inPlanExecutionID) {
		simulationMode = inSimulation;
		workService = inWorkService;
		executionPlan = inExecutionPlan;
		planExecutionID = inPlanExecutionID;
		executorService = inExecutorService;
	}

	public void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				shutdown();
			}
		});
	}
	
	public void shutdown() {
		if (shutdownAlreadyStarted.get() == false) {
			shutdownAlreadyStarted.set(true);
			shutdown.set(true);
			shutdownNow();
			PlanExecutionContext.finished(simulationMode, planExecutionID);
			workService.saveModel();
			if (PlanExecutionContext.isCancelled(planExecutionID)) {
				final String logText = String.format("Plan '%s' finished has been cancelled", this.executionPlan.getTitle());
				LOG.warn(logText);
			} else {
				final String logText = String.format("Plan '%s' finished has been finished", this.executionPlan.getTitle());
				LOG.info(logText);
			}
		}
	}

	private void shutdownNow() {
		final List<Runnable> scheduledButNotStartedJobs = executorService.shutdownNow();
		if (scheduledButNotStartedJobs != null) {
			for (Runnable job : scheduledButNotStartedJobs) {
				LOG.warn(String.format("'%s' was scheduled but not started because of shutdown.", job.toString()));
			}
		}
	}

	public boolean isShutDown() {
		return shutdown.get();
	}
}
