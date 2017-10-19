package com.rest.test;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rest.hal9000.ProgramItem;
import com.rest.hal9000.ProgramObjAgent;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({ ProgramObjAgent.class })
public class ProgramObjAgentTest {
    // int hour, int min, int day, int tempLevel, boolean interpolation
    private final static ProgramItem[] PRG = { new ProgramItem(2, 5, 1, 1, false), new ProgramItem(15, 23, 7, 3, true),
	    new ProgramItem(15, 5, 2, 2, false), new ProgramItem(18, 45, 1, 2, true) };
    // FORMAT h:<hour>-m:<min>-d:<day>-t:<temp>-i:<0|1 interp>
    private final static String[] WRG = { "h:25-m:5-d:1-t:1-i:0", "h:-1-m:5-d:1-t:1-i:0", "h:15-m:70-d:1-t:1-i:0",
	    "h:15-m:-1-d:1-t:1-i:0", "h:15-m:5-d:8-t:1-i:0", "h:15-m:5-d:-1-t:1-i:0", "h:15-m:5-d:1-t:-1-i:0",
	    "h:15-m:5-d:1-t:4-i:0", "h:15-m:5-d:1-t:1-i:o", "h:o5-m:5-d:1-t:1-i:0", "h:15-m:5-d:1-t:1-i:2",
	    "m:5-d:1-t:1-i:0", "h:15-m:5-t:1-i:0", };

    ArrayList<String> msgSent = new ArrayList<>();

    void sendMsg(final String msg) {
	msgSent.add(msg);
	System.out.println("MSG SENT:" + msg);
    }

    String getSentMsg() {
	if (noMsgSent()) {
	    return "";
	} else {
	    String msg = msgSent.get(0);
	    msgSent.remove(0);
	    return msg;
	}
    }

    boolean noMsgSent() {
	return msgSent.size() == 0;
    }

    void emptySentMsg() {
	msgSent.clear();
    }

    @InjectMocks
    private final ProgramObjAgent program = new ProgramObjAgent("program", (s) -> sendMsg(s));

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
    }

    private String extractAttributeValueAsStr(String attribute) {
	try {
	    ObjectMapper mapper = new ObjectMapper();
	    JsonNode rootNode = mapper.readTree((String) program.exposeJsonData().getEntity());
	    Iterator<Map.Entry<String, JsonNode>> fieldsIterator = rootNode.fields();
	    while (fieldsIterator.hasNext()) {
		Map.Entry<String, JsonNode> field = fieldsIterator.next();
		if (field.getKey().equals(attribute)) {
		    return field.getValue().toString();
		}
	    }
	    return null;
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }

    private int extractAttributeValueAsInt(String attribute) {
	return Integer.parseInt(extractAttributeValueAsStr(attribute));
    }

    @Test
    public void testExposeJsonAttributeMode() {
	emptySentMsg();
	program.parseGetAnswer('M', "A");
	String et = null;
	String err = null;
	Response r = null;
	Response.Status st = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	Response.Status sterr = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	try {
	    r = program.exposeJsonAttribute("mode");
	    et = (String) r.getEntity();
	    st = Response.Status.fromStatusCode(r.getStatus());
	    r = program.exposeJsonAttribute("stanlio");
	    err = (String) r.getEntity();
	    sterr = Response.Status.fromStatusCode(r.getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "AUTO", et);
	Assert.assertNull("ERROR:", err);
	Assert.assertEquals("ERROR in return value", Response.Status.OK, st);
	Assert.assertEquals("ERROR in return value", Response.Status.BAD_REQUEST, sterr);
	Assert.assertEquals("ERROR:", "", getSentMsg());
    }

    @Test
    public void testExposeJsonAttributeCntdwn() {
	emptySentMsg();
	program.parseGetAnswer('C', "118");
	String et = null;
	Response r = null;
	Response.Status st = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	try {
	    r = program.exposeJsonAttribute("countdown");
	    et = (String) r.getEntity();
	    st = Response.Status.fromStatusCode(r.getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "118", et);
	Assert.assertEquals("ERROR in return value", Response.Status.OK, st);
	Assert.assertEquals("ERROR:", "", getSentMsg());
    }

    @Test
    public void testParseEvent() {
	emptySentMsg();
	// M Mode changed [ManualOff|ManualOn|-off|Automatic|Interpolation]
	program.parseEvent('M', "-");
	Assert.assertEquals("ERROR:", "\"OFF\"", extractAttributeValueAsStr("mode"));
	program.parseEvent('M', "N");
	Assert.assertEquals("ERROR:", "\"MAN_ON\"", extractAttributeValueAsStr("mode"));
	program.parseEvent('M', "F");
	Assert.assertEquals("ERROR:", "\"MAN_OFF\"", extractAttributeValueAsStr("mode"));
	program.parseEvent('M', "A");
	Assert.assertEquals("ERROR:", "\"AUTO\"", extractAttributeValueAsStr("mode"));
	program.parseEvent('M', "S");
	Assert.assertEquals("ERROR:", "\"SPECIAL\"", extractAttributeValueAsStr("mode"));
	// C Countdown changed timeout in hours
	program.parseEvent('C', "150");
	Assert.assertEquals("ERROR:", 150, extractAttributeValueAsInt("countdown"));
	program.parseEvent('C', "2");
	Assert.assertEquals("ERROR:", 2, extractAttributeValueAsInt("countdown"));
	program.parseEvent('C', "0");
	Assert.assertEquals("ERROR:", 0, extractAttributeValueAsInt("countdown"));
	Assert.assertEquals("ERROR:", "", getSentMsg());
    }

    @Test
    public void testParseGetAnswer() {
	// NOT YET IMPLEMENTED ON HAL9000 (D)Day Temp 0-7;min_temp in 10ofCelsius
	emptySentMsg();
	// M Mode______*_____[oFf|oN|-|Auto|Spec]__[oFf|oN|-o|Auto|Spec]_CancAll
	program.parseGetAnswer('M', "-");
	Assert.assertEquals("ERROR:", "\"OFF\"", extractAttributeValueAsStr("mode"));
	program.parseGetAnswer('M', "N");
	Assert.assertEquals("ERROR:", "\"MAN_ON\"", extractAttributeValueAsStr("mode"));
	program.parseGetAnswer('M', "F");
	Assert.assertEquals("ERROR:", "\"MAN_OFF\"", extractAttributeValueAsStr("mode"));
	program.parseGetAnswer('M', "A");
	Assert.assertEquals("ERROR:", "\"AUTO\"", extractAttributeValueAsStr("mode"));
	program.parseGetAnswer('M', "S");
	Assert.assertEquals("ERROR:", "\"SPECIAL\"", extractAttributeValueAsStr("mode"));
	// C CntDwn____*_____0:off; timeout in h 0:off; timeout in h
	program.parseGetAnswer('C', "150");
	Assert.assertEquals("ERROR:", 150, extractAttributeValueAsInt("countdown"));
	program.parseGetAnswer('C', "2");
	Assert.assertEquals("ERROR:", 2, extractAttributeValueAsInt("countdown"));
	program.parseGetAnswer('C', "0");
	Assert.assertEquals("ERROR:", 0, extractAttributeValueAsInt("countdown"));
	// T Prgr Temp_*_____(T1;T2;T3;Moff;Mon in 10ofCelsius degrees
	program.parseGetAnswer('T', "15;18;20;9;23");
	Assert.assertEquals("ERROR:", 15, extractAttributeValueAsInt("t1"));
	Assert.assertEquals("ERROR:", 18, extractAttributeValueAsInt("t2"));
	Assert.assertEquals("ERROR:", 20, extractAttributeValueAsInt("t3"));
	Assert.assertEquals("ERROR:", 9, extractAttributeValueAsInt("toff"));
	Assert.assertEquals("ERROR:", 23, extractAttributeValueAsInt("ton"));
	Assert.assertEquals("ERROR:", "", getSentMsg());
    }

    public void testExposeJsonAttributeSize(final int size) {
	String et = null;
	Response r = null;
	Response.Status st = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	try {
	    r = program.exposeJsonAttribute("size");
	    et = (String) r.getEntity();
	    st = Response.Status.fromStatusCode(r.getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", Integer.toString(size), et);
	Assert.assertEquals("ERROR in return value", Response.Status.OK, st);
	Assert.assertEquals("ERROR:", "", getSentMsg());
    }

    // h:<hour>-m:<min>-d:<day>-t:<temp>-i:<0|1 interp>
    private String urlFormatPrgEntry(final ProgramItem prg) {
	return String.format("h:%d-m:%d-d:%d-t:%d-i:%d", prg.getHour(), prg.getMin(), prg.getDay(), prg.getTempLevel(),
		prg.isInterpolation() ? 1 : 0);
    }

    @Test
    public void testProgramGet() {
	// P Program_n:0=qty__Hour-5Min-Day-Mode___val_to_add__________val to del
	emptySentMsg();
	program.parseEvent('P', "");
	Assert.assertEquals("ERROR:", "GPP0", getSentMsg());
	program.parseGetAnswer('P', Integer.toString(PRG.length)); // n programmi
	for (int i = 1; i <= PRG.length; i++) {
	    Assert.assertEquals("ERROR:", "GPP" + i, getSentMsg());
	    Assert.assertEquals("ERROR:", "", getSentMsg());
	    Assert.assertEquals("ERROR:", "", getSentMsg());
	    program.parseGetAnswer('P', PRG[i - 1].getHalFormat());
	}
	Assert.assertEquals("ERROR:", "", getSentMsg());
	Assert.assertEquals("ERROR:", "", getSentMsg());
	testExposeJsonAttributeSize(PRG.length);
	// test Delete Data
	// program?c=entry&p=h:<hour>-m:<min>-d:<day>-t:<temp>-i:<0|1 interp>
	emptySentMsg();
	Response.Status et = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	try {
	    et = Response.Status.fromStatusCode(program.deleteData("entry", urlFormatPrgEntry(PRG[0])).getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "RPP" + PRG[0].getHalFormat(), getSentMsg());
	Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	Assert.assertEquals("ERROR:", "", getSentMsg());
	// test switch
	// Now I want to have two item program
	program.parseEvent('P', "");
	getSentMsg();
	program.parseGetAnswer('P', "2");
	getSentMsg();
	program.parseGetAnswer('P', PRG[1].getHalFormat());
	getSentMsg();
	testExposeJsonAttributeSize(PRG.length);
	program.parseGetAnswer('P', PRG[0].getHalFormat());
	testExposeJsonAttributeSize(2);
	Assert.assertEquals("ERROR:", "", getSentMsg());
    }

    @Test
    public void testDeleteDataNotExisting() {
	// test Delete Data
	// program?c=entry&p=h:<hour>-m:<min>-d:<day>-t:<temp>-i:<0|1 interp>
	emptySentMsg();
	// Now I'm sure to have one item program
	program.parseEvent('P', "");
	getSentMsg();
	program.parseGetAnswer('P', "1");
	getSentMsg();
	program.parseGetAnswer('P', PRG[1].getHalFormat());

	Response.Status et = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	try {
	    et = Response.Status.fromStatusCode(program.deleteData("entry", urlFormatPrgEntry(PRG[0])).getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR in return value", Response.Status.BAD_REQUEST, et);
	Assert.assertEquals("ERROR:", "", getSentMsg());
    }

    @Test
    public void testDeleteAll() {
	// test Delete Data
	// program?c=entry&p=h:<hour>-m:<min>-d:<day>-t:<temp>-i:<0|1 interp>
	emptySentMsg();
	Response.Status et = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	try {
	    et = Response.Status.fromStatusCode(program.deleteData("allprograms", "").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "RPM", getSentMsg());
	Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	Assert.assertEquals("ERROR:", "", getSentMsg());
    }

    @Test
    public void testEmptyProgram() {
	// test Delete Data
	// program?c=entry&p=h:<hour>-m:<min>-d:<day>-t:<temp>-i:<0|1 interp>
	emptySentMsg();
	// Now I'm sure to have zero item program
	program.parseEvent('P', "");
	Assert.assertEquals("ERROR:", "GPP0", getSentMsg());
	program.parseGetAnswer('P', "0");
	testExposeJsonAttributeSize(0);
	Assert.assertEquals("ERROR:", "", getSentMsg());
	// test Delete Data
	Response.Status et = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	try {
	    et = Response.Status.fromStatusCode(program.deleteData("entry", urlFormatPrgEntry(PRG[0])).getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR in return value", Response.Status.BAD_REQUEST, et);
	Assert.assertEquals("ERROR:", "", getSentMsg());
    }

    private void testExecuteSetAttrOk(String attr, String urlVal, String halVal) {
	// http://localhost:8080/hal9000/program?a=mode&v=<mode>
	Response.Status et = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	try {
	    et = Response.Status.fromStatusCode(program.executeSet(attr, urlVal).getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", halVal, getSentMsg());
	Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	Assert.assertEquals("ERROR:", "", getSentMsg());
    }

    private void testExecuteSetAttrWrong(String attr, String urlVal, Response.Status resp) {
	// http://localhost:8080/hal9000/program?a=mode&v=<mode>
	Response.Status et = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	try {
	    et = Response.Status.fromStatusCode(program.executeSet(attr, urlVal).getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR in return value", resp, et);
	Assert.assertEquals("ERROR:", "", getSentMsg());
    }

    @Test
    public void testExecuteSetModes() {
	// http://localhost:8080/hal9000/program?a=mode&v=<mode>
	// OFF('-'), AUTO('A'), SPECIAL('S'), MAN_OFF('F'), MAN_ON('N');
	emptySentMsg();
	testExecuteSetAttrOk("mode", "off", "SPM-");
	testExecuteSetAttrOk("mode", "auto", "SPMA");
	testExecuteSetAttrOk("mode", "special", "SPMS");
	testExecuteSetAttrOk("mode", "MAN_OFF", "SPMF");
	testExecuteSetAttrOk("mode", "man_on", "SPMN");
    }

    @Test
    public void testExecuteSetCntDwn() {
	// http://localhost:8080/hal9000/program?a=countdown&v=<hours>
	emptySentMsg();
	testExecuteSetAttrOk("countdown", "35", "SPC35");
    }

    @Test
    public void testExecuteSetTemp() {
	// http://localhost:8080/hal9000/program?a=temp1&v=<Celsius*10>
	// http://localhost:8080/hal9000/program?a=temp2&v=<Celsius*10>
	// http://localhost:8080/hal9000/program?a=temp3&v=<Celsius*10>
	// http://localhost:8080/hal9000/program?a=temp_off&v=<Celsius*10>
	// http://localhost:8080/hal9000/program?a=temp_on&v=<Celsius*10>
	emptySentMsg();
	testExecuteSetAttrOk("temp1", "90", "SP190");
	testExecuteSetAttrOk("temp2", "180", "SP2180");
	testExecuteSetAttrOk("temp3", "243", "SP3243");
	testExecuteSetAttrOk("temp_off", "100", "SPF100");
	testExecuteSetAttrOk("temp_on", "250", "SPN250");
    }

    @Test
    public void testExecuteSetAlreadyExists() {
	// program?a=entry&v=h:<hour>-m:<min>-d:<day>-t:<temp>-i:<0|1 interp>
	// Now I'm sure to have one item program
	emptySentMsg();
	program.parseEvent('P', "");
	getSentMsg();
	program.parseGetAnswer('P', "1");
	getSentMsg();
	program.parseGetAnswer('P', PRG[0].getHalFormat());
	Response.Status et = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	try {
	    et = Response.Status.fromStatusCode(program.executeSet("entry", urlFormatPrgEntry(PRG[0])).getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR in return value", Response.Status.FOUND, et);
	Assert.assertEquals("ERROR:", "", getSentMsg());
    }

    @Test
    public void testExecuteSet() {
	// program?a=entry&v=h:<hour>-m:<min>-d:<day>-t:<temp>-i:<0|1 interp>
	emptySentMsg();
	// Now I'm sure to have zero item program
	program.parseEvent('P', "");
	Assert.assertEquals("ERROR:", "GPP0", getSentMsg());
	program.parseGetAnswer('P', "0");
	testExposeJsonAttributeSize(0);
	for (int i = 1; i <= PRG.length; i++) {
	    testExecuteSetAttrOk("entry", urlFormatPrgEntry(PRG[i - 1]), "SPP" + PRG[i - 1].getHalFormat());
	}
	Assert.assertEquals("ERROR:", "", getSentMsg());
    }

    @Test
    public void testExecuteWrongSet() {
	// program?a=entry&v=h:<hour>-m:<min>-d:<day>-t:<temp>-i:<0|1 interp>
	emptySentMsg();
	// Now I'm sure to have zero item program
	program.parseEvent('P', "");
	Assert.assertEquals("ERROR:", "GPP0", getSentMsg());
	program.parseGetAnswer('P', "0");
	testExposeJsonAttributeSize(0);
	for (int i = 1; i <= WRG.length; i++) {
	    testExecuteSetAttrWrong("entry", WRG[i - 1], Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE);
	}
	Assert.assertEquals("ERROR:", "", getSentMsg());
    }

}
