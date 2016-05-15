package de.boetzmeyer.jobengine.distributed;

public final class RemoteApplication {
	private final String remoteUrl;
	private final int remotePort;
	
	public RemoteApplication(final String inRemoteUrl, final int inRemotePort) {
		remoteUrl = inRemoteUrl;
		remotePort = inRemotePort;
	}

	public String getRemoteUrl() {
		return remoteUrl;
	}

	public int getRemotePort() {
		return remotePort;
	}

	@Override
	public int hashCode() {
		return remoteUrl.toLowerCase().hashCode();
	}

	@Override
	public boolean equals(final Object inObj) {
		if (inObj instanceof RemoteApplication) {
			final RemoteApplication remoteEngine = (RemoteApplication) inObj;
			return remoteEngine.remoteUrl.equalsIgnoreCase(remoteUrl) && (remoteEngine.remotePort == remotePort);
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("%s:%s", remoteUrl, Integer.toString(remotePort));
	}

}
