package com.rest.hal9000;

import java.util.function.Consumer;

import javax.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClockObjAgent extends HalObjAgent {

    private EpocTime expAttr = new EpocTime();

    public ClockObjAgent(char id, Consumer<String> sendMsgCallBack) {
	super(id, sendMsgCallBack);
	// TODO Auto-generated constructor stub
    }

    public void setActualTime() {
	log.debug("Sending actual time to hal9000");
	String val = expAttr.getEpocOfActualTime();
	sendMsgToHal("SCE" + val);
    }

    @Override
    public void parseGetAnswer(char attribute, String msg) {
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
    public void parseEvent(char event, String msg) {
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

    @Override
    public String exposeData() throws Exception {
	log.info("Clock exposeData");
	ObjectMapper mapper = new ObjectMapper();
	mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
	mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

	return mapper.writeValueAsString(expAttr);
    }

    @Override
    public Response executeCmd(String cmd, String prm) {
	log.info("Setting actual time");
	setActualTime();
	return Response.status(Response.Status.OK).build();
    }

}
