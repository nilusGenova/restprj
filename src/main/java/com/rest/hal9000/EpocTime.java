package com.rest.hal9000;

import java.util.Calendar;

import org.apache.commons.validator.GenericValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EpocTime {
    private final static int[] DAY1 = { 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335 };
    private final static int[] DAY2 = { 366, 397, 425, 456, 486, 517, 547, 578, 609, 639, 670, 700 };
    private final static int[] DAY3 = { 731, 762, 790, 821, 851, 882, 912, 943, 974, 1004, 1035, 1065 };
    private final static int[] DAY4 = { 1096, 1127, 1155, 1186, 1216, 1247, 1277, 1308, 1339, 1369, 1400, 1430 };

    private final static int[][] DAYS = { DAY1, DAY2, DAY3, DAY4 }; // days[4][12]

    private static final Logger LOG = LoggerFactory.getLogger(EpocTime.class);

    private final static int NTP_TOLLERANCE = 61; // in Sec

    private String date;
    private String time;
    private int weekDay; // 0:Sun -- 6:Sat
    private long epochTime; // value in Sec from 1-1-2000

    public EpocTime() {
	date = "01-01-2000";
	time = "00:00";
	weekDay = 0;
	epochTime = 0;
    }

    public void setEpocTime(final long et) {
	LOG.debug("Setting Epoc time:{}", et);
	epochTime = et;
	weekDay = (int) (epochTime / 3600) / 24;
	weekDay = (weekDay + 6) % 7; // 0:Sun --- 6:Sat

	int hour = (int) ((epochTime / 3600) % 24);
	int min = (int) ((epochTime / 60) % 60);
	time = String.format("%02d:%02d", hour, min);
	LOG.debug("WeekDay:{}", weekDay);
	LOG.debug("Time:" + time);

	int years;
	long localEpoch = epochTime / 3600;
	localEpoch /= 24;
	years = (int) localEpoch / (365 * 4 + 1) * 4;
	localEpoch %= 365 * 4 + 1;
	int year;
	int month;
	int day;
	for (year = 3; year > 0; year--) {
	    if (localEpoch >= DAYS[year][0])
		break;
	}
	for (month = 11; month > 0; month--) {
	    if (localEpoch >= DAYS[year][month])
		break;
	}
	day = (int) localEpoch - DAYS[year][month] + 1;
	year = 2000 + years + year;
	month += 1;

	date = String.format("%02d-%02d-%4d", day, month, year);
	LOG.debug("Date:" + date);
    }

    public String getDate() {
	return date;
    }

    public void setDate(final String date) {
	this.date = date;
    }

    public String getTime() {
	return time;
    }

    public void setTime(final String time) {
	this.time = time;
    }

    public int getWeekDay() {
	return weekDay;
    }

    public String getEpochTime() {
	return Long.toString(epochTime);
    }

    public void setWeekDay(final int weekDay) {
	this.weekDay = weekDay;
    }

    public boolean isDateValid(final int day, final int month, final int year) {
	return convertDateIfValid(day, month, year) != null;
    }

    public boolean isTimeValid(final int hour, final int min, final int sec) {
	if ((0 <= sec && sec <= 59) && (0 <= min && min <= 59) && (0 <= hour && hour <= 23)) {
	    return true;
	} else {
	    LOG.error("Invalid time range: {}:{}:{}", hour, min, sec);
	    return false;
	}
    }

    public String getEpocTime(final int day, final int month, int year, final int hour, final int min, final int sec) {
	if (year < 2000) {
	    year += 2000;
	}
	LOG.debug("getEpocTime of {}-{}-{} {}:{}:{}", day, month, year, hour, min, sec);
	if (isTimeValid(hour, min, sec)) {
	    String date = convertDateIfValid(day, month, year);
	    if (date != null) {
		return calculateEpocTime(day, month, year, hour, min, sec);
	    }
	}
	return null;
    }

    public String getEpocOfActualTime() {
	LOG.debug("getEpocOfActualTime");
	Calendar cal = Calendar.getInstance();
	return calculateEpocTime(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR),
		cal.get(Calendar.HOUR) + cal.get(Calendar.AM_PM) * 12, cal.get(Calendar.MINUTE),
		cal.get(Calendar.SECOND));
    }

    public boolean isEpocTimeNotAccurate() {
	LOG.debug("isEpocTimeNotAccurate");
	Calendar cal = Calendar.getInstance();
	long actualTime = calculateEpocTimeInSecs(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1,
		cal.get(Calendar.YEAR), cal.get(Calendar.HOUR) + cal.get(Calendar.AM_PM) * 12, cal.get(Calendar.MINUTE),
		cal.get(Calendar.SECOND));
	return Math.abs(actualTime - epochTime) > NTP_TOLLERANCE;
    }

    private Integer calculateEpocTimeInSecs(int day, int month, int year, final int hour, final int min,
	    final int sec) {
	LOG.debug("Calculate EpocTime of {}-{}-{} {}:{}:{}", day, month, year, hour, min, sec);
	day -= 1;
	month -= 1;
	year -= 2000;

	return ((((year / 4 * (365 * 4 + 1) + DAYS[year % 4][month] + day) * 24 + hour) * 60 + min) * 60 + sec);
    }

    private String calculateEpocTime(int day, int month, int year, final int hour, final int min, final int sec) {
	LOG.debug("Calculate EpocTime of {}-{}-{} {}:{}:{}", day, month, year, hour, min, sec);

	String retVal = Integer.toString(calculateEpocTimeInSecs(day, month, year, hour, min, sec));
	LOG.debug("Epoc is:{}", retVal);
	return retVal;
    }

    private String convertDateIfValid(final int day, final int month, final int year) {
	final String date = String.format("%02d-%02d-%4d", day, month, year);
	if ((1 <= month && month <= 12) && (1 <= day && day <= 31) && (2000 <= year && year <= 2099)) {
	    if (GenericValidator.isDate(date, "dd-MM-yyyy", true)) {
		return date;
	    } else {
		LOG.error("Invalid Date: {}", date);
		return null;
	    }
	}
	LOG.error("Invalid date range: {}", date);
	return null;
    }
}
