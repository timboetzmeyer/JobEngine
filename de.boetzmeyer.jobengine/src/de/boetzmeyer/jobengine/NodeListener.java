package de.boetzmeyer.jobengine;

public interface NodeListener {
	void beforeExecution(final NodeContext inNodeContext);
	void afterExecution(final NodeContext inNodeContext);
}
