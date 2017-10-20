package com.rest.test;

import static org.junit.Assert.fail;

import java.util.ArrayList;

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
	emptySentMsg();
	clock.parseGetAnswer('D', "14-02-1967");
	clock.parseGetAnswer('T', "18:07");
	clock.parseGetAnswer('W', "3");
	Assert.assertEquals("ERROR:", "14-02-1967", extractAttributeValue().getDate());
	Assert.assertEquals("ERROR:", "18:07", extractAttributeValue().getTime());
	Assert.assertEquals("ERROR:", 3, extractAttributeValue().getWeekDay());
	clock.parseGetAnswer('E', "7623412");
	Assert.assertEquals("ERROR:", "7623412", extractAttributeValue().getEpochTime());
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testParseEvent() {
	emptySentMsg();
	// D forza il riallineamento
	clock.parseEvent('D', "");
	Assert.assertEquals("ERROR:", "GCE", getSentMsg());
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testExposeJsonAttribute() {
	emptySentMsg();
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
	Assert.assertEquals("ERROR in return value", Response.Status.OK, st);
	Assert.assertEquals("ERROR in return value", Response.Status.BAD_REQUEST, sterr);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testExecuteSetActualtime() {
	emptySentMsg();
	Response.Status et = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	try {
	    et = Response.Status.fromStatusCode(clock.executeSet("actualtime", "").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "SCE", getSentMsg().substring(0, 3));
	Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testExecuteSetKnewTime() {
	emptySentMsg();
	Response.Status et = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	try {
	    et = Response.Status.fromStatusCode(clock.executeSet("time", "0:23-12-12-17").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "SCE566353380", getSentMsg());
	Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testExecuteSetTime() {
	// v=<hour>:<min>-<day>-<month>-<year>
	final String[] DATES_OK = { "4:0-12-12-17", "4:23-12-12-17", "4:23-12-12-2017", "04:23-12-12-2017",
		"14:2-12-12-2017", "14:21-1-12-2017", "14:21-1-2-2017", "4:9-1-2-17" };
	for (int i = 0; i < DATES_OK.length; i++) {
	    emptySentMsg();
	    Response.Status et = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	    try {
		et = Response.Status.fromStatusCode(clock.executeSet("time", DATES_OK[i]).getStatus());
	    } catch (Exception e) {
		e.printStackTrace();
		fail("exception");
	    }
	    Assert.assertEquals("ERROR:", "SCE", getSentMsg().substring(0, 3));
	    Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	    Assert.assertTrue("ERROR:", noMsgSent());
	}
    }

    @Test
    public void testExecuteSetWrongTime() {
	// v=<hour>:<min>-<day>-<month>-<year>
	final String[] DATES_WRONG = { "4:23-12-13-17", "40:23-12-12-2017", "04:23-31-11-2017", "14-12-12-2017",
		"14:21-1-12", "14:21-1-0-2017", "4:9:32-1-2-17", "4:9:32-1-2-17", "" };
	for (int i = 0; i < DATES_WRONG.length; i++) {
	    emptySentMsg();
	    Response.Status et = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	    try {
		et = Response.Status.fromStatusCode(clock.executeSet("time", DATES_WRONG[i]).getStatus());
	    } catch (Exception e) {
		e.printStackTrace();
		fail("exception");
	    }
	    Assert.assertEquals("ERROR in return value", Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE, et);
	    Assert.assertTrue("ERROR:", noMsgSent());
	}
    }

    @Test
    public void testExecuteSetWrong() {
	Response.Status et = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	emptySentMsg();
	int exc = 0;
	try {
	    et = Response.Status.fromStatusCode(clock.executeSet("citrullo", "").getStatus());
	} catch (Exception e) {
	    exc = 1;
	}
	Assert.assertEquals("ERROR", 1, exc);
	Assert.assertTrue("ERROR:", noMsgSent());
    }
}
