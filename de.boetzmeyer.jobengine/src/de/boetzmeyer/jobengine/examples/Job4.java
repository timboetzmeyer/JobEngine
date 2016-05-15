package de.boetzmeyer.jobengine.examples;

import de.boetzmeyer.jobengine.NodeContext;
import de.boetzmeyer.jobengine.NodeException;
import de.boetzmeyer.jobengine.NodeState;

public class Job4 extends BaseJob {

	@Override
	public NodeState execute(NodeContext inNodeContext) throws NodeException {
		System.err.println("Job 4 executed");
		sleep();
		return NodeState.SUCCEEDED;
	}

}
