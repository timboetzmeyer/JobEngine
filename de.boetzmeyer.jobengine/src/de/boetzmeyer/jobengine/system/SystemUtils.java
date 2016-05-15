package de.boetzmeyer.jobengine.system;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class SystemUtils {
	private static Log sLog = LogFactory.getLog(SystemUtils.class);

	private static final double ONE_MINUTE = 60000.0;
	private static final String LOCALHOST_IP = "127.0.0.1";

	private SystemUtils() {
	}

	public static String getProcessUrl() {
		String processAddress;
		try {
			final InetAddress address = InetAddress.getLocalHost();
			processAddress = address.getHostAddress();
		} catch (final UnknownHostException e) {
			processAddress = LOCALHOST_IP;
		}
		return processAddress;
	}

	public static int getProcessorCount() {
		try {
			return Runtime.getRuntime().availableProcessors();
		} catch (final Exception e) {
			sLog.error(e);
		}
		return 1;
	}

	public static String getRuntimeInfo() {
		final StringBuilder s = new StringBuilder();
		final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
		s.append("RUNTIME-INFO:\r\n");
		final List<String> inputArgs = runtimeBean.getInputArguments();
		for (String arg : inputArgs) {
			s.append(String.format("Input-Argument:   %s\r\n", arg));
		}
		s.append(String.format("BootClassPath:   %s\r\n", runtimeBean.getBootClassPath()));
		s.append(String.format("ClassPath:   %s\r\n", runtimeBean.getClassPath()));
		s.append(String.format("LibraryPath:   %s\r\n", runtimeBean.getLibraryPath()));
		s.append(String.format("ManagementSpecVersion:   %s\r\n", runtimeBean.getManagementSpecVersion()));
		s.append(String.format("Name:   %s\r\n", runtimeBean.getName()));
		s.append(String.format("SpecName:   %s\r\n", runtimeBean.getSpecName()));
		s.append(String.format("SpecVendor:   %s\r\n", runtimeBean.getSpecVendor()));
		s.append(String.format("SpecVersion:   %s\r\n", runtimeBean.getSpecVersion()));
		s.append(String.format("StartTime:   %s\r\n", DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(runtimeBean.getStartTime()))));
		s.append(String.format("Uptime:   %s minutes\r\n", Double.toString(runtimeBean.getUptime() / ONE_MINUTE)));
		s.append(String.format("VmName:   %s\r\n", runtimeBean.getVmName()));
		s.append(String.format("VmVendor:   %s\r\n", runtimeBean.getVmVendor()));
		s.append(String.format("VmVersion:   %s\r\n", runtimeBean.getVmVersion()));
		s.append(String.format("VM Bit Size:  %s\r\n", System.getProperty("sun.arch.data.model")));
		return s.toString();
	}

	public static String getOsInfo() {
		final StringBuilder s = new StringBuilder();
		final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
		s.append("OS-INFO:\r\n");
		s.append(String.format("Arch:   %s\r\n", osBean.getArch()));
		s.append(String.format("AvailableProcessors:   %s\r\n", Integer.toString(osBean.getAvailableProcessors())));
		s.append(String.format("Name:   %s\r\n", osBean.getName()));
		s.append(String.format("Version:   %s\r\n", osBean.getVersion()));
		s.append(String.format("SystemLoadAverage:   %s\r\n", Double.toString(osBean.getSystemLoadAverage())));
		return s.toString();
	}
}
