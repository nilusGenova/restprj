package com.rest.test;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Calendar;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import com.rest.hal9000.TempLogger;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({ TempLogger.class, Logger.class, Calendar.class })
public class TempLoggerTest {

    private final static int NUM_OF_TEMP_SAMPLES = 288; // so every 5 minutes
    private final static int NUM_OF_MINS_FOR_SAMPLE = 5;

    @Mock
    private Logger logger;

    @Mock
    private Calendar calendar;

    @InjectMocks
    private final TempLogger tempLogger = new TempLogger();

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
    }

    private double[] generateRetArray(double[] zippedArray) {
	double[] retArray = new double[NUM_OF_TEMP_SAMPLES];
	// printZippedArray(zippedArray);
	int idx = 0;
	double value = 0;
	double count;
	for (int i = 0; i < zippedArray.length; i += 2) {
	    value = zippedArray[i];
	    count = zippedArray[i + 1];
	    for (int j = 0; j < count; j++) {
		retArray[idx++] = value;
	    }
	}
	return retArray;
    }

    private void printZippedArray(double zar[]) {
	System.out.print("{" + zar[0] + ":" + zar[1]);
	for (int i = 1; i < zar.length; i += 2) {
	    System.out.print("," + zar[i] + ":" + zar[i + 1]);
	}
	System.out.println("}");
    }

    private void printArray(double ar[]) {
	System.out.print("{" + ar[0]);
	for (int i = 1; i < NUM_OF_TEMP_SAMPLES; i++) {
	    System.out.print("," + ar[i]);
	}
	System.out.println("}");
    }

    private double roundTemp(final double t) {
	return (double) (Math.round(t * 10) / 2) / 5; // to have only pair numbers
    }

    @Test
    public void testSampling() {
	PowerMockito.mockStatic(Calendar.class);
	PowerMockito.when(Calendar.getInstance()).thenReturn(calendar);
	when(calendar.get(Calendar.MINUTE)).thenReturn(5);
	when(calendar.get(Calendar.HOUR)).thenReturn(2);
	when(calendar.get(Calendar.AM_PM)).thenReturn(0);

	for (int idx = 0; idx < NUM_OF_TEMP_SAMPLES; idx++) {
	    when(calendar.get(Calendar.HOUR)).thenReturn((idx * NUM_OF_MINS_FOR_SAMPLE / 60) % 12);
	    when(calendar.get(Calendar.AM_PM)).thenReturn((idx * NUM_OF_MINS_FOR_SAMPLE / 60) < 12 ? 0 : 1);
	    int min = (idx * NUM_OF_MINS_FOR_SAMPLE) % 60;
	    when(calendar.get(Calendar.MINUTE)).thenReturn(min); // unuseful here

	    for (int m = 0; m < NUM_OF_MINS_FOR_SAMPLE; m++) {
		when(calendar.get(Calendar.MINUTE)).thenReturn(min + m);
		tempLogger.logTemp(2.5 + (m - NUM_OF_MINS_FOR_SAMPLE / 2) + (idx + 0.0) / 10);
	    }
	    tempLogger.logTemp(2.5 + (idx + 0.0) / 10);
	}

	try {
	    double[] retArray = generateRetArray(tempLogger.getTempDayCompressed());
	    // printArray(retArray);

	    int cnt = 0;
	    for (int i = 0; i < NUM_OF_TEMP_SAMPLES; i++) {
		Assert.assertEquals("ERROR in samples #" + cnt + " :", roundTemp(2.5 + (cnt + 0.0) / 10), retArray[i],
			0);
		cnt++;
	    }
	    Assert.assertEquals("wrong num of samples", NUM_OF_TEMP_SAMPLES, cnt); // now impossible
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("ERROR");
	}
    }

    @Test
    public void testFillingsamples() {
	PowerMockito.mockStatic(Calendar.class);
	PowerMockito.when(Calendar.getInstance()).thenReturn(calendar);
	when(calendar.get(Calendar.MINUTE)).thenReturn(0);
	when(calendar.get(Calendar.HOUR)).thenReturn(0);
	when(calendar.get(Calendar.AM_PM)).thenReturn(0);
	tempLogger.logTemp(12.1);

	when(calendar.get(Calendar.MINUTE)).thenReturn(7);
	when(calendar.get(Calendar.HOUR)).thenReturn(1);
	when(calendar.get(Calendar.AM_PM)).thenReturn(0);
	tempLogger.logTemp(15.7);

	try {
	    double[] retArray = generateRetArray(tempLogger.getTempDayCompressed());
	    // printArray(retArray);
	    int cnt = 0;
	    for (int i = 0; i < NUM_OF_TEMP_SAMPLES; i++) {
		if (cnt < 13) { // 1houre+7min = 12+1 samples
		    Assert.assertEquals("ERROR in filling #" + cnt + " :", 12.0, retArray[i], 0);
		}
		if (cnt == 13) {
		    Assert.assertEquals("ERROR in filling #" + cnt + " :", 15.6, retArray[i], 0);
		}
		if (cnt > 13) {
		    break;
		}
		cnt++;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("ERROR");
	}
    }

    @Test
    public void testFillingsamplesOverDay() {
	PowerMockito.mockStatic(Calendar.class);
	PowerMockito.when(Calendar.getInstance()).thenReturn(calendar);
	when(calendar.get(Calendar.MINUTE)).thenReturn(30);
	when(calendar.get(Calendar.HOUR)).thenReturn(11);
	when(calendar.get(Calendar.AM_PM)).thenReturn(1);
	tempLogger.logTemp(18.2);

	when(calendar.get(Calendar.MINUTE)).thenReturn(27);
	when(calendar.get(Calendar.HOUR)).thenReturn(0);
	when(calendar.get(Calendar.AM_PM)).thenReturn(0);
	tempLogger.logTemp(21.3);

	try {
	    double[] retArray = generateRetArray(tempLogger.getTempDayCompressed());
	    int cnt = 0;
	    for (int i = 0; i < NUM_OF_TEMP_SAMPLES; i++) {
		if ((cnt < 5) || (cnt >= 282)) { // ((11+12)*60 + 30)/5
		    Assert.assertEquals("ERROR in filling #" + cnt + " :", 18.2, retArray[i], 0);
		}
		if (cnt == 5) {
		    Assert.assertEquals("ERROR in filling #" + cnt + " :", 21.2, retArray[i], 0);
		}
		cnt++;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("ERROR");
	}
    }

    @Test
    public void testFillingsamplesOverDay4Bug() {
	// 2019-01-18,23:46:13,1,T,20.3
	// 2019-01-19,00:02:25,1,T,20.2
	PowerMockito.mockStatic(Calendar.class);
	PowerMockito.when(Calendar.getInstance()).thenReturn(calendar);
	when(calendar.get(Calendar.MINUTE)).thenReturn(46);
	when(calendar.get(Calendar.HOUR)).thenReturn(11);
	when(calendar.get(Calendar.AM_PM)).thenReturn(1);
	tempLogger.logTemp(20.3);

	when(calendar.get(Calendar.MINUTE)).thenReturn(2);
	when(calendar.get(Calendar.HOUR)).thenReturn(0);
	when(calendar.get(Calendar.AM_PM)).thenReturn(0);
	tempLogger.logTemp(20.1);

	try {
	    double[] retArray = generateRetArray(tempLogger.getTempDayCompressed());
	    // printArray(retArray);
	    int cnt = 0;
	    for (int i = 0; i < NUM_OF_TEMP_SAMPLES; i++) {
		if ((cnt >= 285)) { // ((11+12)*60 + 46)/5
		    Assert.assertEquals("ERROR in filling #" + cnt + " :", 20.2, retArray[i], 0);
		}
		if (cnt == 0) {
		    Assert.assertEquals("ERROR in filling #" + cnt + " :", 20.0, retArray[i], 0);
		}
		cnt++;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("ERROR");
	}
    }

    @Test
    public void testNoFillWhenTimeGoesBack1Hour() {
	PowerMockito.mockStatic(Calendar.class);
	PowerMockito.when(Calendar.getInstance()).thenReturn(calendar);
	when(calendar.get(Calendar.MINUTE)).thenReturn(0);
	when(calendar.get(Calendar.HOUR)).thenReturn(0);
	when(calendar.get(Calendar.AM_PM)).thenReturn(0);
	tempLogger.logTemp(5.5);

	when(calendar.get(Calendar.MINUTE)).thenReturn(0);
	when(calendar.get(Calendar.HOUR)).thenReturn(1);
	when(calendar.get(Calendar.AM_PM)).thenReturn(0);
	tempLogger.logTemp(8.8);

	when(calendar.get(Calendar.MINUTE)).thenReturn(10);
	when(calendar.get(Calendar.HOUR)).thenReturn(1);
	when(calendar.get(Calendar.AM_PM)).thenReturn(0);
	tempLogger.logTemp(9.9);

	when(calendar.get(Calendar.MINUTE)).thenReturn(5);
	when(calendar.get(Calendar.HOUR)).thenReturn(0);
	when(calendar.get(Calendar.AM_PM)).thenReturn(0);
	tempLogger.logTemp(7.7);

	try {
	    double[] retArray = generateRetArray(tempLogger.getTempDayCompressed());
	    // printArray(retArray);
	    int cnt = 0;
	    for (int i = 0; i < NUM_OF_TEMP_SAMPLES; i++) {
		if (cnt == 1) {
		    Assert.assertEquals("ERROR in filling #" + cnt + " :", 7.6, retArray[i], 0);
		}
		if ((cnt < 12) && (cnt != 1)) {
		    Assert.assertEquals("ERROR in filling #" + cnt + " :", 5.4, retArray[i], 0);
		}
		if ((cnt == 12) || (cnt == 13)) {
		    Assert.assertEquals("ERROR in filling #" + cnt + " :", 8.8, retArray[i], 0);
		}
		if (cnt == 14) {
		    Assert.assertEquals("ERROR in filling #" + cnt + " :", 9.8, retArray[i], 0);
		}
		if (cnt > 14) {
		    break;
		}
		cnt++;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("ERROR");
	}
    }

    @Test
    public void testLogMethods() {
	PowerMockito.mockStatic(TempLogger.class);
	Field field = PowerMockito.field(TempLogger.class, "tempLog");
	try {
	    field.set(TempLogger.class, logger);
	} catch (IllegalArgumentException e1) {
	    e1.printStackTrace();
	} catch (IllegalAccessException e1) {
	    e1.printStackTrace();
	}
	tempLogger.logWarming(0);
	verify(logger).info("W,{}", 0);
	tempLogger.logWarming(1);
	verify(logger).info("W,{}", 1);
	tempLogger.logTemp(2.5);
	verify(logger).info("T,{}", 2.5);
	tempLogger.logReqTemp(0, 25.3);
	verify(logger).info("P,{}", 25.3);
	tempLogger.logReqTemp(1, 19.9);
	verify(logger).info("M,{}", 19.9);
    }
}
