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

import com.rest.hal9000.ClockObjAgent;
import com.rest.hal9000.TwoWaysSerialComms;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({ ClockObjAgent.class })
public class ClockObjAgentTest {

    void sendMsg(String msg) {
	System.out.println("MSG SENT:"+msg);
    }
    
    @InjectMocks
    private final ClockObjAgent clock = new ClockObjAgent("clock",(s) -> sendMsg(s));

    
    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testParseGetAnswer() {
	//TODO: D T W E errati
	clock.parseGetAnswer('D', "14-02-1967");
	String exposedVal=null;
	try {
	    exposedVal = clock.exposeJsonData().toString();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	Assert.assertEquals("ERROR:", "14-02-1967", exposedVal);
    }

    @Test
    public void testParseEvent() {
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
}
