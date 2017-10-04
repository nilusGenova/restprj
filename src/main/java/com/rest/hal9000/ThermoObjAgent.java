package com.rest.hal9000;

import java.util.function.Consumer;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ThermoObjAgent extends HalObjAgent {

    private class ExposedAttributes {
	private int warmSwitchStatus = 0;
	private double temperature = 0;
	private int humidity = 0;
	private double requiredTemp = 0;
	private int manuallyForced = 0;
	private int hysteresis = 0;
    }

    private ExposedAttributes expAttr = new ExposedAttributes();

    public ThermoObjAgent(char id, Consumer<String> sendMsgCallBack) {
	super(id, sendMsgCallBack);
    }

    @Override
    public void parseGetAnswer(char attribute, String msg) {
	switch (attribute) {
	case 'W':
	    expAttr.warmSwitchStatus = "0".equals(msg) ? 0 : 1;
	    break;
	case 'T': {
	    String[] list = msg.split(";");
	    expAttr.temperature = Integer.parseInt(list[0]) / 10;
	    expAttr.humidity = Integer.parseInt(list[1]);
	}
	    break;
	case 'R':
	    expAttr.requiredTemp = Integer.parseInt(msg) / 10;
	    expAttr.manuallyForced = (expAttr.requiredTemp > 1000) ? 1 : 0;
	    if (expAttr.manuallyForced == 1) {
		expAttr.requiredTemp -= 1000;
	    }
	    break;
	case 'H':
	    expAttr.hysteresis = Integer.parseInt(msg);
	    break;
	default:
	    wrongAttribute();
	}
    }

    @Override
    public void parseEvent(char event, String msg) {
	switch (event) {
	case 'W':
	    expAttr.warmSwitchStatus = "0".equals(msg) ? 0 : 1;
	    break;
	case 'T':
	    expAttr.temperature = Integer.parseInt(msg) / 10;
	    break;
	case 'R':
	    expAttr.requiredTemp = Integer.parseInt(msg) / 10;
	    expAttr.manuallyForced = (expAttr.requiredTemp > 1000) ? 1 : 0;
	    if (expAttr.manuallyForced == 1) {
		expAttr.requiredTemp -= 1000;
	    }
	    break;
	case 'E':
	    log.error("Sensor [{}] reading error (at least 1 min)", msg);
	    break;
	default:
	    wrongAttribute();
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

    @Override
    public String exposeData() throws Exception {
	log.info("Thermo exposeData");
	ObjectMapper mapper = new ObjectMapper();
	mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
	mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

	return mapper.writeValueAsString(expAttr);
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
    public Response executeCmd(String cmd, String prm) throws Exception {
	switch (cmd) {
	case "R":
	    log.info("Setting required temp:{}", prm);
	    return setRequiredTemp(Integer.parseInt(prm));
	case "H":
	    log.info("Setting hysteresis:{}", prm);
	    return setRequiredHysteresis(Integer.parseInt(prm));
	default:
	    throw new Exception();
	}
    }
}
