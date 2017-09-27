package com.rest.hal9000;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.List;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Parser implements Runnable {

    private final static BlockingQueue<String> msgQueue = new LinkedBlockingQueue<>(5);
    private final static List<HalObjAgent> listRegistered = Collections.synchronizedList(new ArrayList<HalObjAgent>());

    private static final Logger log = LoggerFactory.getLogger(Parser.class);

    public Parser() {
    }

    public void msgToBeParsed(String msg) {
	try {
	    msgQueue.put(msg);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
    }

    private HalObjAgent getRegisteredObj(char objId) {
    	synchronized(listRegistered) {
    		for (HalObjAgent obj : listRegistered) {
    			if (obj.getId() == objId) {
    				return obj;
    			}
	        }
    	}
	    return null;
    }

    public int numOfRegisteredObj() {
    	synchronized(listRegistered) {
    		return listRegistered.size();
    	}
    }

    public void registerObj(HalObjAgent obj) {
	final char id = obj.getId();
	log.info("Register obj {}", id);
	if (getRegisteredObj(id) != null) {
	    log.error("Object {} already registered", id);
	} else {
		synchronized(listRegistered) {
			listRegistered.add(obj);
		}
	}
	log.debug("Num of registered obj:{}", numOfRegisteredObj());
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
	    case '!': // error
		log.error("Received error:{}", msgToParse);
		break;
	    case 'g': // answer to get
	    case 'E': // Event
		// search for registered obj
		log.debug("Correct msg, search registered obj to dispatch");
		final char objId = msgToParse.charAt(1);
		HalObjAgent obj = getRegisteredObj(objId);
		if (obj != null) {
		    if (invocation == 'g') {
			log.debug("Invoking obj get parser");
			obj.parseGetAnswer(msgToParse);
		    } else {
			log.debug("Invoking obj event parser");
			obj.parseEvent(msgToParse);
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
