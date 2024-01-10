package com.rest.hal9000;

import java.util.function.Consumer;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class AlarmObjAgent extends HalObjAgent {

    private AlarmObjAttributes expAttr = new AlarmObjAttributes();

    private final static AlarmLogger alarmLogger = new AlarmLogger();

    public AlarmObjAgent(final String pathName, final Consumer<String> sendMsgCallBack) {
	super(pathName, sendMsgCallBack);
    }

    private String calcModeFormat(final int armed, final int alarmed, final int programming) {
	return String.format("%03d", armed * 100 + alarmed * 10 + programming);
    }

    @Override
    protected Object getExposedData() {
	log.debug("Alarm exposeData");
	return expAttr;
    }

    @Override
    protected String getExposedAttribute(final String attr) throws Exception {
	log.debug("Alarm exposeAttribute");
	if ("mode".equals(attr)) {
	    return calcModeFormat(expAttr.getArmed(), expAttr.getAlarmed(), expAttr.getKeyProgramming());
	}
	if ("campower".equals(attr)) {
		return Integer.toString(expAttr.getCamPower());
	}
	if ("autocampower".equals(attr)) {
		return Integer.toString(expAttr.getAutoCamPower());
	}
	wrongAttribute(attr);
	return null;
    }

    @Override
    protected boolean specializedParseGetAnswer(final char attribute, final String msg) {
    	int v;
	switch (attribute) {
	// M mode = (Armed:[0-1])(Alarm:[0-1])(Prg:[0-1])
	case 'M':
	    int m;
	    m = Integer.parseInt(msg);
	    if ((m > 111) || (m < 0)) {
		wrongValue("get mode:" + m);
	    } else {
		expAttr.setKeyProgramming(m & 1);
		m /= 10;
		expAttr.setAlarmed(m & 1);
		m /= 10;
		expAttr.setArmed(m & 1);
	    }
	    break;
	// K key = (idxKey 0:master)(value 8 chars).(......)
	case 'K':
	    expAttr.flushKeys();
	    String[] key = msg.split("\\.");
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
	// P pin = (idxKey 1:)(value 8 chars).(......).
	case 'P':
	    String[] pin = msg.split("\\.");
	    for (String p : pin) {
		String[] rec = p.split(":");
		expAttr.storePin(Integer.parseInt(rec[0]) - 1, Integer.parseInt(rec[1]));
	    }
	    break;
	case 'C':
	    v = Integer.parseInt(msg);
	    if ((v > 1) || (v < 0)) {
		wrongValue("get val:" + v);
	    } else {
		expAttr.setCamPower(v);
	    }
	    break;
	case 'A':
	    v = Integer.parseInt(msg);
	    if ((v > 1) || (v < 0)) {
		wrongValue("get val:" + v);
	    } else {
		expAttr.setAutoCamPower(v);
	    }
	    break;	    
	default:
	    wrongAttribute(attribute + " " + msg);
	    return false;
	}
	return true;
    }

    @Override
    protected boolean specializedParseEvent(final char event, final String msg) {
	switch (event) {
	// M Mode (Armed:[0-1])(Alarm:[0-1])(Prg:[0-1])
	case 'M':
	    int m = Integer.parseInt(msg);
	    if ((m > 111) || (m < 0)) {
		wrongValue("ev mode:" + m);
		return false;
	    } else {
		boolean changed = expAttr.setKeyProgramming(m & 1);
		alarmLogger.logKeyProgramming(changed, m & 1);
		m /= 10;
		changed = expAttr.setAlarmed(m & 1);
		alarmLogger.logAlarm(changed, m & 1);
		m /= 10;
		changed = expAttr.setArmed(m & 1);
		alarmLogger.logArmed(changed, m & 1);
	    }
	    break;
	// K Key changed
	// N New Pin set (value 8 chars)
	case 'N':
	    log.debug("New PIN: {}", msg);
	    alarmLogger.logNewPin(msg);
	case 'K':
	    sendMsgToHal("GAK");
	    sendMsgToHal("GAP");
	    return false;
	// R Key read (value 8 chars)
	// P Pin read (value 8 chars)
	case 'R':
	    log.debug("ALARM: read key {}", msg);
	    alarmLogger.logKeyRead(msg);
	    return false;
	case 'P':
	    log.debug("ALARM: read pin {}", msg);
	    alarmLogger.logPinRead(msg);
	    return false;
	default:
	    wrongEvent(event + " " + msg);
	    return false;
	}
	return true;
    }

    @Override
    public void alignAll() {
	log.debug("Alarm align all");
	sendMsgToHal("GAM");
	sendMsgToHal("GAK");
	sendMsgToHal("GAP");
	sendMsgToHal("GAC");
	sendMsgToHal("GAA");
    }

    @Override
    public Response executeSet(final String attr, String val) throws Exception {
	int n;
	switch (attr) {
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
	    try {
		  n = Integer.parseInt(val);
	    } catch (Exception e) {
		  n = -1;
	    }
	    if (n <= 0) {
		  wrongValue("masterkey:" + n);
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
	    try {
		  n = Integer.parseInt(val);
	    } catch (Exception e) {
		  n = -1;
	    }
	    if ((n < 0) || (n >= 10)) {
		  wrongValue("pin:" + n);
		  return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
	    } else {
		  log.info("Setting pin for key #{}", n);
		  sendMsgToHal("SAP" + val);
		  return Response.ok("Enter PIN from Keyboard", MediaType.TEXT_PLAIN)
			.header("Access-Control-Allow-Origin", "*").build();
	    }
	    // K Key value to add
	case "newkey":
	    if ("".equals(val)) {
		  val = "-1";
	    }
	    try {
		  n = Integer.parseInt(val);
	    } catch (Exception e) {
		  n = -1;
	    }
	    if (n <= 0) {
		  wrongValue("newkey:" + n);
		  return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
	    } else {
		  log.info("Setting key :{}", val);
		  sendMsgToHal("SAK" + val);
	    }
	    break;
	case "campower":
		log.info("Cam power:{}", val);
	    sendMsgToHal("SAC" +  getBooleanVal(val));
	    break;
	case "autocampower":		
	    log.info("Auto cam power:{}", val);
	    sendMsgToHal("SAA" +  getBooleanVal(val));
	    break;	    
	default:
	    throw new Exception();
	}
	return Response.status(Response.Status.OK).header("Access-Control-Allow-Origin", "*").build();
    }

    // Attr Reset
    @Override
    public Response deleteData(final String cmd, String prm) throws Exception {
	switch (cmd) {
	// K Key value to delete
	case "key":
	    if ("".equals(prm)) {
		prm = "-1";
	    }
	    int m;
	    try {
		m = Integer.parseInt(prm);
	    } catch (Exception e) {
		m = -1;
	    }
	    if (m <= 0) {
		wrongValue("key:" + m + " (" + prm + ")");
		break;
	    } else {
		if (expAttr.keyExists(m)) {
		    log.info("Deleting key :{}", prm);
		    sendMsgToHal("RAK" + prm);
		    return Response.status(Response.Status.OK).header("Access-Control-Allow-Origin", "*").build();
		} else {
		    log.error("Key {} doesn't exists", prm);
		    wrongValue("not existing key:" + m);
		}
	    }
	    break;
	// X Master Key cancella tutte le chiavi
	case "allkeys":
	    log.info("Deleting all keys");
	    sendMsgToHal("RAX");
	    return Response.status(Response.Status.OK).header("Access-Control-Allow-Origin", "*").build();
	// P Pin of key cancella tutti I PIN
	case "allpins":
	    log.info("Deleting all pins");
	    sendMsgToHal("RAP");
	    return Response.status(Response.Status.OK).header("Access-Control-Allow-Origin", "*").build();
	default:
	    throw new Exception();
	}
	return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
    }

}
