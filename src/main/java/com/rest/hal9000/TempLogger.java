package com.rest.hal9000;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TempLogger extends HalObjAgent {

    private static final Logger tempLog = LoggerFactory.getLogger(TempLogger.class);

    // store tempVal every 5 minutes ==> 12 tempVal per 1h => 288 tempVal
    // we have the latest 24 hours temps
    private final static int NUM_OF_TEMP_SAMPLES = 288;
    private final static int NUM_OF_MINS_FOR_SAMPLE = 5;
    private final static int MAX_NUM_OF_HOURS_FOR_UPDATE = 5;

    private class ExposedData {
	private Double[] tempDay = new Double[NUM_OF_TEMP_SAMPLES];
	private int latestTempIdx = -1;
    }

    private void storeLastTempSample(Double temp) {
	Calendar cal = Calendar.getInstance();
	Integer nowIdx = (cal.get(Calendar.MINUTE) + 60 * (cal.get(Calendar.HOUR) + cal.get(Calendar.AM_PM) * 12))
		/ NUM_OF_MINS_FOR_SAMPLE;
	if (expData.latestTempIdx == -1) {
	    expData.latestTempIdx = nowIdx;
	}
	if ((nowIdx > expData.latestTempIdx ? nowIdx - expData.latestTempIdx
		: nowIdx + NUM_OF_TEMP_SAMPLES - expData.latestTempIdx - 1) < (MAX_NUM_OF_HOURS_FOR_UPDATE * 60
			/ NUM_OF_MINS_FOR_SAMPLE)) {
	    // loop to update sample from previous to now
	    for (int i = expData.latestTempIdx; i != nowIdx; i++) {
		if (i == NUM_OF_TEMP_SAMPLES) {
		    i = 0;
		}
		expData.tempDay[i] = expData.tempDay[expData.latestTempIdx];
	    }
	}
	expData.tempDay[nowIdx] = temp;
	expData.latestTempIdx = nowIdx;
    }

    private ExposedData expData = new ExposedData();

    public TempLogger(final String pathName) {
	super(pathName, null);
	for (int i = 0; i < NUM_OF_TEMP_SAMPLES; i++) {
	    expData.tempDay[i] = 0.0;
	}
    }

    public void logWarming(final int val) {
	tempLog.info("W,{}", val);
    }

    public void logTemp(final Double val) {
	tempLog.info("T,{}", val);
	storeLastTempSample(val);
    }

    public void logReqTemp(final int manual, final Double val) {
	if (manual == 1) {
	    tempLog.info("M,{}", val); // Double.toString(val)
	} else {
	    tempLog.info("P,{}", val);
	}
    }

    @Override
    protected Object getExposedData() {
	return expData.tempDay;
    }

    @Override
    protected String getExposedAttribute(String attr) throws Exception {
	wrongAttribute(attr);
	return null;
    }

    @Override
    protected boolean specializedParseGetAnswer(char attribute, String msg) {
	return false;
    }

    @Override
    protected boolean specializedParseEvent(char event, String msg) {
	return false;
    }

    @Override
    public void alignAll() {
    }
}
