package de.boetzmeyer.jobengine;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import de.boetzmeyer.jobengine.source.sql.SQLConfig;
import de.boetzmeyer.jobengine.starter.PlanStarter;

final class WorkerHotfolder implements Runnable {
	private static final String KEY_VALUE_SEPARATOR = "=";
	private static final String CONFIG_NAME = "configName";
	private static final long SLEEP_TIME = 1000L;
	private static final String UTF_8 = "UTF-8";
	
	private final File hotFolder;
	private final File coldFolder;
	private final SQLConfig sqlConfig;
	private final File fileConfig;
	private final ExecutorService executorService;
	private final AtomicBoolean running;
	
	
	public WorkerHotfolder(final File inHotFolder, final File inColdFolder, final SQLConfig inSQLConfig) {
		this(inHotFolder, inColdFolder, inSQLConfig, null);
	}
	
	public WorkerHotfolder(final File inHotFolder, final File inColdFolder, final File inConfigDir) {
		this(inHotFolder, inColdFolder, null, inConfigDir);
	}
	
	private WorkerHotfolder(final File inHotFolder, final File inColdFolder, final SQLConfig inSQLConfig, final File inConfigDir) {
		hotFolder = inHotFolder;
		coldFolder = inColdFolder;
		sqlConfig = inSQLConfig;
		fileConfig = inConfigDir;		
		executorService = Executors.newFixedThreadPool(2);
		running = new AtomicBoolean(true);
		executorService.submit(this);
	}
	
	@Override
	public final void run() {
		while (running.get()) {
			try {
				final File[] files = hotFolder.listFiles();
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						try {
							if (files[i].isFile()) {
								final Path pathSource = files[i].toPath();
								final String configName = readPlanTitle(pathSource);
								executePlan(files[i], pathSource, configName);
							}
						} catch (final Exception e) {
							e.printStackTrace();
						}
					}
				}
				Thread.sleep(SLEEP_TIME);
			} catch (final Throwable e) {
				e.printStackTrace();
			}
		}
	}

	private String readPlanTitle(final Path pathSource) throws IOException {
		final List<String> lines = Files.readAllLines(pathSource , Charset.forName(UTF_8));
		String configName = null;
		for (String line : lines) {
			final String[] keyValue = line.split(KEY_VALUE_SEPARATOR);
			if (keyValue.length == 2) {
				final String key = keyValue[0].trim().toLowerCase();
				if (key.equalsIgnoreCase(CONFIG_NAME)) {
					final String value = keyValue[1].trim();
					if (value.length() > 0) {
						configName = value;
						break;
					}
				}
			}
		}
		return configName;
	}

	private void executePlan(final File file, final Path pathSource, String configName) {
		if (configName != null) {
			try {
				if (sqlConfig != null) {
					PlanStarter.run(configName, true, sqlConfig);
				} else {
					PlanStarter.run(configName, true, fileConfig.getAbsolutePath());
				}
				final String fileName = file.getName();
				final String target = String.format("%s%s%s_%s", coldFolder.getAbsolutePath(), 
						Character.toString(File.separatorChar), Long.toString(System.currentTimeMillis()), fileName);
				final Path pathTarget = new File(target).toPath();
				if (Files.copy(pathSource, pathTarget, StandardCopyOption.REPLACE_EXISTING) != null) {
					file.delete();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public final void shutdown() {
		running.set(false);
		executorService.shutdownNow();
	}
}
