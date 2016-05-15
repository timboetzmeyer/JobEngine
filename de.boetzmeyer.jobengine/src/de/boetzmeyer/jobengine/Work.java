package de.boetzmeyer.jobengine;

public interface Work {
	
	NodeState execute(final NodeContext inNodeContext) throws NodeException;

	String getNodeTitle();

	String getNodeDescription();
}
