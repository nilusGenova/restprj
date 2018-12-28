package com.rest.hal9000;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;

import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CommonUtils {

    private static final long IP_REFRESH_TIMEOUT = (1000 * 60); // in milliseconds

    protected static CachedInfo<String> ipCIDRaddress = new CachedInfo<>(() -> get_CIDR_IpAddress(), IP_REFRESH_TIMEOUT,
	    "IPaddress");

    protected static final Logger log = LoggerFactory.getLogger(CommonUtils.class);

    private CommonUtils() {
    }

    public static void initialDebugStatus(final boolean setDebugLogLevel) {
	if (setDebugLogLevel) {
	    setLogLevel("DEBUG");
	}
    }

    public static void setLogLevel(final String level) {
	CommonUtils o = new CommonUtils();
	String pkgName = o.getClass().getPackage().getName();
	// org.apache.log4j.Logger logger4j = org.apache.log4j.Logger.getRootLogger();
	org.apache.log4j.Logger logger4j = org.apache.log4j.Logger.getLogger(pkgName);
	logger4j.setLevel(org.apache.log4j.Level.toLevel(level));
    }

    public static void setLogPath(final String path) {
	if (path == null || "".equals(path)) {
	    return;
	}
	Enumeration<org.apache.log4j.Appender> e = org.apache.log4j.Logger.getRootLogger().getAllAppenders();
	while (e.hasMoreElements()) {
	    org.apache.log4j.Appender app = e.nextElement();
	    if (app instanceof org.apache.log4j.FileAppender) {
		// I've to get file name removing the leading path
		Path origLogFile = Paths.get(((org.apache.log4j.FileAppender) app).getFile());
		((org.apache.log4j.FileAppender) app).setFile(path + origLogFile.getFileName());
		((org.apache.log4j.FileAppender) app).activateOptions();
	    }
	}

	e = org.apache.log4j.Logger.getLogger(TempLogger.class).getAllAppenders();
	while (e.hasMoreElements()) {
	    org.apache.log4j.Appender app = e.nextElement();
	    if (app instanceof org.apache.log4j.FileAppender) {
		// I've to get file name removing the leading path
		Path origLogFile = Paths.get(((org.apache.log4j.FileAppender) app).getFile());
		((org.apache.log4j.FileAppender) app).setFile(path + origLogFile.getFileName());
		((org.apache.log4j.FileAppender) app).activateOptions();
	    }
	}

	e = org.apache.log4j.Logger.getLogger(AlarmLogger.class).getAllAppenders();
	while (e.hasMoreElements()) {
	    org.apache.log4j.Appender app = e.nextElement();
	    if (app instanceof org.apache.log4j.FileAppender) {
		// I've to get file name removing the leading path
		Path origLogFile = Paths.get(((org.apache.log4j.FileAppender) app).getFile());
		((org.apache.log4j.FileAppender) app).setFile(path + origLogFile.getFileName());
		((org.apache.log4j.FileAppender) app).activateOptions();
	    }
	}
	log.info("Log path updated to:{}", path);
    }

    public static String getTempLoggerFilePath() {
	Enumeration<org.apache.log4j.Appender> e = org.apache.log4j.Logger.getLogger(TempLogger.class)
		.getAllAppenders();
	while (e.hasMoreElements()) {
	    org.apache.log4j.Appender app = e.nextElement();
	    if (app instanceof org.apache.log4j.FileAppender) {
		return ((org.apache.log4j.FileAppender) app).getFile();
	    }
	}
	log.error("Temp Logger file not found");
	return "";
    }

    public static String getAlarmLoggerFilePath() {
	Enumeration<org.apache.log4j.Appender> e = org.apache.log4j.Logger.getLogger(AlarmLogger.class)
		.getAllAppenders();
	while (e.hasMoreElements()) {
	    org.apache.log4j.Appender app = e.nextElement();
	    if (app instanceof org.apache.log4j.FileAppender) {
		return ((org.apache.log4j.FileAppender) app).getFile();
	    }
	}
	log.error("Temp Logger file not found");
	return "";
    }

    public static String getLoggerFilePath() {
	Enumeration<org.apache.log4j.Appender> e = org.apache.log4j.Logger.getRootLogger().getAllAppenders();
	while (e.hasMoreElements()) {
	    org.apache.log4j.Appender app = e.nextElement();
	    if (app instanceof org.apache.log4j.FileAppender) {
		return ((org.apache.log4j.FileAppender) app).getFile();
	    }
	}
	log.error("Root Logger file not found");
	return "";
    }

    private static String get_CIDR_IpAddress() {
	Enumeration<NetworkInterface> e;
	try {
	    log.debug("Getting local IP address");
	    e = NetworkInterface.getNetworkInterfaces();
	    while (e.hasMoreElements()) {
		NetworkInterface n = (NetworkInterface) e.nextElement();
		String ipAddrCache = n.getInterfaceAddresses().get(1).getAddress().getHostAddress();
		Short ipMaskCache = n.getInterfaceAddresses().get(1).getNetworkPrefixLength();
		log.debug("Local IP address:{}/{}", ipAddrCache, ipMaskCache);
		return ipAddrCache + "/" + ipMaskCache;
	    }
	} catch (SocketException e1) {
	    log.error("Local IpAddress not found");
	}
	log.debug("Local IP address:127.0.0.1/24(default)");
	return "127.0.0.1/24";
    }

    public static String getMyIpAddress() {
	return ipCIDRaddress.getInfo().split("/")[0];
    }

    public static Short getMyIpNetworkPrefixLength() {
	return Short.parseShort(ipCIDRaddress.getInfo().split("/")[1]);
    }

    public static String getMy_CIDR_IpAddress() {
	return ipCIDRaddress.getInfo();
    }

    public static boolean isInLocalSubnet(String ip) throws SocketException {
	return new SubnetUtils(ipCIDRaddress.getInfo()).getInfo().isInRange(ip);
    }

    public static String alternateGetMyIpAddress() throws Exception {
	Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
	int ctr = 0;
	while (e.hasMoreElements()) {
	    NetworkInterface n = (NetworkInterface) e.nextElement();
	    Enumeration<InetAddress> ee = n.getInetAddresses();
	    while (ee.hasMoreElements() && ctr < 3) {
		ctr++;
		if (ctr == 3) {
		    break;
		}
		InetAddress i = ee.nextElement();
		if (ctr == 2) {
		    return i.getHostAddress();
		}

	    }
	}
	log.error("Local IpAddress not found");
	return "127.0.0.1";
    }
}
