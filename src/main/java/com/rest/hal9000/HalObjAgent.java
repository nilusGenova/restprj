package com.rest.hal9000;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HalObjAgent {

    private static final Logger log = LoggerFactory.getLogger(HalObjAgent.class);

    private final char id;

    public HalObjAgent(char id) {
	super();
	this.id = id;
    }

    public char getId() {
	return id;
    }

    public void parseGetAnswer(String msg) {
	log.info("Parsing Get answer:", msg);
	// TODO:
    }

    public void parseEvent(String msg) {
	log.info("Parsing Event:", msg);
	// TODO:
    }
}
