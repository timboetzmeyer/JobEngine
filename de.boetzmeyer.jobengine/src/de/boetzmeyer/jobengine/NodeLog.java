package de.boetzmeyer.jobengine;

public interface NodeLog {
	void logError(final String inText);

	void logErrorConstant(final ErrorDefinition inErrorConstant);
	
	void logWarning(final String inText);
	
	void logInfo(final String inText);
}
