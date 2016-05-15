package de.boetzmeyer.jobengine;

public final class NodeException extends RuntimeException {
	private static final long serialVersionUID = -2367162941519006841L;

	public NodeException(final String inMessage, final Throwable inCause) {
		super(inMessage, inCause);
	}

	public NodeException(final String inMessage) {
		super(inMessage);
	}
}
