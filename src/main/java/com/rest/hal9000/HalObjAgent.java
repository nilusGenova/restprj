package com.rest.hal9000;

import java.util.function.Consumer;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HalObjAgent {

    protected static final Logger log = LoggerFactory.getLogger(HalObjAgent.class);

    private final char id; // id used on hal9000 MUST be the first letter of pathName

    private final String pathName; // name used to identify obj in the REST call

    private final Consumer<String> sendMsgCallBack;

    public HalObjAgent(String pathName, Consumer<String> sendMsgCallBack) {
	super();
	this.id = Character.toUpperCase(pathName.charAt(0));
	this.pathName = pathName;
	this.sendMsgCallBack = sendMsgCallBack;
    }

    public char getId() {
	return id;
    }

    public String getPathName() {
	return pathName;
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

    public Response exposeJsonData() throws Exception {
	String jsonInString = exposeData();
	log.debug("exposed json:" + jsonInString);
	return Response.ok(jsonInString, MediaType.APPLICATION_JSON).build();
    }

    public abstract String exposeData() throws Exception;

    public Response executeSet(String attr, String val) throws Exception {
	throw new Exception();
    }

    public Response deleteData(String cmd, String prm) throws Exception {
	throw new Exception();
    }

    public Response createData(String cmd, String prm) throws Exception {
	throw new Exception();
    }
}
