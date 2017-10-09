package com.rest.hal9000;

import java.util.function.Consumer;

import javax.ws.rs.core.Response;

import javassist.compiler.ast.Expr;

public class AlarmObjAgent extends HalObjAgent {

    private class ExposedAttributes {
	// mode = (Armed:[0-1])(Alarm:[0-1])(Prg:[0-1])
	private int armed = 0;
	private int alarmed = 0;
	private int keyProgramming = 0;
	private String key = ""; // (idxKey 0:master)(value 8 chars);(......)
	private String masterKey = "";
	private String pinOfKey = ""; // (idxKey 1:)(value 8 chars);(......)
	private int sensValue = 0;
	private int validSensValue = 0;
    }

    private ExposedAttributes expAttr = new ExposedAttributes();

    public AlarmObjAgent(String pathName, Consumer<String> sendMsgCallBack) {
	super(pathName, sendMsgCallBack);
    }

    @Override
    protected Object getExposedData() {
	log.info("Alarm exposeData");
	return expAttr;
    }

    @Override
    protected String getExposedAttribute(String attr) throws Exception {
	log.info("Alarm exposeAttribute");
	if ("mode".equals(attr)) {
	    return Integer.toString(expAttr.armed * 100 + expAttr.alarmed * 10 + expAttr.keyProgramming);
	}
	wrongAttribute();
	return null;
    }

    @Override
    protected void specializedParseGetAnswer(char attribute, String msg) {
	// TODO Auto-generated method stub
	switch (attribute) {
	case 'M':
	    expAttr.armed = msg.charAt(0)=='0' ? 0 : 1;
	    expAttr.alarmed = msg.charAt(1)=='0' ? 0 : 1;
	    expAttr.keyProgramming = msg.charAt(2)=='0' ? 0 : 1;
	    break;
	
	default:
	    wrongAttribute();
	}
    }

    @Override
    protected void specializedParseEvent(char event, String msg) {
	// TODO Auto-generated method stub
	//
	// Event Prm
	// M Mode (Armed:[0-1])(Alarm:[0-1])(Prg:[0-1])
	// K Key changed
	// R Key read (value 8 chars)
	// P Pin read (value 8 chars)
	// N New Pin set (value 8 chars)
	switch (event) {
	case 'M':
	    expAttr.armed = msg.charAt(0)=='0' ? 0 : 1;
	    expAttr.alarmed = msg.charAt(1)=='0' ? 0 : 1;
	    expAttr.keyProgramming = msg.charAt(2)=='0' ? 0 : 1;
	    break;
	
	default:
	    wrongEvent();
	}
    }

    @Override
    public void alignAll() {
	log.info("Alarm align all");
	sendMsgToHal("GAM");
	sendMsgToHal("GAK");
	sendMsgToHal("GAX");
	sendMsgToHal("GAP");
    }

    // Attr Set
    // M Mode (Armed:[0-1])(Alarm:[0-1])(Prg:[0-1])
    // R Remote ctrl [0:verde 1:rosso]
    @Override
    public Response executeSet(String attr, String val) throws Exception {
	// TODO:
	switch (attr) {
	// case "required":
	// log.info("Setting required temp:{}", val);
	// return setRequiredTemp(Integer.parseInt(val));

	default:
	    throw new Exception();
	}
    }

    // Attr Reset
    // K Key value to delete
    // X Master Key cancella tutte le chiavi
    // P Pin of key cancella tutti I PIN
    @Override
    public Response deleteData(String cmd, String prm) throws Exception {
	// TODO:
	switch (cmd) {
	// case "required":
	// log.info("Setting required temp:{}", val);
	// return setRequiredTemp(Integer.parseInt(val));

	default:
	    throw new Exception();
	}
    }

    // Attr Set
    // K Key value to add
    // X Master Key value
    // P Pin of key idxKey:value to add(to be entered on keyboard)
    @Override
    public Response createData(String cmd, String prm) throws Exception {
	// TODO:
	switch (cmd) {
	// case "required":
	// log.info("Setting required temp:{}", val);
	// return setRequiredTemp(Integer.parseInt(val));

	default:
	    throw new Exception();
	}
    }

}
