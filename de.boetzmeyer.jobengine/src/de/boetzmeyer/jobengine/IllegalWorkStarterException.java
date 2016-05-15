package de.boetzmeyer.jobengine;

public final class IllegalWorkStarterException extends RuntimeException {
	private static final long serialVersionUID = 7452758791206390485L;

	public IllegalWorkStarterException(final String inMessage) {
		super(inMessage);
	}
}
