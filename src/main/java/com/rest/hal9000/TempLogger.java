package com.rest.hal9000;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TempLogger {

    private static final Logger tempLog = LoggerFactory.getLogger(TempLogger.class);

    // store tempVal every 5 minutes ==> 12 tempVal per 1h => 288 tempVal
    // we have the latest 24 hours temps
    private final static int NUM_OF_TEMP_SAMPLES = 288;
    private final static int NUM_OF_MINS_FOR_SAMPLE = 5;
    private final static int MAX_NUM_OF_HOURS_FOR_UPDATE = 5;
    private Double tempAverage = 0.0;
    private int tempSamples = 0;

    private class ExposedData {
	private double[] tempDay = new double[NUM_OF_TEMP_SAMPLES];
	private int latestTempIdx = -1;
    }

    private void storeTempSample(double temp) {
	Calendar cal = Calendar.getInstance();
	Integer nowIdx = (cal.get(Calendar.MINUTE) + 60 * (cal.get(Calendar.HOUR) + cal.get(Calendar.AM_PM) * 12))
		/ NUM_OF_MINS_FOR_SAMPLE;
	if (expData.latestTempIdx == -1) {
	    expData.latestTempIdx = nowIdx;
	}
	if (expData.latestTempIdx != nowIdx) {
	    tempAverage = 0.0;
	    tempSamples = 0;

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
	    expData.latestTempIdx = nowIdx;
	}
	tempAverage += temp;
	tempSamples++;
	expData.tempDay[expData.latestTempIdx] = tempAverage / tempSamples;
    }

    private ExposedData expData = new ExposedData();

    public TempLogger() {
	for (int i = 0; i < NUM_OF_TEMP_SAMPLES; i++) {
	    expData.tempDay[i] = 0.0;
	}
    }

    public void logWarming(final int val) {
	tempLog.info("W,{}", val);
    }

    public void logTemp(final Double val) {
	tempLog.info("T,{}", val);
	storeTempSample(val);
    }

    public void logReqTemp(final int manual, final Double val) {
	if (manual == 1) {
	    tempLog.info("M,{}", val); // Double.toString(val)
	} else {
	    tempLog.info("P,{}", val);
	}
    }

    private double roundTemp(final double t) {
	return (double) (Math.round(t * 10) / 2) / 5; // 5 to have only pair numbers
    }

    public double[] getTempDayCompressed() {
	double val = roundTemp(expData.tempDay[0]);
	int idx = 0;
	int c = 1;
	for (int i = 1; i < NUM_OF_TEMP_SAMPLES; i++) {
	    double t = roundTemp(expData.tempDay[i]);
	    if (val != t) {
		val = t;
		idx++;
		c = 1;
	    } else {
		c++;
	    }
	}
	double[] retArray = new double[2 * (idx + 1)];
	val = roundTemp(expData.tempDay[0]);
	idx = 0;
	c = 1;
	for (int i = 1; i < NUM_OF_TEMP_SAMPLES; i++) {
	    if (val != roundTemp(expData.tempDay[i])) {
		retArray[idx] = val;
		retArray[idx + 1] = (double) c;
		val = roundTemp(expData.tempDay[i]);
		idx += 2;
		c = 1;
	    } else {
		c++;
	    }
	}
	retArray[idx] = val;
	retArray[idx + 1] = (double) c;
	return retArray;
    }

}
