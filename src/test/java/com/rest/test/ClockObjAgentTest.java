package com.rest.test;

import static org.junit.Assert.fail;

import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rest.hal9000.ClockObjAgent;
import com.rest.hal9000.EpocTime;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({ ClockObjAgent.class })
public class ClockObjAgentTest {

    String msgSent = "";

    void sendMsg(String msg) {
	msgSent = msg;
	System.out.println("MSG SENT:" + msg);
    }

    @InjectMocks
    private final ClockObjAgent clock = new ClockObjAgent("clock", (s) -> sendMsg(s));

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
    }

    private EpocTime extractAttributeValue() {
	try {
	    ObjectMapper mapper = new ObjectMapper();
	    return mapper.readValue((String) clock.exposeJsonData().getEntity(), EpocTime.class);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }

    @Test
    public void testParseGetAnswer() {
	clock.parseGetAnswer('D', "14-02-1967");
	clock.parseGetAnswer('T', "18:07");
	clock.parseGetAnswer('W', "3");
	Assert.assertEquals("ERROR:", "14-02-1967", extractAttributeValue().getDate());
	Assert.assertEquals("ERROR:", "18:07", extractAttributeValue().getTime());
	Assert.assertEquals("ERROR:", 3, extractAttributeValue().getWeekDay());
	clock.parseGetAnswer('E', "7623412");
	Assert.assertEquals("ERROR:", "7623412", extractAttributeValue().getEpochTime());
    }

    @Test
    public void testParseEvent() {
	// D forza il riallineamento
	clock.parseEvent('D', "");
	Assert.assertEquals("ERROR:", "GCE", msgSent);
    }

    @Test
    public void testExposeJsonAttribute() {
	clock.parseGetAnswer('E', "38569341");
	String et = null;
	String err = null;
	Response r = null;
	Response.Status st = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	Response.Status sterr = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	try {
	    r = clock.exposeJsonAttribute("epochtime");
	    et = (String) r.getEntity();
	    st = Response.Status.fromStatusCode(r.getStatus());
	    r = clock.exposeJsonAttribute("pippo");
	    err = (String) r.getEntity();
	    sterr = Response.Status.fromStatusCode(r.getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "38569341", et);
	Assert.assertNull("ERROR:", err);
	Assert.assertEquals("ERROR in return value", st, Response.Status.OK);
	Assert.assertEquals("ERROR in return value", sterr, Response.Status.BAD_REQUEST);
    }

    @Test
    public void testExecuteSet() {
	Response.Status et = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	msgSent = null;
	try {
	    et = Response.Status.fromStatusCode(clock.executeSet("actualtime", "").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "SCE", msgSent.substring(0, 3));
	Assert.assertEquals("ERROR in return value", et, Response.Status.OK);
    }
}
