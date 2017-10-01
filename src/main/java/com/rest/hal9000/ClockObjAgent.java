package com.rest.hal9000;

import java.util.function.Consumer;

public class ClockObjAgent extends HalObjAgent {

    EpocTime exposedAttributes = new EpocTime();

    public ClockObjAgent(char id, Consumer<String> sendMsgCallBack) {
	super(id, sendMsgCallBack);
	// TODO Auto-generated constructor stub
    }

    public void setActualTime() {
	String val = exposedAttributes.getEpochOfActualTime();
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
	    exposedAttributes.setEpocTime(Integer.parseInt(msg));
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
	sendMsgToObj("GCE");
    }

    @Override
    public String exposeData() {
	return "error from clock";
	// TODO:
    }

    @Override
    public CmdResult executeCmd(String cmd) {
	setActualTime();
	return CmdResult.OK;
    }

}
