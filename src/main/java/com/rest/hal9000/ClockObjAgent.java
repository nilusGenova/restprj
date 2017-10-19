package com.rest.hal9000;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

import javax.ws.rs.core.Response;

public class ClockObjAgent extends HalObjAgent {

    private EpocTime expAttr = new EpocTime();

    public ClockObjAgent(final String pathName, final Consumer<String> sendMsgCallBack) {
	super(pathName, sendMsgCallBack);
    }

    @Override
    protected Object getExposedData() {
	log.info("Clock exposeData");
	return expAttr;
    }

    @Override
    protected String getExposedAttribute(final String attr) throws Exception {
	log.info("Clock exposeAttribute");
	if ("epochtime".equals(attr)) {
	    return expAttr.getEpochTime();
	}
	wrongAttribute();
	return null;
    }

    @Override
    protected void specializedParseGetAnswer(final char attribute, final String msg) {
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
    protected void specializedParseEvent(final char event, final String msg) {
	switch (event) {
	case 'D':
	    alignAll();
	    break;
	default:
	    wrongEvent();
	}
    }

    @Override
    public void alignAll() {
	log.info("Clock align all");
	sendMsgToHal("GCE");
    }

    @Override
    public Response executeSet(final String attr, final String val) {
	if ("actualtime".equals(attr)) {
	    log.debug("Sending actual time to hal9000");
	    String eat = expAttr.getEpocOfActualTime();
	    sendMsgToHal("SCE" + eat);
	    return Response.status(Response.Status.OK).build();
	}
	throw new NoSuchElementException();
    }
}
