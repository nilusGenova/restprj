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
import com.rest.hal9000.ThermoObjAgent;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({ ThermoObjAgent.class })
public class ThermoObjAgentTest {

    ArrayList<String> msgSent = new ArrayList<>();
    String logMsg = "";

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

    void logger(String msg) {
	logMsg = msg;
    }

    void emptySentMsg() {
	msgSent.clear();
    }

    @InjectMocks
    private final ThermoObjAgent thermo = new ThermoObjAgent("thermo", (s) -> sendMsg(s), (s) -> logger(s));

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
    }

    private double extractAttributeValueAsDouble(String attribute) {
	try {
	    ObjectMapper mapper = new ObjectMapper();
	    JsonNode rootNode = mapper.readTree((String) thermo.exposeJsonData().getEntity());
	    Iterator<Map.Entry<String, JsonNode>> fieldsIterator = rootNode.fields();
	    while (fieldsIterator.hasNext()) {
		Map.Entry<String, JsonNode> field = fieldsIterator.next();
		if (field.getKey().equals(attribute)) {
		    return field.getValue().asDouble();
		}
	    }
	    return -1;
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return -1;
    }

    private int extractAttributeValueAsInt(String attribute) {
	Double d = extractAttributeValueAsDouble(attribute);
	return d.intValue();
    }

    @Test
    public void testParseGetAnswer() {
	// W T R H
	emptySentMsg();
	thermo.parseGetAnswer('W', "0");
	Assert.assertEquals("ERROR:", 0, extractAttributeValueAsInt("warming"));
	thermo.parseGetAnswer('W', "1");
	Assert.assertEquals("ERROR:", 1, extractAttributeValueAsInt("warming"));
	thermo.parseGetAnswer('T', "324;35");
	Assert.assertEquals("ERROR:", 32.4, extractAttributeValueAsDouble("temperature"), 0);
	Assert.assertEquals("ERROR:", 35, extractAttributeValueAsInt("humidity"));
	thermo.parseGetAnswer('T', "80;9");
	Assert.assertEquals("ERROR:", 8, extractAttributeValueAsDouble("temperature"), 0);
	Assert.assertEquals("ERROR:", 9, extractAttributeValueAsInt("humidity"));
	thermo.parseGetAnswer('R', "215");
	Assert.assertEquals("ERROR:", 21.5, extractAttributeValueAsDouble("required"), 0);
	thermo.parseGetAnswer('R', "60");
	Assert.assertEquals("ERROR:", 6, extractAttributeValueAsDouble("required"), 0);
	thermo.parseGetAnswer('H', "52");
	Assert.assertEquals("ERROR:", 5.2, extractAttributeValueAsDouble("hysteresis"), 0);
	thermo.parseGetAnswer('H', "30");
	Assert.assertEquals("ERROR:", 3, extractAttributeValueAsDouble("hysteresis"), 0);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testParseEvent() {
	// W T R (E)
	emptySentMsg();
	logMsg = "";
	thermo.parseEvent('W', "0");
	Assert.assertEquals("ERROR:", 0, extractAttributeValueAsInt("warming"));
	thermo.parseEvent('W', "1");
	Assert.assertEquals("ERROR:", 1, extractAttributeValueAsInt("warming"));
	Assert.assertEquals("LOG ERROR", "", logMsg);
	thermo.parseEvent('T', "324");
	Assert.assertEquals("ERROR:", 32.4, extractAttributeValueAsDouble("temperature"), 0);
	Assert.assertEquals("LOG ERROR", "324", logMsg);
	thermo.parseEvent('T', "80");
	Assert.assertEquals("LOG ERROR", "80", logMsg);
	logMsg = "";
	Assert.assertEquals("ERROR:", 8, extractAttributeValueAsDouble("temperature"), 0);
	thermo.parseEvent('R', "215");
	Assert.assertEquals("ERROR:", 21.5, extractAttributeValueAsDouble("required"), 0);
	thermo.parseEvent('R', "60");
	Assert.assertEquals("ERROR:", 6, extractAttributeValueAsDouble("required"), 0);
	Assert.assertTrue("ERROR:", noMsgSent());
	Assert.assertEquals("LOG ERROR", "", logMsg);
    }

    @Test
    public void testExposeJsonAttribute() {
	emptySentMsg();
	thermo.parseGetAnswer('W', "1");
	String et = null;
	String err = null;
	Response r = null;
	Response.Status st = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	Response.Status sterr = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	try {
	    r = thermo.exposeJsonAttribute("warming");
	    et = (String) r.getEntity();
	    st = Response.Status.fromStatusCode(r.getStatus());
	    r = thermo.exposeJsonAttribute("pluto");
	    err = (String) r.getEntity();
	    sterr = Response.Status.fromStatusCode(r.getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "1", et);
	Assert.assertNull("ERROR:", err);
	Assert.assertEquals("ERROR in return value", Response.Status.OK, st);
	Assert.assertEquals("ERROR in return value", Response.Status.BAD_REQUEST, sterr);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    public void testExecuteSetAttr(final String attr, final String val, final String expMsg, Response.Status retVal) {
	Response.Status et = Response.Status.BAD_REQUEST;
	emptySentMsg();
	try {
	    et = Response.Status.fromStatusCode(thermo.executeSet(attr, val).getStatus());
	} catch (Exception e) {
	    if (retVal == Response.Status.OK) {
		e.printStackTrace();
		fail("exception");
	    }
	}
	Assert.assertEquals("ERROR:", expMsg, getSentMsg());
	Assert.assertEquals("ERROR in return value", retVal, et);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testExecuteSets() {
	testExecuteSetAttr("required", "214", "STR214", Response.Status.OK);
	testExecuteSetAttr("hysteresis", "32", "STH32", Response.Status.OK);
	testExecuteSetAttr("required", "-3", "", Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE);
	testExecuteSetAttr("hysteresis", "-3", "", Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE);
	testExecuteSetAttr("citrullo", "", "", Response.Status.BAD_REQUEST);
    }

}
