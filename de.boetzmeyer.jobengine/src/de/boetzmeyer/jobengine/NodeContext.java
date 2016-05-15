package de.boetzmeyer.jobengine;

import java.util.Date;
import java.util.List;

import de.boetzmeyer.jobstore.jobstore.LogRecord;
import de.boetzmeyer.jobstore.jobstore.PlanJob;
import de.boetzmeyer.jobstore.jobstore.PlanJobExecution;

public interface NodeContext {

	NodeLog getLog();

	void updateBeginTime(final Date inBeginTime);
	
	void cancel();
	
	boolean isCancelled();

	String[] getErrorsAndWarnings();
	
	List<PlanJob> getPlanJobs();
	
	List<PlanJobExecution> getJobHistory();
	
	List<LogRecord> getExecutionErrors();
	
	List<LogRecord> getExecutionInfos();
	
	List<LogRecord> getExecutionWarnings();
	
	boolean hasErrorOccurred(final ErrorDefinition inErrorConstant);
	
}
