package com.rest.hal9000;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Enumeration;

import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CommonUtils {

    private static final long IP_REFRESH_TIMEOUT = (1000 * 60); // in milliseconds
    protected static String ipAddrCache = null;
    protected static Short ipMaskCache = 0;
    private static volatile long lastUpdateTime = 0;

    protected static final Logger log = LoggerFactory.getLogger(CommonUtils.class);

    private CommonUtils() {
    }

    public static void initialDebugStatus(final String debugFileNamePath) {
	if (Files.exists(Paths.get(debugFileNamePath))) {
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

    private static void refreshIpAddress() {
	final long actualTime = Calendar.getInstance().getTimeInMillis();
	if ((ipAddrCache == null) || ((actualTime - lastUpdateTime) > IP_REFRESH_TIMEOUT)) {
	    Enumeration<NetworkInterface> e;
	    try {
		e = NetworkInterface.getNetworkInterfaces();
		while (e.hasMoreElements()) {
		    NetworkInterface n = (NetworkInterface) e.nextElement();
		    ipAddrCache = n.getInterfaceAddresses().get(1).getAddress().getHostAddress();
		    ipMaskCache = n.getInterfaceAddresses().get(1).getNetworkPrefixLength();
		    lastUpdateTime = Calendar.getInstance().getTimeInMillis();
		    log.debug("Local Ip Address refreshed");
		    break;
		}
	    } catch (SocketException e1) {
		log.error("Local IpAddress not found");
		ipAddrCache = "127.0.0.1";
		ipMaskCache = 24;
	    }
	}
    }

    public static String getMyIpAddress() {
	refreshIpAddress();
	return ipAddrCache;
    }

    public static Short getMyIpNetworkPrefixLength() {
	refreshIpAddress();
	return ipMaskCache;
    }

    public static String getMy_CIDR_IpAddress() {
	refreshIpAddress();
	return ipAddrCache + "/" + ipMaskCache;
    }

    public static boolean isInLocalSubnet(String ip) throws SocketException {
	refreshIpAddress();
	return new SubnetUtils(ipAddrCache + "/" + ipMaskCache).getInfo().isInRange(ip);
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
