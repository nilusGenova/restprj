package com.rest.test;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.rest.hal9000.*;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

@RunWith(PowerMockRunner.class)
@PrepareForTest({TwoWaysSerialComms.class, CommPortIdentifier.class, SerialPort.class})
public class TwoWaysSerialCommsTest {

	@InjectMocks
    private final TwoWaysSerialComms objectUnderTest = new TwoWaysSerialComms();
	
	@Mock
	private InputStream input;
	
	@Mock
	private OutputStream output;
	
	@Mock
	private static CommPortIdentifier portIdentifier;
	
	@Mock
	private static SerialPort serialPort;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);	
	}

	private int simulateAnswer(String reply, InvocationOnMock invocation) {
        Object[] args = invocation.getArguments();
        byte[] buffer = (byte[])args[0];
        int offs = (int)args[1];
        int len = Math.min(reply.length(),(int)args[2]);
        for (int i = 0; i < len; i++){
			buffer[i+offs] = (byte)reply.charAt(i);
		}
        return len;
	}
	
	
	@Test
	public void test() throws Exception {	
		PowerMockito.mockStatic(CommPortIdentifier.class);
		PowerMockito.when(CommPortIdentifier.getPortIdentifier(anyString())).thenReturn(portIdentifier);
		when(portIdentifier.isCurrentlyOwned()).thenReturn(false);
		when(portIdentifier.open(anyString(), anyInt())).thenReturn(serialPort);
		doNothing().when(serialPort).setSerialPortParams(anyInt(), anyInt(), anyInt(), anyInt());
		when(serialPort.getInputStream()).thenReturn(input);
		when(serialPort.getOutputStream()).thenReturn(output);	
		String[] toReply = {"gCE174", "gCD01-01-2017", "gCE300"};
		ArrayList<String> replies = new ArrayList<>(); 
				
		Answer<Integer> answer1 = new Answer<Integer>() {
		    @Override
		    public Integer answer(InvocationOnMock invocation) throws Throwable {
		    	return simulateAnswer("gCE174\ngCD01-01-20", invocation);         
		    }
		};
		
		Answer<Integer> answer2 = new Answer<Integer>() {
		    @Override
		    public Integer answer(InvocationOnMock invocation) throws Throwable {
		    	return simulateAnswer("17\ngCE300\n", invocation);         
		    }
		};
		
		//when(input.read(anyObject(),anyInt(),anyInt())).thenReturn(-1);
		when(input.read(anyObject(),anyInt(),anyInt())).thenAnswer(answer1).thenAnswer(answer2).thenReturn(-1);
		
		objectUnderTest.connect("device" , (str) -> replies.add(str));

		Thread.sleep(5000);
		int i=0;
		for (String reply: replies) {
		    System.out.println(reply);
		    Assert.assertEquals("Wrong answer:", reply, toReply[i++]);
		}
		//fail("Not yet implemented");
	}

}
