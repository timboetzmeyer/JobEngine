package de.boetzmeyer.jobengine.system;

import java.io.File;

public final class Files {
	private static final double ONE_MB_IN_BYTES = 1048576.0d;

	private Files() {

	}

	public static long getUsableDiskSpace() {
		final String userHome = System.getProperty("user.home");
		if (Strings.isNotEmpty(userHome)) {
			final File file = new File(userHome);
			return file.getUsableSpace();
		}
		return 0L;
	}

	public static double sizeToMB(final long inFilesize) {
		return inFilesize / ONE_MB_IN_BYTES;
	}

}
