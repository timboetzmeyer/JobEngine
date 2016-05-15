package de.boetzmeyer.jobengine;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.boetzmeyer.jobengine.system.Strings;
import de.boetzmeyer.jobstore.jobstore.PlanJob;

final class Scheduled {

	private final List<PlanJob> scheduledJobs;
	private final Date time;

	public Scheduled(final List<PlanJob> inScheduledJobs, final Date inTime) {
		scheduledJobs = new ArrayList<PlanJob>(inScheduledJobs);
		time = new Date(inTime.getTime());
	}

	public List<PlanJob> getScheduledJobs() {
		return new ArrayList<PlanJob>(scheduledJobs);
	}
	
	public Date getScheduleTime() {
		return new Date(time.getTime());
	}

	public boolean hasScheduledJobs() {
		return (scheduledJobs.size() > 0);
	}
	
	@Override
	public String toString() {
		return String.format("%s SCHEDULED %s", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(time), getLinks());
	}
	
	private String getLinks() {
		final StringBuilder s = new StringBuilder();
		s.append("[");
		for (int i = 0, size = scheduledJobs.size(); i < size; i++) {
			s.append(toHtmlLink(scheduledJobs.get(i)));
			if (i < (size - 1)) {
				s.append(", ");
			}
		}
		s.append("]");
		return s.toString();
	}

	private String toHtmlLink(final PlanJob inPlanJob) {
		if (inPlanJob != null) {
			final HtmlLink link = new HtmlLink(inPlanJob.getPrimaryKey(), inPlanJob.toString());
			return link.toString();
		}
		return Strings.EMPTY;
	}
}
