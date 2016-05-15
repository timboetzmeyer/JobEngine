package de.boetzmeyer.jobengine.source.file;

import java.io.File;

public final class FileConfig {
	private final File dir;

	public FileConfig(final File inDir) {
		dir = inDir;
	}

	public String getAbsolutePath() {
		return dir.getAbsolutePath();
	}
	
	@Override
	public int hashCode() {
		return dir.hashCode();
	}

	@Override
	public boolean equals(final Object inObj) {
		if (inObj instanceof FileConfig) {
			return ((FileConfig) inObj).dir.equals(dir);
		}
		return false;
	}

	@Override
	public String toString() {
		return dir.toString();
	}

}
