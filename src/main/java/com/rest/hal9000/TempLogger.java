package com.rest.hal9000;

import java.io.BufferedReader;
import java.io.FileReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TempLogger {

    private static final Logger tempLog = LoggerFactory.getLogger(TempLogger.class);

    public TempLogger() {
    }

    public String getLog() {
	final StringBuilder ret = new StringBuilder();
	try (BufferedReader br = new BufferedReader(new FileReader("/tmp/temperatures.log"))) {
	    String line = null;
	    while ((line = br.readLine()) != null) {
		ret.append(line);
		ret.append("\n");
	    }
	} catch (Exception e) {
	    ret.append("ERROR READING THE TEMP-LOG");
	    e.printStackTrace();
	}
	return ret.toString();
    }

    public void logTemperature(final String val) {
	tempLog.info("{}", val);
    }

}
