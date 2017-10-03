package com.rest.hal9000;

import java.util.function.Consumer;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HalObjAgent {

    protected static final Logger log = LoggerFactory.getLogger(HalObjAgent.class);

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

    protected void wrongValue() {
	log.error("Wrong value");
    }

    protected void sendMsgToHal(String msg) {
	this.sendMsgCallBack.accept(msg);
    }

    public abstract void parseGetAnswer(char attribute, String msg);

    public abstract void parseEvent(char event, String msg);

    public abstract void alignAll();

    public Response exposeJsonData() {
	String jsonInString = exposeData();
	log.debug("exposed json:" + jsonInString);
	return Response.ok(jsonInString, MediaType.APPLICATION_JSON).build();
    }

    public abstract String exposeData();

    public Response executeCmd(String cmd, String prm) {
	return Response.status(Response.Status.BAD_REQUEST).build();
    }

    public Response deleteData(String cmd, String prm) {
	return Response.status(Response.Status.BAD_REQUEST).build();
    }

    public Response createData(String cmd, String prm) {
	return Response.status(Response.Status.BAD_REQUEST).build();
    }
}
