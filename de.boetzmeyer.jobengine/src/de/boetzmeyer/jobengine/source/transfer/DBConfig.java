package de.boetzmeyer.jobengine.source.transfer;

public final class DBConfig {

	private final String serverName;
	private final int port;
	private final String driverClass;
	private final String driverProtocol;
	private final String userName;
	private final String password;
	private final String database;

	public DBConfig(final String inServerName, final int inPort, final String inDriverClass, final String inDriverProtocol, 
			final String inUserName, final String inPassword, final String inDatabase) {
		serverName = inServerName;
		port = inPort;
		driverClass = inDriverClass;
		driverProtocol = inDriverProtocol;
		userName = inUserName;
		password = inPassword;
		database = inDatabase;
	}
	
	public String getServerName() {
		return serverName;
	}
	
	public int getPort() {
		return port;
	}
	
	public String getDriverClass() {
		return driverClass;
	}
	
	public String getDriverProtocol() {
		return driverProtocol;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public String getPassword() {
		return password;
	}

	public String getDatabase() {
		return database;
	}

	@Override
	public int hashCode() {
		return serverName.toLowerCase().hashCode();
	}

	@Override
	public boolean equals(final Object inObj) {
		if (inObj instanceof DBConfig) {
			final DBConfig config = (DBConfig) inObj;
			return (config.serverName.equalsIgnoreCase(serverName) && config.database.equalsIgnoreCase(database) && (config.port == port));
		}
		return false;
	}

	@Override
	public String toString() {
		return String.format("%s:%s/%s", serverName, Integer.toString(port), database);
	}
}
