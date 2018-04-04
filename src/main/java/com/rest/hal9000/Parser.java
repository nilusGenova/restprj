package com.rest.hal9000;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Parser implements Runnable {

    private final static BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>(5);
    private final Function<Character, HalObjAgent> getRegisteredObj;

    private static final Logger log = LoggerFactory.getLogger(Parser.class);

    public Parser(final Function<Character, HalObjAgent> getRegisteredObj) {
	this.getRegisteredObj = getRegisteredObj;
    }

    public void msgToBeParsed(final String msg) {
	try {
	    msgQueue.put(msg);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    private void parseMsg() {
	try {
	    // block until a msg to parse arrives
	    String msgToParse = msgQueue.take();
	    log.debug("Parsing <{}>", msgToParse);
	    // check if msg is valid
	    final char invocation = msgToParse.charAt(0);
	    switch (invocation) {
	    case ';': // comment
		log.debug("Received comment");
		break;
	    case 'A': // comment
		log.debug("Received Acknowledge");
		break;
	    case '!': // error
		log.error("Received error:{}", msgToParse);
		break;
	    case 'g': // answer to get
	    case 'E': // Event
		// search for registered obj
		log.debug("Correct msg, search registered obj to dispatch");
		final char objId = msgToParse.charAt(1);
		HalObjAgent obj = getRegisteredObj.apply(objId);
		if (obj != null) {
		    if (invocation == 'g') {
			log.debug("Invoking obj get parser");
			obj.parseGetAnswer(msgToParse.charAt(2), msgToParse.substring(3));
		    } else {
			log.debug("Invoking obj event parser");
			obj.parseEvent(msgToParse.charAt(2), msgToParse.substring(3));
		    }
		} else {
		    log.error("Invalid object:{}", objId);
		}
		break;
	    default:
		log.error("Invalid invokation:{}", invocation);
		break;
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void run() {
	log.info("Parser loop");
	while (true) {
	    parseMsg();
	}
    }

    public void start() {
	(new Thread(this)).start();
    }

}
