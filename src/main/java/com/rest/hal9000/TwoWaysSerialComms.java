package com.rest.hal9000;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier;
import gnu.io.CommPort;
import gnu.io.SerialPort;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class TwoWaysSerialComms {
	
	  SerialReader reader = null;
	  SerialWriter writer = null;
      private final static BlockingQueue<String> outMsgQueue = new LinkedBlockingQueue<>(5);

	void connect( String portName, Consumer<String> rcvCallBack ) throws Exception {
	    CommPortIdentifier portIdentifier = CommPortIdentifier
	        .getPortIdentifier( portName );
	    if( portIdentifier.isCurrentlyOwned() ) {
	      System.out.println( "Error: Port is currently in use" );
	    } else {
	      int timeout = 2000;
	      CommPort commPort = portIdentifier.open( this.getClass().getName(), timeout );

	      if( commPort instanceof SerialPort ) {
	        SerialPort serialPort = ( SerialPort )commPort;
	        serialPort.setSerialPortParams( 115200,
	                                        SerialPort.DATABITS_8,
	                                        SerialPort.STOPBITS_1,
	                                        SerialPort.PARITY_NONE );
   	        	 
	        // This delay is required because Arduino restarts after connection
	        Thread.sleep(2000);
	        	        
	        InputStream in = serialPort.getInputStream();
	        OutputStream out = serialPort.getOutputStream();
	        
	        reader = new SerialReader( in , rcvCallBack);
	        writer = new SerialWriter( out );

	        ( new Thread( reader ) ).start();
	        ( new Thread( writer ) ).start();

	      } else {
	        System.out.println( "Error: Only serial ports are handled by this example." );
	      }
	    }
	  }
	  
	  void sendMsg(String msg) {
		  try {
				outMsgQueue.put(msg);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	  }

	  private static class SerialReader implements Runnable {

	    InputStream in;
	    Consumer<String> rcvCallBack;

	    public SerialReader( InputStream in , Consumer<String> rcvCallBack) {
	      this.in = in;
	      this.rcvCallBack = rcvCallBack;
	    }
	    
	    public void run() {
	      byte[] buffer = new byte[ 1024 ];
	      int len = -1;
	      try {
	        while( ( len = this.in.read( buffer ) ) > -1 ) {
	        	rcvCallBack.accept(new String( buffer, 0, len ));
	        }
	      } catch( IOException e ) {
	        e.printStackTrace();
	      }
	    }
	  }

	  private static class SerialWriter implements Runnable {

	    OutputStream out;

	    public SerialWriter( OutputStream out ) {
	      this.out = out;
	    }	    

	    public void run() {
		      try {
		        while(true) {
		        	// block until a msg to send arrives
		        	String msgToSend = outMsgQueue.take();
		            this.out.write( msgToSend.getBytes());
		            this.out.write('\n');
		            this.out.flush();		        	
		        }
		      } catch (Exception e) {
		    	  e.printStackTrace();
		      }
		    }
	  }
	}
