package de.boetzmeyer.jobengine.distributed;

import java.util.HashMap;
import java.util.Map;

public final class ApplicationPool {
	private final Map<RemoteApplication, ApplicationState> remoteApplications = new HashMap<RemoteApplication, ApplicationState>();
	
	public ApplicationPool() {
	}

	public synchronized void addRemoteApplication(final RemoteApplication inEngine) {
		remoteApplications.put(inEngine, ApplicationState.FREE);
	}

	public synchronized void removeEngine(final RemoteApplication inEngine) {
		remoteApplications.remove(inEngine);
	}
	
	public synchronized RemoteApplication acquire() {
		for (RemoteApplication engine : remoteApplications.keySet()) {
			final ApplicationState state = remoteApplications.get(engine);
			if (state.equals(ApplicationState.FREE)) {
				remoteApplications.put(engine, ApplicationState.IN_USE);
				return engine;
			}
		}
		return null;
	}
	
	public synchronized boolean release(final RemoteApplication inRemoteApplication) {
		boolean released = false;
		for (RemoteApplication engine : remoteApplications.keySet()) {
			if (engine.equals(inRemoteApplication)) {
				remoteApplications.put(engine, ApplicationState.FREE);
				released = true;
				break;
			}
		}
		return released;
 	}
}
