package de.boetzmeyer.jobengine.examples;

import de.boetzmeyer.jobengine.NodeContext;
import de.boetzmeyer.jobengine.NodeException;
import de.boetzmeyer.jobengine.NodeState;

public class Job8 extends BaseJob {

	@Override
	public NodeState execute(NodeContext inNodeContext) throws NodeException {
		System.err.println("Job 8 executed");
		sleep();
		return NodeState.SUCCEEDED;
	}

}
