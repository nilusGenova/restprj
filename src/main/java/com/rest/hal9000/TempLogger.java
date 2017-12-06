package com.rest.hal9000;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TempLogger {

    private static final Logger tempLog = LoggerFactory.getLogger(TempLogger.class);

    public TempLogger() {
    }

    public void logTemperature(final String val) {
	tempLog.info("{}", val);
    }

}
