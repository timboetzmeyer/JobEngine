package de.boetzmeyer.jobengine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import de.boetzmeyer.jobstore.jobstore.PlanJob;


abstract class LinkLogger {

	private LinkLogger() {
	}

	public static String toString(final boolean inSimulationMode, final ConcurrentMap<PlanJob, NodeStateInfo> inNodeStateInfos) {
		final List<PlanJob> processes = new ArrayList<PlanJob>(inNodeStateInfos.keySet());
		final StringBuilder s = new StringBuilder();
		if (inSimulationMode) {
			s.append("Plan runs in simulation mode for scheduling tests.\r\n");
		} else {
			s.append("Plan is processing real jobs.\r\n");
		}
		for (int i = 0, size = processes.size(); i < size; i++) {
			final PlanJob process = processes.get(i);
			if (process != null) {
				final String row = String.format("%s.\t%s [%s]\r\n", Integer.toString(i + 1), process.getJobRef().getJobName(), inNodeStateInfos.get(process).getState());
				s.append(row);
			}
		}
		return s.toString();
	}
}
