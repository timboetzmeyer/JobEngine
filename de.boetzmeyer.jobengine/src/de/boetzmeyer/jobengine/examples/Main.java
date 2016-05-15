package de.boetzmeyer.jobengine.examples;

import de.boetzmeyer.jobengine.starter.PlanStarter;

public class Main {
	private static final String EXAMPLE_JOB_STORE_PATH = "C:/Users/Tim/JobStore";

	public static void main(String[] args) {
		try {
			PlanStarter.run("PlanB", false, EXAMPLE_JOB_STORE_PATH);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("main-Thread finished");
	}

}
