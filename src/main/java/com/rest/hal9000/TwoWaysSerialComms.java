package com.rest.hal9000;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

public class TwoWaysSerialComms {

    private static final int RCV_BUFFER_SIZE = 100;
    private static final Logger log = LoggerFactory.getLogger(TwoWaysSerialComms.class);

    SerialReader reader = null;
    SerialWriter writer = null;
    boolean connected = false;
    private final static BlockingQueue<String> outMsgQueue = new LinkedBlockingQueue<>(5);

    public void connect(String portName, Consumer<String> rcvCallBack) throws Exception {
	log.debug("Connecting to: {}", portName);
	CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
	if (portIdentifier.isCurrentlyOwned()) {
	    log.error("Port {} is currently in use", portName);
	} else {
	    int timeout = 2000;
	    CommPort commPort = portIdentifier.open(this.getClass().getName(), timeout);

	    if (commPort instanceof SerialPort) {
		SerialPort serialPort = (SerialPort) commPort;
		serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
			SerialPort.PARITY_NONE);

		// This delay is required because Arduino restarts after connection
		log.debug("Delay to wait for Arduino restart");
		Thread.sleep(2000);

		InputStream in = serialPort.getInputStream();
		OutputStream out = serialPort.getOutputStream();

		reader = new SerialReader(in, rcvCallBack);
		writer = new SerialWriter(out);
		connected = true;

		(new Thread(reader)).start();
		(new Thread(writer)).start();

	    } else {
		log.error("Only serial ports are handled by this example.");
	    }
	}
    }

    void sendMsg(String msg) {
	try {
	    if (connected) {
		outMsgQueue.put(msg);
	    }
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    private static class SerialReader implements Runnable {

	InputStream in;
	Consumer<String> rcvCallBack;

	public SerialReader(InputStream in, Consumer<String> rcvCallBack) {
	    this.in = in;
	    this.rcvCallBack = rcvCallBack;
	}

	public void run() {
	    byte[] buffer = new byte[RCV_BUFFER_SIZE];
	    int len = -1;
	    int idx = 0;
	    log.info("Reader started");
	    try {
		while ((len = this.in.read(buffer, idx, RCV_BUFFER_SIZE - idx)) > -1) {
		    String rcvChars = new String(buffer, 0, idx + len);
		    log.debug("Reader: received <{}>", rcvChars);
		    if (rcvChars.contains("\n")) {
			String[] rcvLines = rcvChars.split("\n");
			int numOfCompletedStrings = rcvLines.length;
			if (buffer[idx + len - 1] != '\n') {
			    // latest string is incomplete so put back in buffer for latest processing
			    log.debug("Reader: put back in buffer for latest processing <{}>",
				    rcvLines[numOfCompletedStrings - 1]);
			    idx = rcvLines[numOfCompletedStrings - 1].length();
			    for (int i = 0; i < idx; i++) {
				buffer[i] = (byte) rcvLines[numOfCompletedStrings - 1].charAt(i);
			    }
			    numOfCompletedStrings--;
			} else {
			    // reset buffer
			    idx = 0;
			}
			// callback for each completed string
			for (int i = 0; i < numOfCompletedStrings; i++) {
			    rcvCallBack.accept(rcvLines[i]);
			}
		    } else {
			idx += len;
			if (idx >= RCV_BUFFER_SIZE - 1) {
			    // delete any received char
			    log.debug("Reader: resetting any received char");
			    idx = 0;
			}
		    }
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

    private static class SerialWriter implements Runnable {

	OutputStream out;

	public SerialWriter(OutputStream out) {
	    this.out = out;
	}

	public void run() {
	    log.info("Writer started");
	    try {
		while (true) {
		    // block until a msg to send arrives
		    String msgToSend = outMsgQueue.take();
		    log.debug("Writer: sending <{}>", msgToSend);
		    this.out.write(msgToSend.getBytes());
		    this.out.write('\n');
		    this.out.flush();
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }
}
