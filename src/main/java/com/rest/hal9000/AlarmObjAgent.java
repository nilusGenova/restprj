package com.rest.hal9000;

import java.util.function.Consumer;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class AlarmObjAgent extends HalObjAgent {

    private AlarmObjAttributes expAttr = new AlarmObjAttributes();

    public AlarmObjAgent(String pathName, Consumer<String> sendMsgCallBack) {
	super(pathName, sendMsgCallBack);
    }

    private String calcModeFormat(int armed, int alarmed, int programming) {
	return String.format("%03d", armed * 100 + alarmed * 10 + programming);
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
	    return calcModeFormat(expAttr.getArmed(), expAttr.getAlarmed(), expAttr.getKeyProgramming());
	}
	wrongAttribute();
	return null;
    }

    @Override
    protected void specializedParseGetAnswer(char attribute, String msg) {
	switch (attribute) {
	// M mode = (Armed:[0-1])(Alarm:[0-1])(Prg:[0-1])
	case 'M':
	    int m = Integer.parseInt(msg);
	    if ((m > 111) || (m < 0)) {
		wrongValue(m);
	    } else {
		expAttr.setKeyProgramming(m & 1);
		m /= 10;
		expAttr.setAlarmed(m & 1);
		m /= 10;
		expAttr.setArmed(m & 1);
	    }
	    break;
	// K key = (idxKey 0:master)(value 8 chars);(......)
	case 'K':
	    expAttr.flushKeys();
	    String[] key = msg.split(";");
	    for (String k : key) {
		String[] rec = k.split(":");
		int idx = Integer.parseInt(rec[0]);
		if (idx == 0) {
		    expAttr.setMasterKey(Integer.parseInt(rec[1]));
		} else {
		    expAttr.storeKey(idx - 1, Integer.parseInt(rec[1]));
		}
	    }
	    break;
	// P pin = (idxKey 1:)(value 8 chars);(......)
	case 'P':
	    String[] pin = msg.split(";");
	    for (String p : pin) {
		String[] rec = p.split(":");
		expAttr.storePin(Integer.parseInt(rec[0]) - 1, Integer.parseInt(rec[1]));
	    }
	    break;
	// S sensor values: (green);(red);(alm)
	case 'S':
	    String[] val = msg.split(";");
	    expAttr.setSensGreen(Integer.parseInt(val[0]));
	    expAttr.setSensRed(Integer.parseInt(val[1]));
	    expAttr.setSensAlm(Integer.parseInt(val[2]));
	    expAttr.setValidSensValue(1);
	    break;
	default:
	    wrongAttribute();
	}
    }

    @Override
    protected void specializedParseEvent(char event, String msg) {
	switch (event) {
	// M Mode (Armed:[0-1])(Alarm:[0-1])(Prg:[0-1])
	case 'M':
	    int m = Integer.parseInt(msg);
	    if ((m > 111) || (m < 0)) {
		wrongValue(m);
	    } else {
		expAttr.setKeyProgramming(m & 1);
		m /= 10;
		expAttr.setAlarmed(m & 1);
		m /= 10;
		expAttr.setArmed(m & 1);
	    }
	    break;
	// K Key changed
	// N New Pin set (value 8 chars)
	case 'N':
	    log.info("New PIN: {}", msg);
	case 'K':
	    sendMsgToHal("GAK");
	    sendMsgToHal("GAP");
	    break;
	// R Key read (value 8 chars)
	// P Pin read (value 8 chars)
	case 'R':
	    log.info("ALARM: read key {}", msg);
	    break;
	case 'P':
	    log.info("ALARM: read pin {}", msg);
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
	sendMsgToHal("GAP");
    }

    @Override
    public Response executeSet(String attr, String val) throws Exception {
	int n;
	switch (attr) {
	// need to force sensor read to have valid values
	case "readsensors":
	    expAttr.setValidSensValue(0);
	    sendMsgToHal("GAS");
	    break;
	// R Remote ctrl [0:verde 1:rosso]
	case "remote":
	    log.info("Forcing remote controller (0:green 1:red) :{}", val);
	    sendMsgToHal("SAR" + getBooleanVal(val));
	    break;
	// M Mode (Armed:[0-1])(Alarm:[0-1])(Prg:[0-1])
	case "armed":
	    log.info("Forcing mode armed:{}", val);
	    sendMsgToHal("SAM" + calcModeFormat(getBooleanVal(val), expAttr.getAlarmed(), expAttr.getKeyProgramming()));
	    break;
	case "alarm":
	    log.info("Forcing mode alarmed:{}", val);
	    sendMsgToHal("SAM" + calcModeFormat(expAttr.getArmed(), getBooleanVal(val), expAttr.getKeyProgramming()));
	    break;
	case "program":
	    log.info("Forcing mode keyProgram:{}", val);
	    sendMsgToHal("SAM" + calcModeFormat(expAttr.getArmed(), expAttr.getAlarmed(), getBooleanVal(val)));
	    break;
	// X Master Key value
	case "masterkey":
	    if ("".equals(val)) {
		val = "-1";
	    }
	    n = Integer.parseInt(val);
	    if (n <= 0) {
		wrongValue(n);
		return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
	    } else {
		log.info("Setting master key :{}", val);
		sendMsgToHal("SAX" + val);
	    }
	    break;
	// P Pin of key idxKey:value to add(to be entered on keyboard)
	case "enterpin":
	    if ("".equals(val)) {
		val = "-1";
	    }
	    n = Integer.parseInt(val);
	    if ((n < 0) || (n >= 10)) {
		wrongValue(n);
		return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
	    } else {
		log.info("Setting pin for key #{}", n);
		sendMsgToHal("SAP" + val);
		return Response.ok("Enter PIN from Keyboard", MediaType.TEXT_PLAIN).build();
	    }
	    // K Key value to add
	case "newkey":
	    if ("".equals(val)) {
		val = "-1";
	    }
	    n = Integer.parseInt(val);
	    if (n <= 0) {
		wrongValue(n);
		return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
	    } else {
		log.info("Setting key :{}", val);
		sendMsgToHal("SAK" + val);
	    }
	    break;
	default:
	    throw new Exception();
	}
	return Response.status(Response.Status.OK).build();
    }

    // Attr Reset
    @Override
    public Response deleteData(String cmd, String prm) throws Exception {
	switch (cmd) {
	// K Key value to delete
	case "key":
	    if ("".equals(prm)) {
		prm = "-1";
	    }
	    int m = Integer.parseInt(prm);
	    if (m <= 0) {
		wrongValue(m);
		break;
	    } else {
		if (expAttr.keyExists(m)) {
		    log.info("Deleting key :{}", prm);
		    sendMsgToHal("RAK" + prm);
		    return Response.status(Response.Status.OK).build();
		} else {
		    log.error("Key {} doesn't exists", prm);
		    wrongValue(m);
		}
	    }
	    break;
	// X Master Key cancella tutte le chiavi
	case "allkeys":
	    log.info("Deleting all keys");
	    sendMsgToHal("RAX");
	    return Response.status(Response.Status.OK).build();
	// P Pin of key cancella tutti I PIN
	case "allpins":
	    log.info("Deleting all pins");
	    sendMsgToHal("RAP");
	    return Response.status(Response.Status.OK).build();
	default:
	    throw new Exception();
	}
	return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
    }

}
