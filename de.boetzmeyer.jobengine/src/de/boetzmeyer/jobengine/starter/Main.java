package de.boetzmeyer.jobengine.starter;

public class Main {

	public static void main(String[] args) {
		try {
			PlanStarter.run("PlanB", false, "C:/Users/Tim/JobStore");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("main-Thread finished");
	}

}
