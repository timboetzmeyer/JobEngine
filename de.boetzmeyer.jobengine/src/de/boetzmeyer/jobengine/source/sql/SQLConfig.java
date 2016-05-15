package de.boetzmeyer.jobengine.source.sql;


public final class SQLConfig {
	private final String databaseURL;
	private final String user;
	private final String pwd;
	private final String driver;
	
	public SQLConfig(String databaseURL, String user, String pwd, String driver) {
		this.databaseURL = databaseURL;
		this.user = user;
		this.pwd = pwd;
		this.driver = driver;
	}

	public String getDatabaseURL() {
		return databaseURL;
	}

	public String getUser() {
		return user;
	}

	public String getPwd() {
		return pwd;
	}

	public String getDriver() {
		return driver;
	}

}
