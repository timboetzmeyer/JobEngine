package de.boetzmeyer.jobengine.distributed;

import de.boetzmeyer.jobengine.source.transfer.DBConfig;

public final class ApplicationRequest {
	private final DBConfig configDB;
	private final long executionPlanId;
	private final long planExecutionId;
	private final long planJobExecutionId;
	private final long planJobId;

	public ApplicationRequest(final DBConfig inConfigDB, final long inPlanId, final long inPlanExecutionId, final long inPlanJobExecutionId, final long inPlanJobId) {
		configDB = inConfigDB;
		executionPlanId = inPlanId;
		planExecutionId = inPlanExecutionId;
		planJobExecutionId = inPlanJobExecutionId;
		planJobId = inPlanJobId;
	}

	public DBConfig getConfigDB() {
		return configDB;
	}

	public long getPlanJobExecutionId() {
		return planJobExecutionId;
	}

	public long getPlanJobId() {
		return planJobId;
	}

	public long getExecutionPlanId() {
		return executionPlanId;
	}

	public long getPlanExecutionId() {
		return planExecutionId;
	}

	@Override
	public int hashCode() {
		return configDB.hashCode();
	}

	@Override
	public boolean equals(final Object inObj) {
		if (inObj instanceof ApplicationRequest) {
			final ApplicationRequest appRequest = (ApplicationRequest) inObj;
			return appRequest.configDB.equals(configDB) && (appRequest.planJobExecutionId == planJobExecutionId) && (appRequest.planJobId == planJobId);
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("%s?c=%s&p=%s", configDB.toString(), Long.toString(planJobExecutionId), Long.toString(planJobId));
	}

}
