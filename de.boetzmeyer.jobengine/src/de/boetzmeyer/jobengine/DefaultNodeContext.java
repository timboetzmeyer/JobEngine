package de.boetzmeyer.jobengine;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import de.boetzmeyer.jobengine.model.WorkService;
import de.boetzmeyer.jobstore.jobstore.ExecutionPlan;
import de.boetzmeyer.jobstore.jobstore.LogRecord;
import de.boetzmeyer.jobstore.jobstore.PlanJob;
import de.boetzmeyer.jobstore.jobstore.PlanJobExecution;

final class DefaultNodeContext implements NodeContext {

	private static final long ONE_SECOND_IN_MILLIS = 1000L;

	private final PlanJob planJob;
	private final PlanJobExecution planJobExecution;
	private final WorkService workService;
	private final ExecutionPlan executionPlan;
	private final NodeLogger nodeLogger;
	private final RuntimeNode runtimeNode;

	public DefaultNodeContext(final PlanJob inPlanJob, final PlanJobExecution inPlanJobExecution, final WorkService inWorkService, final ExecutionPlan inExecutionPlan,
			final ConcurrentMap<PlanJob, NodeStateInfo> inNodeStates, final NodeLogger inNodeLogger) {
		planJob = inPlanJob;
		planJobExecution = inPlanJobExecution;
		workService = inWorkService;
		executionPlan = inExecutionPlan;
		nodeLogger = inNodeLogger;
		runtimeNode = new RuntimeNode(inPlanJob, inPlanJobExecution, inWorkService, inExecutionPlan, inNodeStates);
	}

	@Override
	public String[] getErrorsAndWarnings() {
		return nodeLogger.getErrorsAndWarnings();
	}

	@Override
	public NodeLog getLog() {
		return nodeLogger;
	}

	@Override
	public void cancel() {
		PlanExecutionContext.cancel(planJob.getJobRef().getJobName(), planJobExecution.getPlanExecution());
	}

	@Override
	public boolean isCancelled() {
		return PlanExecutionContext.isCancelled(planJobExecution.getPlanExecution());
	}

	@Override
	public void updateBeginTime(Date inBeginTime) {
		planJobExecution.setFromDate(inBeginTime);
		planJobExecution.setToDate(new Date(inBeginTime.getTime() + ONE_SECOND_IN_MILLIS));
		planJobExecution.save();
	}

	@Override
	public List<LogRecord> getExecutionInfos() {
		return runtimeNode.getExecutionInfos();
	}

	@Override
	public List<LogRecord> getExecutionWarnings() {
		return runtimeNode.getExecutionWarnings();
	}

	@Override
	public List<LogRecord> getExecutionErrors() {
		return runtimeNode.getExecutionErrors();
	}

	@Override
	public boolean hasErrorOccurred(final ErrorDefinition inErrorConstant) {
		return runtimeNode.hasErrorOccurred(inErrorConstant);
	}

	@Override
	public List<PlanJob> getPlanJobs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PlanJobExecution> getJobHistory() {
		return this.workService.getRuntimeHistory(planJob.getPrimaryKey());
	}
}
