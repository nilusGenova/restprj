package com.rest.hal9000;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TempLogger {

    private static final Logger tempLog = LoggerFactory.getLogger(TempLogger.class);

    public TempLogger() {
    }

    public void logWarming(final int val) {
	tempLog.info("W,{}", val);
    }

    public void logTemp(final Double val) {
	tempLog.info("T,{}", val);
    }

    public void logReqTemp(final int manual, final Double val) {
	if (manual == 1) {
	    tempLog.info("M,{}", val); // Double.toString(val)
	} else {
	    tempLog.info("P,{}", val);
	}
    }
}
