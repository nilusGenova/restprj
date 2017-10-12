package com.rest.test;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rest.hal9000.AlarmObjAgent;
import com.rest.hal9000.AlarmObjAttributes;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({ AlarmObjAgent.class })
public class AlarmObjAgentTest {

    String msgSent = "";

    void sendMsg(String msg) {
	msgSent = msg;
	System.out.println("MSG SENT:" + msg);
    }

    @InjectMocks
    private final AlarmObjAgent alarm = new AlarmObjAgent("alarm", (s) -> sendMsg(s));

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
    }
    
    private AlarmObjAttributes extractAttributeValue() {
	try {
	    ObjectMapper mapper = new ObjectMapper();
	    return mapper.readValue((String) alarm.exposeJsonData().getEntity(), AlarmObjAttributes.class);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }

    @Test
    public void testParseGetAnswerMode() {
	// M mode = (Armed:[0-1])(Alarm:[0-1])(Prg:[0-1])
	alarm.parseGetAnswer('M', "001");
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getArmed());
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getAlarmed());
	Assert.assertEquals("ERROR:", 1, extractAttributeValue().getKeyProgramming());
	alarm.parseGetAnswer('M', "1");
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getArmed());
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getAlarmed());
	Assert.assertEquals("ERROR:", 1, extractAttributeValue().getKeyProgramming());
	alarm.parseGetAnswer('M', "010");
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getArmed());
	Assert.assertEquals("ERROR:", 1, extractAttributeValue().getAlarmed());
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getKeyProgramming());
	alarm.parseGetAnswer('M', "10");
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getArmed());
	Assert.assertEquals("ERROR:", 1, extractAttributeValue().getAlarmed());
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getKeyProgramming());
	alarm.parseGetAnswer('M', "100");
	Assert.assertEquals("ERROR:", 1, extractAttributeValue().getArmed());
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getAlarmed());
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getKeyProgramming());
    }
    
    @Test
    public void testParseGetAnswerSensors() {
	// S sensor values: (green);(red);(alm)
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getValidSensValue());
	alarm.parseGetAnswer('S', "15;0;2");
	Assert.assertEquals("ERROR:", 1, extractAttributeValue().getValidSensValue());
	Assert.assertEquals("ERROR:", 15, extractAttributeValue().getSensGreen());
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getSensRed());
	Assert.assertEquals("ERROR:", 2, extractAttributeValue().getSensAlm());
    }
    
    @Test
    public void testParseGetAnswerKeys() {
	// K key = (idxKey 0:master)(value 8 chars);(......)
	alarm.parseGetAnswer('K', "0:07345196;1:91234567;5:00001234");
	Assert.assertEquals("ERROR:", 7345196, extractAttributeValue().getMasterKey());
	Assert.assertFalse("ERROR:", extractAttributeValue().keyExists(42));
	Assert.assertTrue("ERROR:", extractAttributeValue().keyExists(91234567));
	Assert.assertTrue("ERROR:", extractAttributeValue().keyExists(1234));

	fail("Not yet implemented");
    }
    
    @Test
    public void testParseGetAnswerPins() {
	// P pin = (idxKey 1:)(value 8 chars);(......)
	
	fail("Not yet implemented");
	
	alarm.parseGetAnswer('P', "001");
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getArmed());
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getAlarmed());
	Assert.assertEquals("ERROR:", 1, extractAttributeValue().getKeyProgramming());
	fail("Not yet implemented");
    }

    @Test
    public void testParseEvent() {
	// M K N R
	fail("Not yet implemented");
    }

    @Test
    public void testExposeJsonAttribute() {
	fail("Not yet implemented");
    }

    @Test
    public void testExecuteSet() {
	fail("Not yet implemented");
    }

    @Test
    public void testDeleteData() {
	fail("Not yet implemented");
    }

    @Test
    public void testCreateData() {
	fail("Not yet implemented");
    }

}
