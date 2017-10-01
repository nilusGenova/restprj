package com.rest.hal9000;

import java.io.IOException;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClockObjAgent extends HalObjAgent {

    private EpocTime exposedAttributes = new EpocTime();

    public ClockObjAgent(char id, Consumer<String> sendMsgCallBack) {
	super(id, sendMsgCallBack);
	// TODO Auto-generated constructor stub
    }

    public void setActualTime() {
	log.debug("Sending actual time to hal9000");
	String val = exposedAttributes.getEpocOfActualTime();
	sendMsgToObj("SCE" + val);
    }

    @Override
    public void parseGetAnswer(char attribute, String msg) {
	switch (attribute) {
	case 'D':
	    exposedAttributes.setDate(msg);
	    break;
	case 'T':
	    exposedAttributes.setTime(msg);
	    break;
	case 'W':
	    exposedAttributes.setWeekDay(Integer.parseInt(msg));
	    break;
	case 'E':
	    exposedAttributes.setEpocTime(Long.parseLong(msg));
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
	sendMsgToObj("GCE");
    }

    @Override
    public String exposeData() {
	log.info("Clock exposeData");
	ObjectMapper mapper = new ObjectMapper();
	mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
	mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

	try {
	    // Convert object to JSON string
	    return mapper.writeValueAsString(exposedAttributes);
	} catch (JsonGenerationException e) {
	    e.printStackTrace();
	} catch (JsonMappingException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}

	return "";
    }

    @Override
    public CmdResult executeCmd(String cmd) {
	log.info("Setting actual time");
	setActualTime();
	return CmdResult.OK;
    }

}
