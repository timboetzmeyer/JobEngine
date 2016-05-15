package de.boetzmeyer.jobengine;

public final class ErrorDefinition {	
	private final int errorID;
	private final String errorDescription;

	public ErrorDefinition(final int inErrorID, final String inErrorDescription) {
		errorID = inErrorID;
		errorDescription = inErrorDescription;
	}

	@Override
	public final int hashCode() {
		return errorDescription.hashCode();
	}

	@Override
	public final boolean equals(final Object inObj) {
		if (inObj instanceof ErrorDefinition) {
			final ErrorDefinition other = (ErrorDefinition) inObj;
			return (other.errorID == this.errorID) && (other.errorDescription.equalsIgnoreCase(this.errorDescription));
		}
		return false;
	}

	@Override
	public final String toString() {
		return String.format("%s - %s", Integer.toString(errorID), errorDescription);
	}
}
