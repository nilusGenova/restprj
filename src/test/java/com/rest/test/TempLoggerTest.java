package com.rest.test;

import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

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

import com.rest.hal9000.TempLogger;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ TempLogger.class, Logger.class })
public class TempLoggerTest {

    @Mock
    private Logger logger;

    @InjectMocks
    private final TempLogger tempLogger = new TempLogger();

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
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
