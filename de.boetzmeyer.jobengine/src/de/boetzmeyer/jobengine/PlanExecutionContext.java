package de.boetzmeyer.jobengine;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.boetzmeyer.jobengine.distributed.ApplicationRequest;
import de.boetzmeyer.jobengine.model.WorkService;
import de.boetzmeyer.jobengine.system.Strings;
import de.boetzmeyer.jobengine.system.SystemUtils;
import de.boetzmeyer.jobstore.jobstore.ExecutionPlan;
import de.boetzmeyer.jobstore.jobstore.PlanExecution;


public final class PlanExecutionContext {
	private static final Log LOG = LogFactory.getLog(PlanExecutionContext.class);

	private static Map<Long, PlanExecutionContext> contexts = new HashMap<Long, PlanExecutionContext>();
	
	private final WorkService workService;
	private final Map<String, Number> statistics = new HashMap<String, Number>();

	static void init(final WorkService inWorkService, final ApplicationRequest inApplicationRequest, final PlanExecution inPlanExecution) {
		if (inWorkService != null) {
			final long planExecutionID;
			if (inApplicationRequest != null) {
				planExecutionID = inApplicationRequest.getPlanExecutionId();
			} else {
				planExecutionID = inPlanExecution.getPrimaryKey();
			}
			final PlanExecutionContext planExecutionContext = new PlanExecutionContext(inWorkService);
			contexts.put(planExecutionID, planExecutionContext);
		}
	}
	
	private PlanExecutionContext(final WorkService inWorkService) {	
		workService = inWorkService;
	}
	
	private static PlanExecution getPlanExecution(final long inPlanExecutionID) {
		final PlanExecutionContext context = contexts.get(inPlanExecutionID);
		if ((context != null) && (context.workService != null)) {
			return context.workService.getPlanExecution(inPlanExecutionID);
		} else {
			LOG.error("PlanExecutionContext.init() method was not called or called with 'null' parameter!");
		}
		return null;
	}

	static void cancel(final String inJobName, final long inPlanExecutionID) {
		final String cancelled = String.format("%s at %s by [%s]", WorkConstants.CANCELLED, DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date()), inJobName);
		updateStage(false, getPlanExecution(inPlanExecutionID), cancelled, inPlanExecutionID);
	}
	
	static void finished(final boolean inSimulation, final long inPlanExecutionID) {
		updateStage(inSimulation, getPlanExecution(inPlanExecutionID), WorkConstants.REAL_PROCESSING_FINISHED, inPlanExecutionID);
	}
	
	private static void updateStage(final boolean inSimulationMode, final PlanExecution inPlanExecution, final String inStage, final long inPlanExecutionID) {
		final PlanExecution planExecution;
		if (inPlanExecution != null) {
			planExecution = inPlanExecution;
		} else {
			planExecution = getPlanExecution(inPlanExecutionID);
		}
		if (planExecution != null) {
			if (inSimulationMode) {
				planExecution.setSimulated(inSimulationMode);
			} else {
				if (isCancelled(planExecution) == false) {
					planExecution.setDetails(inStage);
				}
	 		}
			planExecution.setToDate(new Date());
			planExecution.save();
		}
	}
	
	static boolean isCancelled(final long inPlanExecutionID) {
		return isCancelled(getPlanExecution(inPlanExecutionID));
	}

	private static boolean isCancelled(final PlanExecution inPlanExecution) {
		if (inPlanExecution != null) {
			final String stage = inPlanExecution.getDetails();
			if (Strings.isNotEmpty(stage)) {
				return (stage.indexOf(WorkConstants.CANCELLED) >= 0);
			}
		}
		return false;
	}
	
	static void initPlanExecution(final boolean inSimulation, final ExecutionPlan inExecutionPlan, final long inPlanExecutionID) {
		final PlanExecution planExecution = getPlanExecution(inPlanExecutionID);
		if (planExecution != null) {
			planExecution.setFromDate(new Date());
			planExecution.setExecutionPlan(inExecutionPlan.getPrimaryKey());
			updateStage(inSimulation, planExecution, WorkConstants.REAL_PROCESSING_RUNNING, inPlanExecutionID);
			final PlanExecutionContext context = contexts.get(inPlanExecutionID);
			if (context != null) {
				context.saveStatistics(planExecution.getPrimaryKey());
			}
		}
	}

	static void updatePlanExecution(final String inStartedByUser, final String inConfigurationName, final long inPlanExecutionID) {
		final PlanExecution planExecution = getPlanExecution(inPlanExecutionID);
		if (planExecution != null) {
			String processAddress = SystemUtils.getProcessUrl();
			planExecution.setDetails(String.format("%s  [%s]", processAddress, inStartedByUser));
			planExecution.save();
			
			final HtmlLink link = new HtmlLink(planExecution.getPrimaryKey(), String.format("%s %s", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(planExecution.getFromDate()), inConfigurationName));
			LOG.info(String.format("For monitoring the current process, click here:  %s", link.toString()));
		}
	}
	
	public static void logStatistics(final String inKey, final Number inNumber, final long inPlanExecutionID) {
		final PlanExecutionContext context = contexts.get(inPlanExecutionID);
		if (context != null) {
			context.statistics(inKey, inNumber, inPlanExecutionID);
		}
	}
	
	private void statistics(final String inKey, final Number inNumber, final long inPlanExecutionID) {
		if (Strings.isNotEmpty(inKey)) {
			synchronized (statistics) {
				statistics.put(inKey, inNumber);
			}
			saveStatistics(inPlanExecutionID);
		}	
	}

	private void saveStatistics(final long inPlanExecutionID) {
		final PlanExecution planExecution = workService.getPlanExecution(inPlanExecutionID);
		if (planExecution != null) {
			final StringBuilder s = new StringBuilder(SystemUtils.getOsInfo());
			s.append(SystemUtils.getRuntimeInfo());
			s.append(WorkConstants.STATISTICS);
			synchronized (statistics) {
				final List<String> keys = new ArrayList<String>(statistics.keySet());
				Collections.sort(keys);
				for (String key : keys) {
					if (Strings.isNotEmpty(key)) {
						s.append(key);
						s.append(WorkConstants.KV_SEPARATOR);
						final Number value = statistics.get(key);
						if (value != null) {
							s.append(value.toString());
						} else {
							s.append(WorkConstants.NULL);
						}
						s.append(WorkConstants.CR);
					}
				}
			}
			planExecution.save();
		}
	}
}
