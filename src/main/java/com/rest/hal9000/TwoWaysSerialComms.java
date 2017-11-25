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
    private static final int CONN_RETRY_TIMEOUT = 5000; // in mSec
    private static final Logger log = LoggerFactory.getLogger(TwoWaysSerialComms.class);

    ConnectionManager connectionManager = null;
    private Boolean connected = false;
    private final static BlockingQueue<String> outMsgQueue = new LinkedBlockingQueue<>(5);
    String portName = null;
    Consumer<String> rcvCallBack;

    public boolean isConnected() {
	synchronized (connected) {
	    return connected;
	}
    }

    public void connect(final String portName, final Consumer<String> rcvCallBack) throws Exception {
	this.portName = portName;
	this.rcvCallBack = rcvCallBack;
	log.debug("Starting connection manager for: {}", portName);
	connectionManager = new ConnectionManager();
	(new Thread(connectionManager)).start();
    }

    void sendMsg(final String msg) {
	try {
	    synchronized (connected) {
		if (connected) {
		    outMsgQueue.put(msg);
		}
	    }
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    private class ConnectionManager implements Runnable {

	// TODO:
	SerialReader reader = null;
	SerialWriter writer = null;

	public void run() {
	    // TODO:
	    while (true) {
		log.debug("Connecting to: {}", portName);
		try {
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

			    reader = new SerialReader(in);
			    writer = new SerialWriter(out);
			    synchronized (connected) {
				connected = true;
			    }

			    Thread readThread = new Thread(reader);
			    Thread writeThread = new Thread(writer);
			    readThread.start();
			    writeThread.start();

			    // wait for reader stop
			    readThread.join();
			    // then proceed to stop writer too
			    writer.cancel();
			    synchronized (connected) {
				connected = false;
			    }
			    writeThread.join();

			} else {
			    log.error("Only serial ports are handled by this code.");
			}
		    }
		} catch (Exception e) {
		    log.error("Connection lost, retry to connet in {} secs", CONN_RETRY_TIMEOUT / 1000);
		}
		try {
		    Thread.sleep(CONN_RETRY_TIMEOUT);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}
    }

    private class SerialReader implements Runnable {

	InputStream in;

	public SerialReader(final InputStream in) {
	    this.in = in;
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

	public SerialWriter(final OutputStream out) {
	    this.out = out;
	}

	public void cancel() throws Exception {
	    Thread.currentThread().interrupt();
	    outMsgQueue.put(";"); // just to force wake up of the writer
	}

	public void run() {
	    log.info("Writer started");
	    try {
		while (!Thread.currentThread().isInterrupted()) {
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
