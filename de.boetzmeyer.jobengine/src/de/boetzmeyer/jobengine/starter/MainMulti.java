package de.boetzmeyer.jobengine.starter;

public class MainMulti {
	
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
			PlanStarter.run("PlanC", false, "C:/Users/Tim/JobStore");
			sleep();
			PlanStarter.run("PlanB", false, "C:/Users/Tim/JobStore");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("main-Thread finished");
	}

}
