package com.rest.test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;

import com.rest.hal9000.AlarmLogger;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AlarmLogger.class, Logger.class })
public class AlarmLoggerTest {

    @Mock
    private Logger logger;

    @InjectMocks
    private final AlarmLogger alarmLogger = new AlarmLogger();

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
    }

    public void testLogMethods() {
	PowerMockito.mockStatic(AlarmLogger.class);
	Field field = PowerMockito.field(AlarmLogger.class, "alarmLog");
	try {
	    field.set(AlarmLogger.class, logger);
	} catch (IllegalArgumentException e1) {
	    e1.printStackTrace();
	} catch (IllegalAccessException e1) {
	    e1.printStackTrace();
	}
	alarmLogger.logKeyProgramming(false, 1);
	alarmLogger.logAlarm(false, 1);
	alarmLogger.logArmed(false, 1);
	verify(logger, never()).info(anyString());
	alarmLogger.logKeyRead("1234");
	verify(logger).info("KEY,{}", "1234");
	alarmLogger.logPinRead("4567");
	verify(logger).info("PIN,{}", "4567");
	alarmLogger.logNewPin("9876");
	verify(logger).info("NEWPIN,{}", "9876");
	alarmLogger.logKeyProgramming(true, 1);
	verify(logger).info("PROG,{}", 1);
	alarmLogger.logAlarm(true, 1);
	verify(logger).info("ALARM,{}", 1);
	alarmLogger.logArmed(true, 1);
	verify(logger).info("ARMED,{}", 1);
    }

}
