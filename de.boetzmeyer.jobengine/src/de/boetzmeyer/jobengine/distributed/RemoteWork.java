package de.boetzmeyer.jobengine.distributed;

import de.boetzmeyer.jobengine.AbstractWork;
import de.boetzmeyer.jobengine.NodeContext;
import de.boetzmeyer.jobengine.NodeException;
import de.boetzmeyer.jobengine.NodeState;
import de.boetzmeyer.jobengine.source.transfer.DBConfig;

public final class RemoteWork extends AbstractWork {
	private final RemoteApplication remoteEngine;
	private final ApplicationRequest remoteRequest;
	
	public RemoteWork(final String inRemoteUrl, final int inRemotePort, final DBConfig inConfigDB, 
			final long inExecutionPlanId, final long inPlanExecutionId, final long inPlanJobExecutionId, final long inPlanJobId) {
		this(new RemoteApplication(inRemoteUrl, inRemotePort), new ApplicationRequest(inConfigDB, inExecutionPlanId, inPlanExecutionId, inPlanJobExecutionId, inPlanJobId));
	}
	
	public RemoteWork(final RemoteApplication inRemoteEngine, final ApplicationRequest inRemoteRequest) {
		remoteEngine = inRemoteEngine;
		remoteRequest = inRemoteRequest;
	}

	public RemoteApplication getRemoteEngine() {
		return remoteEngine;
	}

	public ApplicationRequest getRemoteRequest() {
		return remoteRequest;
	}

	public String getRemoteUrl() {
		return remoteEngine.getRemoteUrl();
	}

	public int getRemotePort() {
		return remoteEngine.getRemotePort();
	}

	public DBConfig getConfigDB() {
		return remoteRequest.getConfigDB();
	}

	public long getConfigurationId() {
		return remoteRequest.getExecutionPlanId();
	}

	public long getPlanExecutionId() {
		return remoteRequest.getPlanExecutionId();
	}

	public long getPlanJobExecutionId() {
		return remoteRequest.getPlanJobExecutionId();
	}

	public long getPlanJobId() {
		return remoteRequest.getPlanJobId();
	}

	@Override
	public int hashCode() {
		return remoteEngine.hashCode();
	}

	@Override
	public boolean equals(final Object inObj) {
		if (inObj instanceof RemoteWork) {
			final RemoteWork remoteCall = (RemoteWork) inObj;
			return remoteCall.remoteEngine.equals(remoteEngine) && remoteCall.remoteRequest.equals(remoteRequest);
		}
		return false;
	}

	@Override
	public NodeState execute(final NodeContext inJobExecutionContext) throws NodeException {
		//TODO make a call to remote engine
		return NodeState.SUCCEEDED;
	}

}
