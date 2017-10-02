package com.rest.hal9000;

import java.nio.file.Files;
import java.nio.file.Paths;

public final class CommonUtils {

    private CommonUtils() {
    }

    public static void initialDebugStatus(String debugFileNamePath) {
	if (Files.exists(Paths.get(debugFileNamePath))) {
	    setLogLevel("DEBUG");
	}
    }

    public static void setLogLevel(String level) {
	CommonUtils o = new CommonUtils();
	String pkgName = o.getClass().getPackage().getName();
	//org.apache.log4j.Logger logger4j = org.apache.log4j.Logger.getRootLogger();
	org.apache.log4j.Logger logger4j = org.apache.log4j.Logger.getLogger(pkgName);
	logger4j.setLevel(org.apache.log4j.Level.toLevel(level));
    }
}
