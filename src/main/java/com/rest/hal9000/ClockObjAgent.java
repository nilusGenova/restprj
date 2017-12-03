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
    protected boolean specializedParseGetAnswer(final char attribute, final String msg) {
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
	    return false;
	}
	return true;
    }

    @Override
    protected boolean specializedParseEvent(final char event, final String msg) {
	switch (event) {
	case 'D':
	    alignAll();
	    break;
	default:
	    wrongEvent();
	}
	return false;
    }

    @Override
    public void alignAll() {
	log.info("Clock align all");
	sendMsgToHal("GCE");
    }

    // Format of msg
    // v=<hour>:<min>-<day>-<month>-<year>
    private String epocFromValidatedFormatTime(final String val) {
	String[] vals = val.split("-");
	if ((vals.length != 4) || (val.chars().filter(ch -> ch == ':').count() != 1)) {
	    return null;
	}
	if (!vals[0].contains(":")) {
	    return null;
	}
	int h;
	int m;
	int d;
	int mo;
	int y;
	try {
	    h = Integer.parseInt(vals[0].split(":")[0]);
	    m = Integer.parseInt(vals[0].split(":")[1]);
	    d = Integer.parseInt(vals[1]);
	    mo = Integer.parseInt(vals[2]);
	    y = Integer.parseInt(vals[3]);
	} catch (NumberFormatException e) {
	    return null;
	}
	return expAttr.getEpocTime(d, mo, y, h, m, 0);
    }

    @Override
    public Response executeSet(final String attr, final String val) {
	String eat;
	switch (attr) {
	case "actualtime":
	    log.debug("Sending actual time to hal9000");
	    eat = expAttr.getEpocOfActualTime();
	    sendMsgToHal("SCE" + eat);
	    break;
	case "time":
	    log.debug("Sending time to hal9000");
	    eat = epocFromValidatedFormatTime(val);
	    if (eat == null) {
		return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
	    }
	    sendMsgToHal("SCE" + eat);
	    break;
	default:
	    throw new NoSuchElementException();
	}
	return Response.status(Response.Status.OK).build();
    }
}
