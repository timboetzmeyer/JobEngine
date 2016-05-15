package de.boetzmeyer.jobengine;


/**
 * This class defines constants that are used by the import engine.
 *
 */
public abstract class WorkConstants {
	public static final String INFO = "INFO";
	public static final String WARNING = "WARNING";
	public static final String ERROR = "ERROR";
	public static final String CR = "\r\n";
	public static final String NULL = "NULL";
	public static final String KV_SEPARATOR = "=";
	public static final String REAL_PROCESSING_RUNNING = "Job [RUNNING]";
	public static final String REAL_PROCESSING_FINISHED = "Job [FINISHED]";
	public static final String CANCELLED = "CANCELLED";
	public static final String SIMULATION = "Simulation";
	public static final String STATISTICS = "[STATISTICS]";

	private WorkConstants() {
	}

}
