package de.boetzmeyer.jobengine.examples;

import de.boetzmeyer.jobengine.starter.PlanStarter;

public class MainMulti {
	
	private static final String EXAMPLE_JOB_STORE_PATH = "C:/Users/Tim/JobStore";
	private static final long SLEEP_TIME = 15000L;

	private static final void sleep() {
		try {
			Thread.sleep(SLEEP_TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			PlanStarter.run("PlanC", false, EXAMPLE_JOB_STORE_PATH);
			sleep();
			PlanStarter.run("PlanB", false, EXAMPLE_JOB_STORE_PATH);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("main-Thread finished");
	}

}
