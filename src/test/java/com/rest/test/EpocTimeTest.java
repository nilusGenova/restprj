package com.rest.test;

import java.util.Calendar;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
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
	String expDate = String.format("%02d-%02d-%4d", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH),
		cal.get(Calendar.YEAR));
	String expTime = String.format("%02d:%02d", cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE));

	String date = epocTime.getEpocTime(31, 9, 2017, 8, 7, 5);
	Assert.assertNull("wrong epocTime calculation", date);

	date = epocTime.getEpocTime(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal.get(Calendar.YEAR),
		cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
	Assert.assertNotNull("wrong epocTime calculation", date);

	epocTime.setEpocTime(Integer.parseInt(date));
	Assert.assertEquals("wrong date", expDate, epocTime.getDate());
	Assert.assertEquals("wrong time", expTime, epocTime.getTime());
	Assert.assertEquals("wrong weekDay", (cal.get(Calendar.DAY_OF_WEEK) + 4) % 7, epocTime.getWeekDay());
    }

}
