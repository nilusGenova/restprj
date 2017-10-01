package com.rest.hal9000;

import java.util.Calendar;

import org.apache.commons.validator.GenericValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EpocTime {
    private final static int[] day1 = { 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335 };
    private final static int[] day2 = { 366, 397, 425, 456, 486, 517, 547, 578, 609, 639, 670, 700 };
    private final static int[] day3 = { 731, 762, 790, 821, 851, 882, 912, 943, 974, 1004, 1035, 1065 };
    private final static int[] day4 = { 1096, 1127, 1155, 1186, 1216, 1247, 1277, 1308, 1339, 1369, 1400, 1430 };

    private final static int[][] days = { day1, day2, day3, day4 }; // days[4][12]

    private static final Logger log = LoggerFactory.getLogger(EpocTime.class);

    private String date;
    private String time;
    private int weekDay; // 0:Sun -- 6:Sat
    private int epochtime; // value in Sec from 1-1-2000

    public EpocTime() {
	super();
	date = "01-01-2000";
	time = "00:00";
	weekDay = 0;
	epochtime = 0;
    }

    public void setEpocTime(int et) {

	epochtime = et;
	weekDay = (epochtime / 3600) / 24;
	weekDay = (weekDay + 6) % 7; // 0:Sun --- 6:Sat

	int hour = ((epochtime / 3600) % 24);
	int min = ((epochtime / 60) % 60);
	time = String.format("%02d:%02d", hour, min);

	int years;
	int epoch = epochtime / 3600;
	epoch /= 24;
	years = epoch / (365 * 4 + 1) * 4;
	epoch %= 365 * 4 + 1;
	int year;
	int month;
	int day;
	for (year = 3; year > 0; year--) {
	    if (epoch >= days[year][0])
		break;
	}
	for (month = 11; month > 0; month--) {
	    if (epoch >= days[year][month])
		break;
	}
	day = epoch - days[year][month] + 1;
	year = 2000 + years + year;
	month += 1;

	date = String.format("%02d-%02d-%4d", day, month, year);
    }

    public String getDate() {
	return date;
    }

    public void setDate(String date) {
	this.date = date;
    }

    public String getTime() {
	return time;
    }

    public void setTime(String time) {
	this.time = time;
    }

    public int getWeekDay() {
	return weekDay;
    }

    public void setWeekDay(int weekDay) {
	this.weekDay = weekDay;
    }

    public boolean isDateValid(int day, int month, int year) {
	return convertDateIfValid(day, month, year) != null;
    }

    public boolean isTimeValid(int hour, int min, int sec) {
	if ((0 <= sec && sec <= 59) && (0 <= min && min <= 59) && (0 <= hour && hour <= 23)) {
	    return true;
	} else {
	    log.error("Invalid time range: {}:{}:{}", hour, min, sec);
	    return false;
	}
    }

    public String getEpocTime(int day, int month, int year, int hour, int min, int sec) {
	if (isTimeValid(hour, min, sec)) {
	    String date = convertDateIfValid(day, month, year);
	    if (date != null) {
		return calculateEpocTime(day, month, year, hour, min, sec);
	    }
	}
	return null;
    }

    public String getEpochOfActualTime() {
	Calendar cal = Calendar.getInstance();
	return calculateEpocTime(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR),
		cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
    }

    private String calculateEpocTime(int day, int month, int year, int hour, int min, int sec) {
	day -= 1;
	month -= 1;
	year -= 2000;

	return Integer.toString(
		(((year / 4 * (365 * 4 + 1) + days[year % 4][month] + day) * 24 + hour) * 60 + min) * 60 + sec);
    }

    private String convertDateIfValid(int day, int month, int year) {
	final String date = String.format("%02d-%02d-%4d", day, month, year);
	if ((1 <= month && month <= 12) && (1 <= day && day <= 31) && (2000 <= year && year <= 2099)) {
	    if (GenericValidator.isDate(date, "dd-MM-yyyy", true)) {
		return date;
	    } else {
		log.error("Invalid Date: {}", date);
		return null;
	    }
	}
	log.error("Invalid date range: {}", date);
	return null;
    }
}
