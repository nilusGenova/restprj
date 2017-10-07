package com.rest.hal9000;

import java.util.function.Consumer;

import javax.ws.rs.core.Response;

public class ClockObjAgent extends HalObjAgent {

    private EpocTime expAttr = new EpocTime();

    public ClockObjAgent(String pathName, Consumer<String> sendMsgCallBack) {
	super(pathName, sendMsgCallBack);
    }

    @Override
    protected Object getExposedData() {
	log.info("Clock exposeData");

	return expAttr;
    }

    @Override
    public void specializedParseGetAnswer(char attribute, String msg) {
	switch (attribute) {
	case 'D':
	    expAttr.setDate(msg);
	    break;
	case 'T':
	    expAttr.setTime(msg);
	    break;
	case 'W':
	    expAttr.setWeekDay(Integer.parseInt(msg));
	    break;
	case 'E':
	    expAttr.setEpocTime(Long.parseLong(msg));
	    break;
	default:
	    wrongAttribute();
	}
    }

    @Override
    public void specializedParseEvent(char event, String msg) {
	switch (event) {
	case 'D':
	    alignAll();
	    break;
	default:
	    wrongAttribute();
	}
    }

    @Override
    public void alignAll() {
	log.info("Clock align all");
	sendMsgToHal("GCE");
    }

    public void setActualTime() {
	log.debug("Sending actual time to hal9000");
	String val = expAttr.getEpocOfActualTime();
	sendMsgToHal("SCE" + val);
    }

    @Override
    public Response executeSet(String attr, String val) {
	log.info("Setting actual time");
	setActualTime();
	return Response.status(Response.Status.OK).build();
    }
}
