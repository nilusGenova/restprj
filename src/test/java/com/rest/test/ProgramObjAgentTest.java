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
import com.rest.hal9000.ProgramObjAgent;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({ ProgramObjAgent.class })
public class ProgramObjAgentTest {

    ArrayList<String> msgSent = new ArrayList<>();

    void sendMsg(String msg) {
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
	Assert.assertTrue("ERROR:", noMsgSent());
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
	Assert.assertTrue("ERROR:", noMsgSent());
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
	// P Program datas changed
	program.parseEvent('P', "");
	Assert.assertEquals("ERROR:", "GPP0", getSentMsg());
	Assert.assertTrue("ERROR:", noMsgSent());
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
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testProgramGet() {
	// P Program_n:0=qty__Hour-5Min-Day-Mode___val_to_add__________val to del
	// TODO:
	fail("Not yet implemented");
    }

    @Test
    public void testDeleteData() {
	// TODO:
	// http://localhost:8080/hal9000/program?c=allprograms
	// http://localhost:8080/hal9000/program?c=entry&p=h:<hour>-m:<min>-d:<day>-t:<temp>-i:<0|1
	// interp>
	fail("Not yet implemented");
    }

    @Test
    public void testExecuteSet() {
	// TODO:
	// http://localhost:8080/hal9000/program?a=mode&v=<mode>
	// http://localhost:8080/hal9000/program?a=countdown&v=<hours>
	// http://localhost:8080/hal9000/program?a=temp1&v=<Celsius*10>
	// http://localhost:8080/hal9000/program?a=temp2&v=<Celsius*10>
	// http://localhost:8080/hal9000/program?a=temp3&v=<Celsius*10>
	// http://localhost:8080/hal9000/program?a=temp_off&v=<Celsius*10>
	// http://localhost:8080/hal9000/program?a=temp_on&v=<Celsius*10>
	// http://localhost:8080/hal9000/program?a=entry&v=h:<hour>-m:<min>-d:<day>-t:<temp>-i:<0|1
	// interp>
	fail("Not yet implemented");
    }

}
