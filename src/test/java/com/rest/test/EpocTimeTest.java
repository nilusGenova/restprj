package com.rest.test;

import java.util.Calendar;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.rest.hal9000.EpocTime;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({ EpocTime.class })
public class EpocTimeTest {

    @InjectMocks
    private final EpocTime epocTime = new EpocTime();

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testIsDateValid() {
	Assert.assertFalse("wrong date validator", epocTime.isDateValid(0, 2, 2017));
	Assert.assertFalse("wrong date validator", epocTime.isDateValid(2, 0, 2017));
	Assert.assertFalse("wrong date validator", epocTime.isDateValid(2, 2, 1970));
	Assert.assertFalse("wrong date validator", epocTime.isDateValid(40, 2, 2017));
	Assert.assertFalse("wrong date validator", epocTime.isDateValid(10, 15, 2017));
	Assert.assertTrue("wrong date validator", epocTime.isDateValid(12, 10, 2017));
	Assert.assertTrue("wrong date validator", epocTime.isDateValid(13, 10, 2017));
	Assert.assertFalse("wrong date validator", epocTime.isDateValid(10, 13, 2017));
	Assert.assertTrue("wrong date validator", epocTime.isDateValid(2, 2, 2017));
	Assert.assertFalse("wrong date validator", epocTime.isDateValid(29, 2, 2017));
	Assert.assertTrue("wrong date validator", epocTime.isDateValid(29, 2, 2008));
	Assert.assertTrue("wrong date validator", epocTime.isDateValid(1, 1, 2010));
	Assert.assertTrue("wrong date validator", epocTime.isDateValid(31, 12, 2017));
	Assert.assertFalse("wrong date validator", epocTime.isDateValid(31, 9, 2017));
    }

    @Test
    public void testIsTimeValid() {
	Assert.assertFalse("wrong time validator", epocTime.isTimeValid(-2, 15, 0));
	Assert.assertFalse("wrong time validator", epocTime.isTimeValid(25, 15, 0));
	Assert.assertFalse("wrong time validator", epocTime.isTimeValid(13, -2, 0));
	Assert.assertFalse("wrong time validator", epocTime.isTimeValid(13, 61, 0));
	Assert.assertFalse("wrong time validator", epocTime.isTimeValid(13, 15, -2));
	Assert.assertFalse("wrong time validator", epocTime.isTimeValid(13, 15, 61));
	Assert.assertTrue("wrong time validator", epocTime.isTimeValid(13, 15, 59));
	Assert.assertTrue("wrong time validator", epocTime.isTimeValid(0, 0, 0));
	Assert.assertTrue("wrong time validator", epocTime.isTimeValid(8, 7, 5));
	Assert.assertTrue("wrong time validator", epocTime.isTimeValid(23, 59, 59));
    }

    @Test
    public void testGetEpocTime() {
	Calendar cal = Calendar.getInstance();
	String expDate = String.format("%02d-%02d-%4d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1,
		cal.get(Calendar.YEAR));
	String expTime = String.format("%02d:%02d", cal.get(Calendar.HOUR) + cal.get(Calendar.AM_PM) * 12,
		cal.get(Calendar.MINUTE));

	String date = epocTime.getEpocTime(31, 9, 2017, 8, 7, 5);
	Assert.assertNull("wrong epocTime calculation", date);

	date = epocTime.getEpocTime(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR),
		cal.get(Calendar.HOUR) + cal.get(Calendar.AM_PM) * 12, cal.get(Calendar.MINUTE),
		cal.get(Calendar.SECOND));
	Assert.assertNotNull("wrong epocTime calculation", date);

	epocTime.setEpocTime(Long.parseLong(date));
	Assert.assertEquals("wrong date", expDate, epocTime.getDate());
	Assert.assertEquals("wrong time", expTime, epocTime.getTime());
	Assert.assertEquals("wrong weekDay", (cal.get(Calendar.DAY_OF_WEEK) + 6) % 7, epocTime.getWeekDay());

	date = epocTime.getEpocTime(1, 10, 2017, 22, 1, 0); // 1-oct-2017 is Sunday(0) PM case
	epocTime.setEpocTime(Long.parseLong(date));
	Assert.assertEquals("wrong date", "01-10-2017", epocTime.getDate());
	Assert.assertEquals("wrong time", "22:01", epocTime.getTime());
	Assert.assertEquals("wrong weekDay", 0, epocTime.getWeekDay());

	date = epocTime.getEpocTime(1, 11, 2017, 10, 1, 20); // 1-Nov-2017 AM case
	epocTime.setEpocTime(Long.parseLong(date));
	Assert.assertEquals("wrong date", "01-11-2017", epocTime.getDate());
	Assert.assertEquals("wrong time", "10:01", epocTime.getTime());

	// date:"03-11-2018" time:"15:59" weekDay:6 epochTime:594575948
	date = epocTime.getEpocTime(3, 11, 2018, 15, 59, 8);
	Assert.assertEquals("wrong date epoc time", "594575948", date);

	Assert.assertEquals("wrong year calculation", epocTime.getEpocTime(1, 10, 2017, 22, 1, 0),
		epocTime.getEpocTime(1, 10, 17, 22, 1, 0));
    }

    @Test
    public void testGetEpochOfActualTime() {
	Calendar cal = Calendar.getInstance();
	epocTime.setEpocTime(Long.parseLong(epocTime.getEpocOfActualTime()));
	String date = epocTime.getDate();
	String time = epocTime.getTime();
	int day = Integer.parseInt(date.substring(0, 2));
	int month = Integer.parseInt(date.substring(3, 5));
	int year = Integer.parseInt(date.substring(6, 10));
	int hour = Integer.parseInt(time.substring(0, 2));
	int min = Integer.parseInt(time.substring(3, 5));

	System.out.println("Actual date:" + date);
	System.out.println("Actual time:" + time);

	Assert.assertEquals("Wrong day", cal.get(Calendar.DAY_OF_MONTH), day);
	Assert.assertEquals("Wrong month", cal.get(Calendar.MONTH) + 1, month);
	Assert.assertEquals("Wrong year", cal.get(Calendar.YEAR), year);
	Assert.assertEquals("Wrong hour", cal.get(Calendar.HOUR) + cal.get(Calendar.AM_PM) * 12, hour);
	Assert.assertEquals("Wrong minute", cal.get(Calendar.MINUTE), min);
	Assert.assertTrue("Error in date part of getEpochOfActualTime", epocTime.isDateValid(day, month, year));
	Assert.assertTrue("Error in time part of getEpochOfActualTime", epocTime.isTimeValid(hour, min, 0));
    }

    @Test
    public void testisEpocTimeNotAccurate() {
	String date = epocTime.getEpocTime(1, 4, 2010, 22, 1, 0); // 1-apr-2010
	epocTime.setEpocTime(Long.parseLong(date));
	Assert.assertTrue("Wrong accuration check", epocTime.isEpocTimeNotAccurate());
	epocTime.setEpocTime(Long.parseLong(epocTime.getEpocOfActualTime()));
	Assert.assertFalse("Wrong accuration check", epocTime.isEpocTimeNotAccurate());
    }

}
