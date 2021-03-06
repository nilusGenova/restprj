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

    private final static int TIMER_INIT_VAL = 10;

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
	clock.setNtpPriority(false);
	clock.setRtcCheck(true);
	clock.parseGetAnswer('D', "14-02-1967");
	clock.parseGetAnswer('T', "18:07");
	clock.parseGetAnswer('W', "3");
	clock.parseGetAnswer('R', "0");
	Assert.assertEquals("ERROR:", "14-02-1967", extractAttributeValue().getDate());
	Assert.assertEquals("ERROR:", "18:07", extractAttributeValue().getTime());
	Assert.assertEquals("ERROR:", 3, extractAttributeValue().getWeekDay());
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getRtcError());
	clock.parseGetAnswer('E', "7623412");
	Assert.assertEquals("ERROR:", "7623412", extractAttributeValue().getEpochTime());
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testTimerValueandTicks() {
	Response.Status et = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	Response r = null;
	clock.setNtpPriority(false);
	try {
	    r = clock.exposeJsonAttribute("timer");
	    Assert.assertEquals("ERROR in timer get", Integer.toString(TIMER_INIT_VAL), (String) r.getEntity());
	    emptySentMsg();
	    for (int i = 0; i < TIMER_INIT_VAL - 1; i++) {
		clock.one_min_tick();
		Assert.assertTrue("ERROR on tick:" + i, noMsgSent());
	    }
	    clock.one_min_tick();
	    Assert.assertEquals("ERROR on timer:", "GCE", getSentMsg());
	    clock.one_min_tick();
	    Assert.assertTrue("ERROR on last tick:", noMsgSent());
	    et = Response.Status
		    .fromStatusCode(clock.executeSet("timer", Integer.toString(TIMER_INIT_VAL * 3)).getStatus());
	    r = clock.exposeJsonAttribute("timer");
	    Assert.assertEquals("ERROR in timer get/set", Integer.toString(TIMER_INIT_VAL * 3), (String) r.getEntity());
	    Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	    emptySentMsg();
	    for (int i = 0; i < (TIMER_INIT_VAL * 3) - 1; i++) {
		clock.one_min_tick();
		Assert.assertTrue("ERROR on tick:" + i, noMsgSent());
	    }
	    clock.one_min_tick();
	    Assert.assertEquals("ERROR on timer:", "GCE", getSentMsg());
	    clock.one_min_tick();
	    Assert.assertTrue("ERROR on last tick:", noMsgSent());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}

    }

    @Test
    public void testParseEvent() {
	emptySentMsg();
	clock.setNtpPriority(false);
	clock.setRtcCheck(true);
	// D forza il riallineamento
	clock.parseEvent('D', "");
	Assert.assertEquals("ERROR:", "GCE", getSentMsg());
	Assert.assertEquals("ERROR:", "GCR", getSentMsg());
	Assert.assertTrue("ERROR:", noMsgSent());
	try {
	    clock.executeSet("rtcchecks", "0");
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	clock.parseEvent('D', "");
	Assert.assertEquals("ERROR:", "GCE", getSentMsg());
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testExposeJsonAttribute() {
	emptySentMsg();
	clock.setNtpPriority(false);
	clock.setRtcCheck(true);
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
    public void testSetGetNtpFlag() {
	Response.Status et = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	Response r = null;
	clock.setNtpPriority(false);
	try {
	    r = clock.exposeJsonAttribute("ntp_priority");
	    Assert.assertEquals("ERROR in ntp_priority get", "0", (String) r.getEntity());
	    et = Response.Status.fromStatusCode(clock.executeSet("ntp_priority", "1").getStatus());
	    r = clock.exposeJsonAttribute("ntp_priority");
	    Assert.assertEquals("ERROR in ntp_priority get/set", "1", (String) r.getEntity());
	    Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
    }

    @Test
    public void testSetGetRtcCheckFlag() {
	Response.Status et = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	Response r = null;
	try {
	    clock.setRtcCheck(true);
	    r = clock.exposeJsonAttribute("rtcchecks");
	    Assert.assertEquals("ERROR in rtcCheck get", "1", (String) r.getEntity());
	    et = Response.Status.fromStatusCode(clock.executeSet("rtcchecks", "0").getStatus());
	    r = clock.exposeJsonAttribute("rtcchecks");
	    Assert.assertEquals("ERROR in rtcCheck get/set", "0", (String) r.getEntity());
	    Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
    }

    @Test
    public void testNtpCorrection() {
	emptySentMsg();
	clock.setNtpPriority(false);
	clock.setRtcCheck(true);
	try {
	    Response.Status.fromStatusCode(clock.executeSet("ntp_priority", "1").getStatus());
	} catch (Exception e1) {
	    e1.printStackTrace();
	    fail("exception");
	}
	clock.parseGetAnswer('E', "566353380");
	Assert.assertEquals("ERROR:", "SCE", getSentMsg().substring(0, 3));
	Assert.assertTrue("ERROR:", noMsgSent());
	try {
	    Response.Status.fromStatusCode(clock.executeSet("ntp_priority", "0").getStatus());
	} catch (Exception e1) {
	    e1.printStackTrace();
	    fail("exception");
	}
    }

    @Test
    public void testExecuteSetActualtime() {
	emptySentMsg();
	clock.setNtpPriority(false);
	clock.setRtcCheck(true);
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
	clock.setNtpPriority(false);
	clock.setRtcCheck(true);
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
	clock.setNtpPriority(false);
	clock.setRtcCheck(true);
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
	clock.setNtpPriority(false);
	clock.setRtcCheck(true);
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
	clock.setNtpPriority(false);
	clock.setRtcCheck(true);
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
