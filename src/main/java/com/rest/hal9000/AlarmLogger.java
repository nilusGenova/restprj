package com.rest.hal9000;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmLogger {

    private static final Logger alarmLog = LoggerFactory.getLogger(AlarmLogger.class);

    public AlarmLogger() {
    }

    public void logKeyRead(final String key) {
	alarmLog.info("KEY,{}", key);
    }

    public void logPinRead(final String pin) {
	alarmLog.info("PIN,{}", pin);
    }

    public void logNewPin(final String pin) {
	alarmLog.info("NEWPIN,{}", pin);
    }

    public void logKeyProgramming(final boolean changed, final int onOff) {
	if (changed) {
	    alarmLog.info("PROG,{}", onOff);
	}
    }

    public void logAlarm(final boolean changed, final int onOff) {
	if (changed) {
	    alarmLog.info("ALARM,{}", onOff);
	}
    }

    public void logArmed(final boolean changed, final int onOff) {
	if (changed) {
	    alarmLog.info("ARMED,{}", onOff);
	}
    }

}
