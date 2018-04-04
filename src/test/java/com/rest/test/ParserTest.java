package com.rest.test;

import static org.mockito.Matchers.anyChar;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.rest.hal9000.Registry;

//@RunWith(PowerMockRunner.class)
@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({ Parser.class, HalObjAgent.class })
public class ParserTest {

    private Registry registry = new Registry();

    @Mock
    private HalObjAgent objA;

    @Mock
    private HalObjAgent objB;

    @InjectMocks
    private final Parser parser = new Parser((id) -> registry.getRegisteredObj(id));

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
	doNothing().when(objA).parseGetAnswer(anyChar(), anyString());
	doNothing().when(objB).parseGetAnswer(anyChar(), anyString());
	doNothing().when(objA).parseEvent(anyChar(), anyString());
	doNothing().when(objB).parseEvent(anyChar(), anyString());

	registry.registerObj(objA);
	registry.registerObj(objB);
	parser.start();

	// Verify Wrong Messages
	parser.msgToBeParsed("GA");
	parser.msgToBeParsed("SB");
	parser.msgToBeParsed("sB");
	parser.msgToBeParsed("RB");
	delay();

	verify(objA, never()).parseGetAnswer(anyChar(), anyString());
	verify(objB, never()).parseGetAnswer(anyChar(), anyString());
	verify(objB, never()).parseEvent(anyChar(), anyString());
	verify(objA, never()).parseEvent(anyChar(), anyString());

	// Verify Wrong Object
	parser.msgToBeParsed("gC");
	parser.msgToBeParsed("EC");
	delay();

	verify(objA, never()).parseGetAnswer(anyChar(), anyString());
	verify(objB, never()).parseGetAnswer(anyChar(), anyString());
	verify(objB, never()).parseEvent(anyChar(), anyString());
	verify(objA, never()).parseEvent(anyChar(), anyString());

	// Verify Errors
	parser.msgToBeParsed("!A");
	parser.msgToBeParsed("!B");
	delay();

	verify(objA, never()).parseGetAnswer(anyChar(), anyString());
	verify(objB, never()).parseGetAnswer(anyChar(), anyString());
	verify(objB, never()).parseEvent(anyChar(), anyString());
	verify(objA, never()).parseEvent(anyChar(), anyString());

	// Verify Comments
	parser.msgToBeParsed(";hello");
	delay();

	verify(objA, never()).parseGetAnswer(anyChar(), anyString());
	verify(objB, never()).parseGetAnswer(anyChar(), anyString());
	verify(objB, never()).parseEvent(anyChar(), anyString());
	verify(objA, never()).parseEvent(anyChar(), anyString());

	// Verify Acknowledges
	parser.msgToBeParsed("A");
	delay();

	verify(objA, never()).parseGetAnswer(anyChar(), anyString());
	verify(objB, never()).parseGetAnswer(anyChar(), anyString());
	verify(objB, never()).parseEvent(anyChar(), anyString());
	verify(objA, never()).parseEvent(anyChar(), anyString());

	// Verify Correct messages
	parser.msgToBeParsed("gA1parm");
	parser.msgToBeParsed("gB2Z");
	parser.msgToBeParsed("EB3Zza");
	parser.msgToBeParsed("EA1Z");
	delay();

	verify(objA, times(1)).parseGetAnswer('1', "parm");
	verify(objB, times(1)).parseGetAnswer('2', "Z");
	verify(objB, times(1)).parseEvent('3', "Zza");
	verify(objA, times(1)).parseEvent('1', "Z");
    }
}
