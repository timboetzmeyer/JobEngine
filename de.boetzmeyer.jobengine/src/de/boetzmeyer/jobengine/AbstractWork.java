package de.boetzmeyer.jobengine;


import java.util.Date;

import de.boetzmeyer.jobengine.system.Strings;


public abstract class AbstractWork implements Work {
	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AbstractWork.class);
	
	private static final long SIMULATION_LOOP_COUNT = 500000L;
	private static final String START_RUNNING = "STARTED_RUNNING";

	private boolean simulationMode = false;
	private String nodeTitle = Strings.EMPTY;
	private String nodeDescription = Strings.EMPTY;
	private NodeListener nodeListener;
	
	@Override
	public abstract NodeState execute(final NodeContext inNodeContext) throws NodeException;
	
	public final NodeState startJob(final NodeContext inNodeContext) throws NodeException {
		NodeState processState = NodeState.SUCCEEDED;
		try {
			if (isSimulatedPlan()) {
				simulateWork();
				return NodeState.SUCCEEDED;
			}
			inNodeContext.getLog().logInfo(START_RUNNING);
			inNodeContext.updateBeginTime(new Date());
			System.gc();
			beforeProcessing(inNodeContext);
			processState = this.execute(inNodeContext);
		} catch (final Exception e) {
			LOG.error(e.getMessage(),  e);
			throw new NodeException(e.getMessage(), e);
		} finally {
			afterProcessing(inNodeContext);
		}
		return processState;
	}
	
	private synchronized void afterProcessing(final NodeContext inNodeContext) {
		if (nodeListener != null) {
			if (simulationMode == false) {
				nodeListener.afterExecution(inNodeContext);
			}
		}
	}

	private synchronized void beforeProcessing(final NodeContext inNodeContext) {
		if (nodeListener != null) {
			if (simulationMode == false) {
				nodeListener.beforeExecution(inNodeContext);
			}
		}
	}
	
	public final synchronized void setNodeListener(final NodeListener inPhaseListener) {
		nodeListener = inPhaseListener;
	}
	
	public final synchronized boolean hasNodeListener() {
		return (nodeListener != null);
	}

	private void simulateWork() {
		makeSimulations();
	}
	
	private void makeSimulations() {
		long stupid = 0;
		for (long i = 0; i < SIMULATION_LOOP_COUNT; i++) {
			stupid += i - stupid;
		}
	}
	
	public final boolean isSimulatedPlan() {
		return simulationMode;
	}

	final void setSimulationMode(final boolean inSimulationMode) {
		this.simulationMode = inSimulationMode;
	}

	final void setJobTitle(final String inTitle) {
		nodeTitle = inTitle;
	}

	final void setJobDescription(final String inDescription) {
		nodeDescription = inDescription;
	}

	@Override
	public final String getNodeTitle() {
		return nodeTitle;
	}

	@Override
	public final String getNodeDescription() {
		return nodeDescription;
	}
	
	@Override
	public final String toString() {
		return getNodeTitle();
	}
	
	protected NodeState getNodeState(boolean inSuccess) {
		return inSuccess ? NodeState.SUCCEEDED : NodeState.FAILED;
	}
}
