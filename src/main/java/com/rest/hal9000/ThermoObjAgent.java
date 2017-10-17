package com.rest.hal9000;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

import javax.ws.rs.core.Response;

public class ThermoObjAgent extends HalObjAgent {

    private class ExposedAttributes {
	private int warming = 0;
	private double temperature = 0;
	private int humidity = 0;
	private double required = 0;
	private int manuallyForced = 0;
	private double hysteresis = 0;
    }

    private ExposedAttributes expAttr = new ExposedAttributes();

    private final Consumer<String> tempLoggerCallBack;

    public ThermoObjAgent(String pathName, Consumer<String> sendMsgCallBack, Consumer<String> tempLoggerCallBack) {
	super(pathName, sendMsgCallBack);
	this.tempLoggerCallBack = tempLoggerCallBack;
    }

    @Override
    protected Object getExposedData() {
	log.info("Thermo exposeData");
	return expAttr;
    }

    @Override
    protected String getExposedAttribute(String attr) throws Exception {
	log.info("Thermo exposeAttribute");
	if ("warming".equals(attr)) {
	    return Integer.toString(expAttr.warming);
	}
	wrongAttribute();
	return null;
    }

    @Override
    protected void specializedParseGetAnswer(char attribute, String msg) {
	switch (attribute) {
	case 'W':
	    expAttr.warming = "0".equals(msg) ? 0 : 1;
	    break;
	case 'T': {
	    String[] list = msg.split(";");
	    expAttr.temperature = Integer.parseInt(list[0]) / 10.0;
	    expAttr.humidity = Integer.parseInt(list[1]);
	}
	    break;
	case 'R':
	    expAttr.required = Integer.parseInt(msg) / 10.0;
	    expAttr.manuallyForced = (expAttr.required > 1000) ? 1 : 0;
	    if (expAttr.manuallyForced == 1) {
		expAttr.required -= 1000;
	    }
	    break;
	case 'H':
	    expAttr.hysteresis = Integer.parseInt(msg) / 10.0;
	    break;
	default:
	    wrongAttribute();
	}
    }

    @Override
    protected void specializedParseEvent(char event, String msg) {
	switch (event) {
	case 'W':
	    expAttr.warming = "0".equals(msg) ? 0 : 1;
	    break;
	case 'T':
	    expAttr.temperature = Integer.parseInt(msg) / 10.0;
	    tempLoggerCallBack.accept(msg);
	    break;
	case 'R':
	    expAttr.required = Integer.parseInt(msg) / 10.0;
	    expAttr.manuallyForced = (expAttr.required > 1000) ? 1 : 0;
	    if (expAttr.manuallyForced == 1) {
		expAttr.required -= 1000;
	    }
	    break;
	case 'E':
	    log.error("Sensor [{}] reading error (at least 1 min)", msg);
	    break;
	default:
	    wrongEvent();
	}
    }

    @Override
    public void alignAll() {
	log.info("Thermo align all");
	sendMsgToHal("GTT");
	sendMsgToHal("GTW");
	sendMsgToHal("GTR");
	sendMsgToHal("GTH");
    }

    private Response setRequiredTemp(int temp) {
	if (temp >= 0) {
	    sendMsgToHal("STR" + temp);
	    return Response.status(Response.Status.OK).build();
	} else {
	    return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
	}
    }

    private Response setRequiredHysteresis(int hyst) {
	if (hyst >= 0) {
	    sendMsgToHal("STH" + hyst);
	    return Response.status(Response.Status.OK).build();
	} else {
	    return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
	}
    }

    @Override
    public Response executeSet(String attr, String val) throws Exception {
	if ("".equals(val)) {
	    val = "-1";
	}
	switch (attr) {
	case "required":
	    log.info("Setting required temp:{}", val);
	    return setRequiredTemp(Integer.parseInt(val));
	case "hysteresis":
	    log.info("Setting hysteresis:{}", val);
	    return setRequiredHysteresis(Integer.parseInt(val));
	default:
	    throw new NoSuchElementException();
	}
    }
}
