package de.boetzmeyer.jobengine;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import de.boetzmeyer.jobengine.model.WorkService;
import de.boetzmeyer.jobstore.jobstore.PlanJob;
import de.boetzmeyer.jobstore.jobstore.PlanJobLink;


abstract class NodeCalculator {

	private NodeCalculator() {
	}

	public static boolean containsJob(final PlanJob inPlanJob, final PlanJob[] inPlanJobs) {
		if ((inPlanJob != null) && (inPlanJobs != null)) {
			for (int i = 0; i < inPlanJobs.length; i++) {
				if (inPlanJob.equals(inPlanJobs[i])) {
					return true;
				}
			}
		}
		return false;
	}

	public static PlanJob[] getJobs(final WorkService inWorkService, final List<PlanJobLink> inJobLinks) {
		final Set<PlanJob> executed = new HashSet<PlanJob>();
		final Set<PlanJob> skipped = new HashSet<PlanJob>();
		final List<PlanJob> all = inWorkService.getPlanJobs();
		for (PlanJob planJob : all) {
			if (isPartOfPlan(planJob, inJobLinks)) {
				executed.add(planJob);
			} else {
				skipped.add(planJob);
			}			
		} 
		return getJobs(inWorkService, executed, skipped);
	}

	private static boolean isPartOfPlan(final PlanJob inJob, final List<PlanJobLink> inJobLinks) {
		if ((inJob != null) && (inJobLinks != null)) {
			final long id = inJob.getPrimaryKey();
			for (PlanJobLink jobLink : inJobLinks) {
				if (jobLink != null) {
					if ((jobLink.getSource() == id) || (jobLink.getDestination() == id)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static PlanJob[] getJobs(final WorkService inWorkService, final Set<PlanJob> inExecutedJobs, final Set<PlanJob> inSkippedJobs) {
		final Set<PlanJob> markedJobs = new HashSet<PlanJob>();
		final AtomicInteger counter = new AtomicInteger(0);
		addJobs(inWorkService, markedJobs, inExecutedJobs, inSkippedJobs, counter);
		for (PlanJob planJob : inExecutedJobs) {
			markedJobs.add(planJob);
		}
		return markedJobs.toArray(new PlanJob[markedJobs.size()]);
	}

	private static void addJobs(final WorkService inWorkService, final Set<PlanJob> inMarkedJobs, 
			final Set<PlanJob> inExecutedJobs, final Set<PlanJob> inSkippedJobs, final AtomicInteger inCounter) {
		for (PlanJob planJob : inExecutedJobs) {
			if (inSkippedJobs.contains(planJob) == false) {
				inMarkedJobs.add(planJob);
				final List<PlanJobLink> requiredEdges = inWorkService.getJobLinks(planJob);
				final Set<PlanJob> requiredJobs = new HashSet<PlanJob>();
				for (PlanJobLink inputEdge : requiredEdges) {
					final PlanJob requiredProcess = inputEdge.getSourceRef();
					if (requiredProcess != null) {
						if (inMarkedJobs.contains(requiredProcess) == false) {
							requiredJobs.add(requiredProcess);
						}
					}
				}
				addJobs(inWorkService, inMarkedJobs, requiredJobs, inSkippedJobs, inCounter);
			}
		}
	}

}
