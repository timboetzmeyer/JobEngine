package de.boetzmeyer.jobengine;

final class NodeStateInfo {
	private final boolean skipped;
	private NodeState nodeState;
	
	public static NodeStateInfo createFromState(final NodeState inProcessState) {
		return new NodeStateInfo(inProcessState, false);
	}
	
	public static NodeStateInfo createAsSkipped() {
		return new NodeStateInfo(NodeState.SUCCEEDED, true);
	}
	
	private NodeStateInfo(final NodeState inState, final boolean inSkipped) {
		skipped = inSkipped;
		nodeState = inState;
	}

	@Override
	public String toString() {
		return String.format("%s [skip=%s]", nodeState.toString(), Boolean.toString(skipped));
	}

	public boolean updateState(final NodeState inState) {
		if (inState != null) {
			nodeState = inState;
			return true;
		}
		return false;
	}
	
	public NodeState getState() {
		return nodeState;
	}

	public boolean isSkipped() {
		return skipped;
	}
}
