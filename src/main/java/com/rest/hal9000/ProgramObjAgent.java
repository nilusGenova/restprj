package com.rest.hal9000;

import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;

import javax.ws.rs.core.Response;

public class ProgramObjAgent extends HalObjAgent {

    public enum ModeStates {
	OFF('-'), AUTO('A'), SPECIAL('S'), MAN_OFF('F'), MAN_ON('N');

	private char halPmode;

	ModeStates(char pm) {
	    this.halPmode = pm;
	}
    }

    private SortedSet<ProgramItem> newProgram = null;

    // Attr________Req____answer_______________Set___________________Reset
    // P Program_n:0=qty__Hour-5Min-Day-Mode___val_to_add__________val to del
    // M Mode______*_____[oFf|oN|-|Auto|Spec]__[oFf|oN|-o|Auto|Spec]_CancAll
    // C CntDwn____*_____0:off; timeout in h 0:off; timeout in h
    // (D)Day Temp 0-7;min_temp in 10ofCelsius degrees
    // T Prgr Temp_*_____(T1;T2;T3;Moff;Mon in 10ofCelsius degrees
    // 1 T1 in 10C_X_____X_____________________10ofCelsius degrees
    // 2 T2 in 10C_X_____X_____________________10ofCelsius degrees
    // 3 T3 in 10C_X_____X_____________________10ofCelsius degrees
    // F ManOff10C_X_____X_____________________10ofCelsius degrees
    // N ManOn 10C_X_____X_____________________10ofCelsius degrees

    private class ExposedAttributes {
	private SortedSet<ProgramItem> program = new TreeSet<>();
	private String mode = ModeStates.OFF.toString();
	private int countdown = 0;
	private int dayTemp = 0;
	private int progrTemp = 0;
	private int t1 = 0;
	private int t2 = 0;
	private int t3 = 0;
	private int toff = 0;
	private int ton = 0;
    }

    int itemsToAsk = 0;
    int numOfProgItems = 0;

    private ExposedAttributes expAttr = new ExposedAttributes();

    // [oFf|oN|-|Auto|Spec]
    private void setMode(char cm) {
	for (ModeStates m : ModeStates.values()) {
	    if (m.halPmode == cm) {
		expAttr.mode = m.toString();
		return;
	    }
	}
	wrongValue(0);
    }

    private void setHalMode(String mode) throws Exception {
	mode = mode.toUpperCase();
	for (ModeStates m : ModeStates.values()) {
	    if (mode.equals(m.toString())) {
		sendMsgToHal("SPM" + m.halPmode);
		return;
	    }
	}
	wrongValue(0);
	throw new Exception();
    }

    private void askProgramItems() {
	itemsToAsk = 0;
	numOfProgItems = 0;
	newProgram = new TreeSet<>();
	sendMsgToHal("GPP0");
    }

    private void receivedProgramGetAnsw(String msg) {
	if (itemsToAsk == 0) {
	    numOfProgItems = Integer.parseInt(msg);
	} else {
	    ProgramItem item = new ProgramItem();
	    item.setFromHalFormat(msg);
	    newProgram.add(item);
	}
	if (itemsToAsk >= numOfProgItems) {
	    itemsToAsk = 0; // end of query loop
	    expAttr.program.clear();
	    expAttr.program = newProgram;
	} else {
	    itemsToAsk++;
	    sendMsgToHal("GPP" + itemsToAsk);
	}
    }

    private int getValFromFormattedRequest(String s) {
	String[] v = s.split(":");
	return Integer.parseInt(v[1]);
    }

    // Format of msg
    // h:<hour>-m:<min>-d:<day>-t:<temp>-i:<0|1 interp>
    private ProgramItem validateFormatItem(String val) {
	String[] vals = val.split("-");
	if ((vals.length != 5) || (val.chars().filter(ch -> ch == ':').count() != 5)) {
	    return null;
	}
	if ((vals[0].charAt(0) != 'h') || (vals[1].charAt(0) != 'm') || (vals[2].charAt(0) != 'd')
		|| (vals[3].charAt(0) != 't') || (vals[4].charAt(0) != 'i')) {
	    return null;
	}
	int h = getValFromFormattedRequest(vals[0]);
	int m = getValFromFormattedRequest(vals[1]);
	int d = getValFromFormattedRequest(vals[2]);
	int t = getValFromFormattedRequest(vals[3]);
	int i = getValFromFormattedRequest(vals[4]);
	if ((h < 0) || (h > 23) || (m < 0) || (m > 59) || (d < 0) || (d > 7) || (t < 0) || (t > 3) || (i < 0)
		|| (i > 1)) {
	    return null;
	}
	// int hour, int min, int day, int tempLevel, boolean interpolation
	ProgramItem item = new ProgramItem(h, m, d, t, i == 1);
	return item;
    }

    // return false if item already exists
    private boolean addProgramItem(ProgramItem item) {
	if (expAttr.program.contains(item)) {
	    return false;
	}
	sendMsgToHal("SPP" + item.getHalFormat());
	return true;
    }

    // return false if item doesn't exists
    private boolean deleteProgramItem(ProgramItem item) {
	if (!expAttr.program.contains(item)) {
	    return false;
	}
	sendMsgToHal("RPP" + item.getHalFormat());
	return true;
    }

    public ProgramObjAgent(String pathName, Consumer<String> sendMsgCallBack) {
	super(pathName, sendMsgCallBack);
    }

    @Override
    protected Object getExposedData() {
	log.info("Program exposeData");
	return expAttr;
    }

    @Override
    public void alignAll() {
	log.info("Program align all");
	sendMsgToHal("GPM");
	sendMsgToHal("GPC");
	sendMsgToHal("GPT");
	askProgramItems();
    }

    // Event Prm
    // M Mode changed [ManualOff|ManualOn|-off|Automatic|Interpolation]
    // C Countdown changed timeout in hours
    // Program datas changed
    @Override
    protected void specializedParseEvent(char event, String msg) {
	switch (event) {
	case 'M':
	    setMode(msg.charAt(0));
	    break;
	case 'C':
	    expAttr.countdown = Integer.parseInt(msg);
	    break;
	case 'P':
	    askProgramItems();
	    break;
	default:
	    wrongEvent();
	}
    }

    // Attr________Req____answer_______________Set___________________Reset
    // P Program_n:0=qty__Hour-5Min-Day-Mode___val_to_add__________val to del
    // M Mode______*_____[oFf|oN|-|Auto|Spec]__[oFf|oN|-o|Auto|Spec]_CancAll
    // C CntDwn____*_____0:off; timeout in h 0:off; timeout in h
    // (D)Day Temp 0-7;min_temp in 10ofCelsius degrees
    // T Prgr Temp_*_____(T1;T2;T3;Moff;Mon in 10ofCelsius degrees
    @Override
    protected void specializedParseGetAnswer(char attribute, String msg) {
	switch (attribute) {
	case 'M':
	    setMode(msg.charAt(0));
	    break;
	case 'C':
	    expAttr.countdown = Integer.parseInt(msg);
	    break;
	case 'T':
	    String[] pin = msg.split(";");
	    expAttr.t1 = Integer.parseInt(pin[0]);
	    expAttr.t2 = Integer.parseInt(pin[1]);
	    expAttr.t3 = Integer.parseInt(pin[2]);
	    expAttr.toff = Integer.parseInt(pin[3]);
	    expAttr.ton = Integer.parseInt(pin[4]);
	    break;
	case 'P':
	    receivedProgramGetAnsw(msg);
	    break;
	default:
	    wrongAttribute();
	}
    }

    @Override
    protected String getExposedAttribute(String attr) throws Exception {
	log.info("Program exposeAttribute");
	if ("countdown".equals(attr)) {
	    return Integer.toString(expAttr.countdown);
	}
	if ("mode".equals(attr)) {
	    return expAttr.mode.toString();
	}
	wrongAttribute();
	return null;
    }

    private int convertVal(String val) {
	if ("".equals(val)) {
	    val = "-1";
	}
	return Integer.parseInt(val);
    }

    private Response setPrgAttr(char prgAttr, int temp) {
	if (temp >= 0) {
	    sendMsgToHal("SP" + prgAttr + temp);
	    return Response.status(Response.Status.OK).build();
	} else {
	    return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
	}
    }

    @Override
    public Response executeSet(String attr, String val) throws Exception {
	int n;
	switch (attr) {
	// M Mode______*_____[oFf|oN|-|Auto|Spec]
	case "mode":
	    setHalMode(val);
	    break;
	// C CntDwn____*_____0:off; timeout in h
	case "countdown":
	    n = convertVal(val);
	    if (n <= 0) {
		wrongValue(n);
		return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
	    } else {
		log.info("Setting countdown:{} hours", val);
		sendMsgToHal("SPC" + val);
	    }
	    break;
	// 1 T1 in 10C_X_____X_____________________10ofCelsius degrees
	// 2 T2 in 10C_X_____X_____________________10ofCelsius degrees
	// 3 T3 in 10C_X_____X_____________________10ofCelsius degrees
	case "temp1":
	case "temp2":
	case "temp3":
	    n = convertVal(val);
	    if (n <= 0) {
		wrongValue(n);
		return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
	    } else {
		log.info("Setting temp {} to {}", attr.charAt(4), val);
		return setPrgAttr(attr.charAt(4), n);
	    }
	    // F ManOff10C_X_____X_____________________10ofCelsius degrees
	case "temp_off":
	    n = convertVal(val);
	    if (n <= 0) {
		wrongValue(n);
		return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
	    } else {
		log.info("Setting temp OFF to {}", val);
		return setPrgAttr('F', n);
	    }
	    // N ManOn 10C_X_____X_____________________10ofCelsius degrees
	case "temp_on":
	    n = convertVal(val);
	    if (n <= 0) {
		wrongValue(n);
		return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
	    } else {
		log.info("Setting temp ON to {}", val);
		return setPrgAttr('N', n);
	    }
	    // P Program_n:0=qty__Hour-5Min-Day-Mode___val_to_add__________val to del
	case "entry": {
	    ProgramItem item = validateFormatItem(val);
	    if (item == null) {
		wrongValue(val);
		return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
	    } else {
		log.info("Adding program item:{}", val);
		if (!addProgramItem(item)) {
		    log.info("Item already exists");
		    return Response.status(Response.Status.FOUND).build();
		}
	    }
	}
	    break;
	default:
	    throw new NoSuchElementException();
	}
	return Response.status(Response.Status.OK).build();

    }

    @Override
    public Response deleteData(String cmd, String prm) throws Exception {
	switch (cmd) {
	case "allprograms":
	    log.info("Deleting all programs");
	    // Attr________Req____answer_______________Set___________________Reset
	    // M Mode______*_____[oFf|oN|-|Auto|Spec]__[oFf|oN|-o|Auto|Spec]_CancAll
	    sendMsgToHal("RPM");
	    return Response.status(Response.Status.OK).build();
	case "entry": {
	    ProgramItem item = validateFormatItem(prm);
	    if (item == null) {
		wrongValue(prm);
	    } else {
		log.info("Deleting program item:{}", prm);
		if (!deleteProgramItem(item)) {
		    log.info("Item does'n exist");
		    return Response.status(Response.Status.BAD_REQUEST).build();
		} else {
		    return Response.status(Response.Status.OK).build();
		}
	    }
	}
	    break;
	default:
	    throw new Exception();
	}
	return Response.status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE).build();
    }
}
