package com.rest.test;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rest.hal9000.TempLogger;
import com.rest.hal9000.ThermoObjAgent;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ThermoObjAgent.class, TempLogger.class })
public class ThermoObjAgentTest {

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
    private final ThermoObjAgent thermo = new ThermoObjAgent("thermo", (s) -> sendMsg(s));

    @Mock
    private TempLogger tempLog;

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
	thermo.parseGetAnswer('T', "179;71");
	Assert.assertEquals("ERROR:", 17.9, extractAttributeValueAsDouble("temperature"), 0);
	Assert.assertEquals("ERROR:", 71, extractAttributeValueAsInt("humidity"));
	thermo.parseGetAnswer('R', "215");
	Assert.assertEquals("ERROR:", 21.5, extractAttributeValueAsDouble("required"), 0);
	Assert.assertEquals("ERROR:", 0, extractAttributeValueAsInt("manuallyForced"));
	thermo.parseGetAnswer('R', "60");
	Assert.assertEquals("ERROR:", 6, extractAttributeValueAsDouble("required"), 0);
	Assert.assertEquals("ERROR:", 0, extractAttributeValueAsInt("manuallyForced"));
	thermo.parseGetAnswer('R', "1299");
	Assert.assertEquals("ERROR:", 29.9, extractAttributeValueAsDouble("required"), 0);
	Assert.assertEquals("ERROR:", 1, extractAttributeValueAsInt("manuallyForced"));
	thermo.parseGetAnswer('H', "52");
	Assert.assertEquals("ERROR:", 5.2, extractAttributeValueAsDouble("hysteresis"), 0);
	thermo.parseGetAnswer('H', "30");
	Assert.assertEquals("ERROR:", 3, extractAttributeValueAsDouble("hysteresis"), 0);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testParseEvent() {
	PowerMockito.mockStatic(ThermoObjAgent.class);
	Field field = PowerMockito.field(ThermoObjAgent.class, "tempLogger");
	try {
	    field.set(ThermoObjAgent.class, tempLog);
	} catch (IllegalArgumentException e1) {
	    e1.printStackTrace();
	} catch (IllegalAccessException e1) {
	    e1.printStackTrace();
	}
	// W T R (E)
	emptySentMsg();
	thermo.parseEvent('W', "0");
	Assert.assertEquals("ERROR:", 0, extractAttributeValueAsInt("warming"));
	verify(tempLog).logWarming(0);
	thermo.parseEvent('W', "1");
	Assert.assertEquals("ERROR:", 1, extractAttributeValueAsInt("warming"));
	verify(tempLog).logWarming(1);
	thermo.parseEvent('T', "324");
	Assert.assertEquals("ERROR:", 32.4, extractAttributeValueAsDouble("temperature"), 0);
	verify(tempLog).logTemp(32.4);
	thermo.parseEvent('T', "80");
	verify(tempLog).logTemp(8.0);
	Assert.assertEquals("ERROR:", 8, extractAttributeValueAsDouble("temperature"), 0);
	thermo.parseEvent('R', "215");
	Assert.assertEquals("ERROR:", 21.5, extractAttributeValueAsDouble("required"), 0);
	Assert.assertEquals("ERROR:", 0, extractAttributeValueAsInt("manuallyForced"));
	verify(tempLog).logReqTemp(0, 21.5);
	thermo.parseEvent('R', "60");
	Assert.assertEquals("ERROR:", 6, extractAttributeValueAsDouble("required"), 0);
	Assert.assertEquals("ERROR:", 0, extractAttributeValueAsInt("manuallyForced"));
	verify(tempLog).logReqTemp(0, 6.0);
	thermo.parseEvent('R', "1302");
	Assert.assertEquals("ERROR:", 30.2, extractAttributeValueAsDouble("required"), 0);
	Assert.assertEquals("ERROR:", 1, extractAttributeValueAsInt("manuallyForced"));
	verify(tempLog).logReqTemp(1, 30.2);
	Assert.assertTrue("ERROR:", noMsgSent());
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
