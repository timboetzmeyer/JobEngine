package de.boetzmeyer.jobengine.examples;

import de.boetzmeyer.jobengine.AbstractWork;

abstract class BaseJob extends AbstractWork {
	
	private static final long SLEEP_TIME = 10000L;

	protected final void sleep() {
		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
