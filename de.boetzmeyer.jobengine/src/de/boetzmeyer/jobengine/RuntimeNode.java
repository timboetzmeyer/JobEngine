package de.boetzmeyer.jobengine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import de.boetzmeyer.jobengine.model.WorkService;
import de.boetzmeyer.jobstore.jobstore.ExecutionPlan;
import de.boetzmeyer.jobstore.jobstore.LogRecord;
import de.boetzmeyer.jobstore.jobstore.PlanExecution;
import de.boetzmeyer.jobstore.jobstore.PlanJob;
import de.boetzmeyer.jobstore.jobstore.PlanJobExecution;


final class RuntimeNode {

	private final PlanJob planJob;
	private final PlanJobExecution planJobExecution;
	private final WorkService workService;
	private final ExecutionPlan executionPlan;
	private final ConcurrentMap<PlanJob, NodeStateInfo> nodeStates;

	public RuntimeNode(final PlanJob inPlanJob, final PlanJobExecution inPlanJobExecution, final WorkService inProcessModelService, final ExecutionPlan inRuntimeConfiguration,
			final ConcurrentMap<PlanJob, NodeStateInfo> inProcessStates) {
		planJob = inPlanJob;
		planJobExecution = inPlanJobExecution;
		workService = inProcessModelService;
		executionPlan = inRuntimeConfiguration;
		nodeStates = inProcessStates;
	}

	public List<LogRecord> getExecutionErrors() {
		return workService.getExecutionErrors(planJobExecution.getPlanExecution(), 0);
	}

	public List<LogRecord> getExecutionInfos() {
		return workService.getExecutionInfos(planJobExecution.getPlanExecution());
	}

	public List<LogRecord> getExecutionWarnings() {
		return workService.getExecutionWarnings(planJobExecution.getPlanExecution());
	}

	public List<PlanJobExecution> getRuntimeHistory() {
		final List<PlanJobExecution> analysedExecutions = new ArrayList<PlanJobExecution>();
		final List<PlanJobExecution> allExecutions = workService.getRuntimeHistory(planJob.getPrimaryKey());
		for (PlanJobExecution execution : allExecutions) {
			if (execution != null) {
				final PlanExecution planExecution = execution.getPlanExecutionRef();
				if (planExecution != null) {
					final String stage = planExecution.getDetails();
					if (stage.equalsIgnoreCase(WorkConstants.SIMULATION) == false) {
						analysedExecutions.add(execution);
					}
				}
			}
		}
		return analysedExecutions;
	}

	public boolean hasErrorOccurred(final ErrorDefinition inErrorConstant) {
		return workService.hasErrorOccurred(inErrorConstant.toString(), planJobExecution.getPlanExecution());
	}

	public boolean isCancelled() {
		return PlanExecutionContext.isCancelled(planJobExecution.getPlanExecution());
	}
}
