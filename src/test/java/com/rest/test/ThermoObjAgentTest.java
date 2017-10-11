package com.rest.test;

import static org.junit.Assert.fail;

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

    String msgSent = "";

    void sendMsg(String msg) {
	msgSent = msg;
	System.out.println("MSG SENT:" + msg);
    }

    @InjectMocks
    private final ThermoObjAgent thermo = new ThermoObjAgent("thermo", (s) -> sendMsg(s));

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
	thermo.parseGetAnswer('W', "0");
	Assert.assertEquals("ERROR:", 0, extractAttributeValueAsInt("warming"));
	thermo.parseGetAnswer('W', "1");
	Assert.assertEquals("ERROR:", 1, extractAttributeValueAsInt("warming"));
	thermo.parseGetAnswer('T', "324");
	Assert.assertEquals("ERROR:", 32.4, extractAttributeValueAsDouble("temperature"), 0);
	thermo.parseGetAnswer('T', "80");
	Assert.assertEquals("ERROR:", 8, extractAttributeValueAsDouble("temperature"), 0);
	thermo.parseGetAnswer('R', "215");
	Assert.assertEquals("ERROR:", 21.5, extractAttributeValueAsDouble("required"), 0);
	thermo.parseGetAnswer('R', "60");
	Assert.assertEquals("ERROR:", 6, extractAttributeValueAsDouble("required"), 0);
	thermo.parseGetAnswer('H', "52");
	Assert.assertEquals("ERROR:", 5.2, extractAttributeValueAsDouble("hysteresis"), 0);
	thermo.parseGetAnswer('H', "30");
	Assert.assertEquals("ERROR:", 3, extractAttributeValueAsDouble("hysteresis"), 0);
    }

    @Test
    public void testParseEvent() {
	// W T R (E)
	thermo.parseEvent('W', "0");
	Assert.assertEquals("ERROR:", 0, extractAttributeValueAsInt("warming"));
	thermo.parseEvent('W', "1");
	Assert.assertEquals("ERROR:", 1, extractAttributeValueAsInt("warming"));
	thermo.parseEvent('T', "324");
	Assert.assertEquals("ERROR:", 32.4, extractAttributeValueAsDouble("temperature"), 0);
	thermo.parseEvent('T', "80");
	Assert.assertEquals("ERROR:", 8, extractAttributeValueAsDouble("temperature"), 0);
	thermo.parseEvent('R', "215");
	Assert.assertEquals("ERROR:", 21.5, extractAttributeValueAsDouble("required"), 0);
	thermo.parseEvent('R', "60");
	Assert.assertEquals("ERROR:", 6, extractAttributeValueAsDouble("required"), 0);
    }

    @Test
    public void testExposeJsonAttribute() {
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
	Assert.assertEquals("ERROR in return value", st, Response.Status.OK);
	Assert.assertEquals("ERROR in return value", sterr, Response.Status.BAD_REQUEST);
    }

    @Test
    public void testExecuteSetRequired() {
	Response.Status et = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	msgSent = null;
	try {
	    et = Response.Status.fromStatusCode(thermo.executeSet("required", "214").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "STR214", msgSent);
	Assert.assertEquals("ERROR in return value", et, Response.Status.OK);
    }

    @Test
    public void testExecuteSetHysteresis() {
	Response.Status et = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	msgSent = null;
	try {
	    et = Response.Status.fromStatusCode(thermo.executeSet("hysteresis", "32").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "STH32", msgSent);
	Assert.assertEquals("ERROR in return value", et, Response.Status.OK);
    }

}
