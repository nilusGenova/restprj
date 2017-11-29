package com.rest.test;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.rest.hal9000.TwoWaysSerialComms;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ TwoWaysSerialComms.class, CommPortIdentifier.class, SerialPort.class })
public class TwoWaysSerialCommsTest {

    // \n is the separator between messages to be considered as received
    // \t is the separator between provided by the mocked read
    static final String MESSAGES = "gCE174\n\tgC\tD01-01-20\t17\ngCE300\t\ngC\tT";

    static final String[] toReceive = MESSAGES.replaceAll("\t", "").split("\n");
    static final String[] toUseInRead = MESSAGES.split("\t");
    int readMsgIdx = 0;

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

    private int simulateRead(InvocationOnMock invocation) {
	Object[] args = invocation.getArguments();
	byte[] buffer = (byte[]) args[0];
	int offs = (int) args[1];
	if (readMsgIdx >= toUseInRead.length) {
	    return -1;
	}
	String reply = toUseInRead[readMsgIdx++];
	int len = Math.min(reply.length(), (int) args[2]);
	for (int i = 0; i < len; i++) {
	    buffer[i + offs] = (byte) reply.charAt(i);
	}
	return len;
    }

    @Test
    public void test() throws Exception {
	PowerMockito.mockStatic(CommPortIdentifier.class);
	PowerMockito.when(CommPortIdentifier.getPortIdentifier(anyString())).thenReturn(portIdentifier);
	Vector<CommPortIdentifier> devs = new Vector<>();
	devs.add(portIdentifier);
	Enumeration<CommPortIdentifier> list;
	list = devs.elements();
	PowerMockito.when(CommPortIdentifier.getPortIdentifiers()).thenReturn(list);
	when(portIdentifier.getName()).thenReturn("/dev/ttyUSB0");
	when(portIdentifier.isCurrentlyOwned()).thenReturn(false);
	when(portIdentifier.open(anyString(), anyInt())).thenReturn(serialPort);
	doNothing().when(serialPort).setSerialPortParams(anyInt(), anyInt(), anyInt(), anyInt());
	when(serialPort.getInputStream()).thenReturn(input);
	when(serialPort.getOutputStream()).thenReturn(output);
	ArrayList<String> replies = new ArrayList<>();
	readMsgIdx = 0;

	Answer<Integer> answer = new Answer<Integer>() {
	    @Override
	    public Integer answer(InvocationOnMock invocation) throws Throwable {
		return simulateRead(invocation);
	    }
	};
	when(input.read(anyObject(), anyInt(), anyInt())).thenAnswer(answer);

	objectUnderTest.startConnectionManager("device", (str) -> replies.add(str));

	while (readMsgIdx < toUseInRead.length)
	    ;
	Thread.sleep(500);

	int msgToReceive = toReceive.length;
	if (!MESSAGES.endsWith("\n")) {
	    msgToReceive--;
	}
	Assert.assertEquals("wrong number of received messages", msgToReceive, replies.size());
	int i = 0;
	for (String reply : replies) {
	    System.out.println(reply);
	    Assert.assertEquals("Wrong answer:", reply, toReceive[i++]);
	}
    }

}
