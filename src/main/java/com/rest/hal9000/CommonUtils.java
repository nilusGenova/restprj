package com.rest.hal9000;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CommonUtils {

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

    public static String getMyIpAddress() throws Exception {
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

}
