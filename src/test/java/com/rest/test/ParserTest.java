package com.rest.test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.rest.hal9000.HalObjAgent;
import com.rest.hal9000.Parser;

//@RunWith(PowerMockRunner.class)
@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({ Parser.class, HalObjAgent.class })
public class ParserTest {

    @InjectMocks
    private final Parser parser = new Parser();

    @Mock
    private HalObjAgent objA;

    @Mock
    private HalObjAgent objB;

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
	
    }

    private void delay() {
    	try {
    		Thread.sleep(500);
    	} catch (InterruptedException e) {
    		e.printStackTrace();
    	}
    }
    
    @Test
    public void checkIfMessagesAreRecognized() {
    	when(objA.getId()).thenReturn('A');
    	when(objB.getId()).thenReturn('B');
    	doNothing().when(objA).parseGetAnswer(anyString());
    	doNothing().when(objB).parseGetAnswer(anyString());
    	doNothing().when(objA).parseEvent(anyString());
    	doNothing().when(objB).parseEvent(anyString());

    	parser.registerObj(objA);
    	parser.registerObj(objB);
    	parser.start();
    	
	Assert.assertEquals("Wrong number of registered objects:", parser.numOfRegisteredObj(), 2);

	// Verify Wrong Messages
	parser.msgToBeParsed("GA");
	parser.msgToBeParsed("SB");
	parser.msgToBeParsed("sB");
	delay();

	verify(objA, never()).parseGetAnswer(anyString());
	verify(objB, never()).parseGetAnswer(anyString());
	verify(objB, never()).parseEvent(anyString());
	verify(objA, never()).parseEvent(anyString());

	// Verify Wrong Object
	parser.msgToBeParsed("gC");
	parser.msgToBeParsed("EC");
	delay();

	verify(objA, never()).parseGetAnswer(anyString());
	verify(objB, never()).parseGetAnswer(anyString());
	verify(objB, never()).parseEvent(anyString());
	verify(objA, never()).parseEvent(anyString());

	// Verify Errors
	parser.msgToBeParsed("!A");
	parser.msgToBeParsed("!B");
	delay();

	verify(objA, never()).parseGetAnswer(anyString());
	verify(objB, never()).parseGetAnswer(anyString());
	verify(objB, never()).parseEvent(anyString());
	verify(objA, never()).parseEvent(anyString());

	// Verify Comments
	parser.msgToBeParsed(";hello");
	delay();

	verify(objA, never()).parseGetAnswer(anyString());
	verify(objB, never()).parseGetAnswer(anyString());
	verify(objB, never()).parseEvent(anyString());
	verify(objA, never()).parseEvent(anyString());

	// Verify Correct messages
	parser.msgToBeParsed("gA");
	parser.msgToBeParsed("gB");
	parser.msgToBeParsed("EB");
	parser.msgToBeParsed("EA");
	delay();
	
	verify(objA).parseGetAnswer("gA");
	verify(objB).parseGetAnswer("gB");
	verify(objB).parseEvent("EB");
	verify(objA).parseEvent("EA");
    }
}
