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

    private double roundTemp(final String val) {
	Integer intVal = Integer.parseInt(val);
	if (intVal >= 1000) {
	    intVal -= 1000;
	}
	return intVal / 10.0;
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
    protected String getExposedAttribute(final String attr) throws Exception {
	log.info("Thermo exposeAttribute");
	if ("warming".equals(attr)) {
	    return Integer.toString(expAttr.warming);
	}
	wrongAttribute();
	return null;
    }

    @Override
    protected boolean specializedParseGetAnswer(final char attribute, final String msg) {
	switch (attribute) {
	case 'W':
	    expAttr.warming = "0".equals(msg) ? 0 : 1;
	    break;
	case 'T': {
	    String[] list = msg.split(";");
	    expAttr.temperature = roundTemp(list[0]);
	    expAttr.humidity = Integer.parseInt(list[1]);
	}
	    break;
	case 'R':
	    expAttr.required = roundTemp(msg);
	    expAttr.manuallyForced = (Integer.parseInt(msg) > 1000) ? 1 : 0;
	    break;
	case 'H':
	    expAttr.hysteresis = roundTemp(msg);
	    break;
	default:
	    wrongAttribute();
	    return false;
	}
	return true;
    }

    @Override
    protected boolean specializedParseEvent(final char event, final String msg) {
	// Format of message:
	// type(temp,warm,required,error),value
	switch (event) {
	case 'W':
	    expAttr.warming = "0".equals(msg) ? 0 : 1;
	    tempLoggerCallBack.accept("W," + expAttr.warming);
	    break;
	case 'T':
	    expAttr.temperature = roundTemp(msg);
	    tempLoggerCallBack.accept("T," + Double.toString(expAttr.temperature));
	    break;
	case 'R':
	    expAttr.required = roundTemp(msg);
	    expAttr.manuallyForced = (Integer.parseInt(msg) > 1000) ? 1 : 0;
	    tempLoggerCallBack
		    .accept(((expAttr.manuallyForced == 1) ? "M," : "P,") + Double.toString(expAttr.required));
	    break;
	case 'E':
	    log.error("Sensor [{}] reading error (at least 1 min)", msg);
	    return false;
	default:
	    wrongEvent();
	    return false;
	}
	return true;
    }

    @Override
    public void alignAll() {
	log.info("Thermo align all");
	sendMsgToHal("GTT");
	sendMsgToHal("GTW");
	sendMsgToHal("GTR");
	sendMsgToHal("GTH");
    }

    private Response setRequiredTemp(final int temp) {
	if (temp >= 0) {
	    sendMsgToHal("STR" + temp);
	    return Response.status(Response.Status.OK).build();
	} else {
	    return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
	}
    }

    private Response setRequiredHysteresis(final int hyst) {
	if (hyst >= 0) {
	    sendMsgToHal("STH" + hyst);
	    return Response.status(Response.Status.OK).build();
	} else {
	    return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
	}
    }

    @Override
    public Response executeSet(final String attr, String val) throws Exception {
	if ("".equals(val)) {
	    val = "-1";
	}
	try {
	    Integer.parseInt(val);
	} catch (Exception e) {
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
