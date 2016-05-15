package de.boetzmeyer.jobengine.starter;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import de.boetzmeyer.jobengine.IllegalWorkStarterException;
import de.boetzmeyer.jobengine.NodeListener;
import de.boetzmeyer.jobengine.PlanConfiguration;
import de.boetzmeyer.jobengine.distributed.ApplicationPool;
import de.boetzmeyer.jobengine.distributed.ApplicationRequest;
import de.boetzmeyer.jobengine.source.file.ModelFiles;
import de.boetzmeyer.jobengine.source.sql.SQLConfig;
import de.boetzmeyer.jobengine.system.Strings;
import de.boetzmeyer.jobengine.system.SystemUtils;

public final class PlanStarter {
	private static final Log LOG = LogFactory.getLog(PlanStarter.class);

	private PlanStarter() {
	}

	public static void run(final String inPlanTitle, final boolean inSimulation, final String inJobStoreDir) throws Exception {
		run(null, null, inPlanTitle, inSimulation, inJobStoreDir, null);
	}

	public static void run(final String inPlanTitle, final boolean inSimulation, final SQLConfig inConfig) throws Exception {
		run(null, null, inPlanTitle, inSimulation, null, inConfig);
	}

	public static void run(final NodeListener inNodeListener, final String inPlanTitle, final boolean inSimulation, final String inJobStoreDir) throws Exception {
		run(inNodeListener, null, inPlanTitle, inSimulation, inJobStoreDir, null);
	}

	public static void run(final NodeListener inNodeListener, final String inPlanTitle, final boolean inSimulation, final SQLConfig inConfig) throws Exception {
		run(inNodeListener, null, inPlanTitle, inSimulation, null, inConfig);
	}
	
	private static void run(final NodeListener inNodeListener, final ApplicationRequest inApplicationRequest, final String inPlanTitle, final boolean inSimulation,
			final String inJobStoreDir, final SQLConfig inConfig) throws Exception {
		final String user = "unknown";
		boolean runFromFile = false;
		if (Strings.isNotEmpty(inJobStoreDir)) {
			final String jobStoreFile = String.format("%s%s%s%s", inJobStoreDir,
					Character.toString(File.separatorChar), ModelFiles.DB_FILE_NAME, ModelFiles.DB_EXTENSION);
			final File file = new File(jobStoreFile);
			if (file.exists()) {
				runFromFile = true;
			}
		}
		try {
			final int maxThreadCount = SystemUtils.getProcessorCount();
			final PlanConfiguration engine;
			if (runFromFile) {
				engine = createPlanFromLocalFileConfig(inNodeListener, maxThreadCount, inSimulation, inPlanTitle, inJobStoreDir);
			} else {
				engine = createPlanFromDatabaseConfig(inNodeListener, maxThreadCount, inSimulation, inPlanTitle, inApplicationRequest, inConfig);
			}
			engine.executePlan(user);
		} catch (final IllegalWorkStarterException e) {
			LOG.error(e);
			throw new Exception(e);
		}
	}

	private static PlanConfiguration createPlanFromLocalFileConfig(final NodeListener inNodeListener, final int inMaxThreadCount, final boolean inSimulation,
			final String inPlanTitle, final String inJobStoreDir) throws IllegalWorkStarterException {
		return new PlanConfiguration(inNodeListener, inSimulation, inMaxThreadCount, inPlanTitle, inJobStoreDir);
	}

	private static PlanConfiguration createPlanFromDatabaseConfig(final NodeListener inNodeListener, final int inMaxThreadCount, 
			final boolean inSimulation, final String inPlanTitle, final ApplicationRequest inApplicationRequest, final SQLConfig inConfig) throws IllegalWorkStarterException {
		final ApplicationPool appPool = new ApplicationPool();
		final String databaseURL = inConfig.getDatabaseURL();
		final String user = inConfig.getUser();
		final String pwd = inConfig.getPwd();
		final String driver = inConfig.getDriver();
		LOG.info(String.format("Connecting to job store on '%s' as user '%s'", databaseURL, user));
		return new PlanConfiguration(inNodeListener, inSimulation,
				inMaxThreadCount, inPlanTitle, driver, databaseURL, user, pwd, appPool, inApplicationRequest);
	}
}
