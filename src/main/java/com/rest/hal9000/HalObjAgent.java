package com.rest.hal9000;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HalObjAgent {

    private static final Logger log = LoggerFactory.getLogger(HalObjAgent.class);

    private final char id;
    private final Consumer<String> sendMsgCallBack;

    public HalObjAgent(char id, Consumer<String> sendMsgCallBack) {
	super();
	this.id = id;
	this.sendMsgCallBack = sendMsgCallBack;
    }

    public char getId() {
	return id;
    }

    protected void wrongAttribute() {
	log.error("Wrong attribute");
    }

    protected void wrongEvent() {
	log.error("Wrong event");
    }

    protected void sendMsgToObj(String msg) {
	this.sendMsgCallBack.accept(msg);
    }

    public abstract void parseGetAnswer(char attribute, String msg);

    public abstract void parseEvent(char event, String msg);
}
