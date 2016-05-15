package de.boetzmeyer.jobengine;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.LogFactory;

import de.boetzmeyer.jobengine.system.SystemUtils;
import de.boetzmeyer.jobstore.jobstore.LogRecord;
import de.boetzmeyer.jobstore.jobstore.PlanJobExecution;

final class NodeLogger implements de.boetzmeyer.jobengine.NodeLog {
	private static final org.apache.commons.logging.Log LOG = LogFactory.getLog(NodeLogger.class);
	
	private static final String INFO = WorkConstants.INFO;
	private static final String WARNING = WorkConstants.WARNING;
	private static final String ERROR = WorkConstants.ERROR;

	private final PlanJobExecution planJobExecution;
	private final String url;
	private final List<String> errorsAndWarnings = new ArrayList<String>();

	public NodeLogger(final PlanJobExecution inPlanJobExecution) {
		planJobExecution = inPlanJobExecution;
		url = SystemUtils.getProcessUrl();
	}

	@Override
	public void logErrorConstant(final ErrorDefinition inErrorConstant) {
		logEntry(ERROR, inErrorConstant.toString());
		errorsAndWarnings.add(String.format("%s: %s", ERROR, inErrorConstant.toString()));
	}

	@Override
	public void logError(final String inText) {
		LOG.error(inText);
		logEntry(ERROR, inText);
		errorsAndWarnings.add(String.format("%s: %s", ERROR, inText));
	}

	@Override
	public void logWarning(final String inText) {
		LOG.warn(inText);
		logEntry(WARNING, inText);
		errorsAndWarnings.add(String.format("%s: %s", WARNING, inText));
	}

	@Override
	public void logInfo(final String inText) {
		LOG.info(inText);
		logEntry(INFO, inText);
	}

	public String[] getErrorsAndWarnings() {
		if (errorsAndWarnings.size() > 0) {
			return errorsAndWarnings.toArray(new String[errorsAndWarnings.size()]);
		}
		return null;
	}
	
	private void logEntry(final String inCategory, final String inText) {
		final LogRecord logRecord = LogRecord.generate();
		logRecord.setPlanJobExecution(planJobExecution.getPrimaryKey());
		logRecord.setLoggingTime(new Date());
		logRecord.setLogLevel(inCategory);
		final String description = String.format("[%s] %s", url, inText);
		logRecord.setDetails(description);
		logRecord.save();
	}
}
