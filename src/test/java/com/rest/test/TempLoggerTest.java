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
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rest.hal9000.TempLogger;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ TempLogger.class, Logger.class, Calendar.class })
public class TempLoggerTest {

    private final static int NUM_OF_TEMP_SAMPLES = 288; // so every 5 minutes
    private final static int NUM_OF_MINS_FOR_SAMPLE = 5;

    @Mock
    private Logger logger;

    @Mock
    private Calendar calendar;

    @InjectMocks
    private final TempLogger tempLogger = new TempLogger("logger");

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
    }

    private int[] generateRetArray(JsonNode rootNode) {
	int[] retArray = new int[NUM_OF_TEMP_SAMPLES];
	int idx = 0;
	int value = 0;
	int count;
	int nodeCnt = 0;
	for (JsonNode val : rootNode) {
	    if (nodeCnt % 2 == 0) {
		value = val.asInt();
	    } else {
		count = val.asInt();
		for (int j = 0; j < count; j++) {
		    retArray[idx++] = value;
		}
	    }
	    nodeCnt++;
	}
	return retArray;
    }

    @Test
    public void testSampling() {
	PowerMockito.mockStatic(Calendar.class);
	PowerMockito.when(Calendar.getInstance()).thenReturn(calendar);
	when(calendar.get(Calendar.MINUTE)).thenReturn(5);
	when(calendar.get(Calendar.HOUR)).thenReturn(2);
	when(calendar.get(Calendar.AM_PM)).thenReturn(0);

	tempLogger.logTemp(10.1);
	tempLogger.logTemp(12.1);

	for (int idx = 0; idx < NUM_OF_TEMP_SAMPLES; idx++) {
	    when(calendar.get(Calendar.HOUR)).thenReturn((idx * NUM_OF_MINS_FOR_SAMPLE / 60) % 12);
	    when(calendar.get(Calendar.AM_PM)).thenReturn((idx * NUM_OF_MINS_FOR_SAMPLE / 60) < 12 ? 0 : 1);
	    int min = (idx * NUM_OF_MINS_FOR_SAMPLE) % 60;
	    when(calendar.get(Calendar.MINUTE)).thenReturn(min); // unuseful here

	    for (int m = 0; m < NUM_OF_MINS_FOR_SAMPLE - 1; m++) {
		when(calendar.get(Calendar.MINUTE)).thenReturn(min + m);
		tempLogger.logTemp(2.4 + (idx + 0.0) / 10);
	    }
	    // good run;
	    tempLogger.logTemp(2.2 + (idx + 0.0) / 10);
	    tempLogger.logTemp(2.5 + (idx + 0.0) / 10); // this is the good one
	}

	try {
	    ObjectMapper mapper = new ObjectMapper();
	    JsonNode rootNode = mapper.readTree((String) tempLogger.exposeJsonData().getEntity());
	    // System.out.println(rootNode);
	    int[] retArray = generateRetArray(rootNode);
	    int cnt = 0;
	    for (int i = 0; i < NUM_OF_TEMP_SAMPLES; i++) {
		Assert.assertEquals("ERROR in samples #" + cnt + " :", Math.round(2.5 + (cnt + 0.0) / 10), retArray[i]);
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
	    ObjectMapper mapper = new ObjectMapper();
	    JsonNode rootNode = mapper.readTree((String) tempLogger.exposeJsonData().getEntity());
	    // System.out.println(rootNode);
	    int[] retArray = generateRetArray(rootNode);
	    int cnt = 0;
	    for (int i = 0; i < NUM_OF_TEMP_SAMPLES; i++) {
		if (cnt < 13) { // 1houre+7min = 12+1 samples
		    Assert.assertEquals("ERROR in filling #" + cnt + " :", 12, retArray[i]);
		}
		if (cnt == 13) {
		    Assert.assertEquals("ERROR in filling #" + cnt + " :", 16, retArray[i]);
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
	    ObjectMapper mapper = new ObjectMapper();
	    JsonNode rootNode = mapper.readTree((String) tempLogger.exposeJsonData().getEntity());
	    // System.out.println(rootNode);
	    int[] retArray = generateRetArray(rootNode);
	    int cnt = 0;
	    for (int i = 0; i < NUM_OF_TEMP_SAMPLES; i++) {
		if ((cnt < 5) || (cnt >= 282)) { // ((11+12)*60 + 30)/5
		    Assert.assertEquals("ERROR in filling #" + cnt + " :", 18, retArray[i]);
		}
		if (cnt == 5) {
		    Assert.assertEquals("ERROR in filling #" + cnt + " :", 21, retArray[i]);
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
	    ObjectMapper mapper = new ObjectMapper();
	    JsonNode rootNode = mapper.readTree((String) tempLogger.exposeJsonData().getEntity());
	    // System.out.println(rootNode);
	    int[] retArray = generateRetArray(rootNode);
	    int cnt = 0;
	    for (int i = 0; i < NUM_OF_TEMP_SAMPLES; i++) {
		if (cnt == 1) {
		    Assert.assertEquals("ERROR in filling #" + cnt + " :", 8, retArray[i]);
		}
		if ((cnt < 12) && (cnt != 1)) {
		    Assert.assertEquals("ERROR in filling #" + cnt + " :", 6, retArray[i]);
		}
		if ((cnt == 12) || (cnt == 13)) {
		    Assert.assertEquals("ERROR in filling #" + cnt + " :", 9, retArray[i]);
		}
		if (cnt == 14) {
		    Assert.assertEquals("ERROR in filling #" + cnt + " :", 10, retArray[i]);
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
	tempLogger.logTemp(3.5);
	verify(logger).info("T,{}", 3.5);
	tempLogger.logReqTemp(0, 25.3);
	verify(logger).info("P,{}", 25.3);
	tempLogger.logReqTemp(1, 19.9);
	verify(logger).info("M,{}", 19.9);
    }
}
