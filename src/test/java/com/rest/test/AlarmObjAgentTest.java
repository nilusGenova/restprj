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
import com.rest.hal9000.AlarmObjAgent;
import com.rest.hal9000.AlarmObjAttributes;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({ AlarmObjAgent.class })
public class AlarmObjAgentTest {

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
    private final AlarmObjAgent alarm = new AlarmObjAgent("alarm", (s) -> sendMsg(s));

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
    }

    private AlarmObjAttributes extractAttributeValue() {
	try {
	    ObjectMapper mapper = new ObjectMapper();
	    return mapper.readValue((String) alarm.exposeJsonData().getEntity(), AlarmObjAttributes.class);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }

    @Test
    public void testParseGetAnswerMode() {
	// M mode = (Armed:[0-1])(Alarm:[0-1])(Prg:[0-1])
	emptySentMsg();
	alarm.parseGetAnswer('M', "001");
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getArmed());
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getAlarmed());
	Assert.assertEquals("ERROR:", 1, extractAttributeValue().getKeyProgramming());
	alarm.parseGetAnswer('M', "1");
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getArmed());
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getAlarmed());
	Assert.assertEquals("ERROR:", 1, extractAttributeValue().getKeyProgramming());
	alarm.parseGetAnswer('M', "010");
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getArmed());
	Assert.assertEquals("ERROR:", 1, extractAttributeValue().getAlarmed());
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getKeyProgramming());
	alarm.parseGetAnswer('M', "10");
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getArmed());
	Assert.assertEquals("ERROR:", 1, extractAttributeValue().getAlarmed());
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getKeyProgramming());
	alarm.parseGetAnswer('M', "100");
	Assert.assertEquals("ERROR:", 1, extractAttributeValue().getArmed());
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getAlarmed());
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getKeyProgramming());
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testParseGetAnswerKeysPinsDelete() {
	emptySentMsg();
	// K key = (idxKey 0:master)(value 8 chars).(......).
	alarm.parseGetAnswer('K', "0:07345196.1:91234567.5:00001234.7:36.");
	Assert.assertEquals("ERROR:", 7345196, extractAttributeValue().getMasterKey());
	Assert.assertFalse("ERROR:", extractAttributeValue().keyExists(42));
	Assert.assertTrue("ERROR:", extractAttributeValue().keyExists(91234567));
	Assert.assertTrue("ERROR:", extractAttributeValue().keyExists(1234));
	Assert.assertTrue("ERROR:", extractAttributeValue().keyExists(36));
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getPin(1 - 1));
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getPin(5 - 1));
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getPin(7 - 1));
	Assert.assertEquals("ERROR:", -1, extractAttributeValue().getPin(2 - 1));
	// P pin = (idxKey 1:)(value 8 chars);(......)
	alarm.parseGetAnswer('P', "1:00087892.3:8475.7:4026597376.");  
	Assert.assertEquals("ERROR:", 87892, extractAttributeValue().getPin(1 - 1));
	Assert.assertEquals("ERROR:", 8475, extractAttributeValue().getPin(3 - 1));
	Assert.assertEquals("ERROR:", 0xf0010000L, extractAttributeValue().getPin(7 - 1));  

	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getPin(5 - 1));
	Assert.assertEquals("ERROR:", -1, extractAttributeValue().getPin(2 - 1));
	Assert.assertTrue("ERROR:", noMsgSent());
	// Delete
	emptySentMsg();
	Response.Status et = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	try {
	    et = Response.Status.fromStatusCode(alarm.deleteData("key", "1234").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "RAK1234", getSentMsg());
	Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testExposeJsonAttribute() {
	emptySentMsg();
	alarm.parseGetAnswer('M', "001");
	String et = null;
	String err = null;
	Response r = null;
	Response.Status st = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	Response.Status sterr = Response.Status.HTTP_VERSION_NOT_SUPPORTED;
	try {
	    r = alarm.exposeJsonAttribute("mode");
	    et = (String) r.getEntity();
	    st = Response.Status.fromStatusCode(r.getStatus());
	    r = alarm.exposeJsonAttribute("paperino");
	    err = (String) r.getEntity();
	    sterr = Response.Status.fromStatusCode(r.getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "001", et);
	Assert.assertNull("ERROR:", err);
	Assert.assertEquals("ERROR in return value", Response.Status.OK, st);
	Assert.assertEquals("ERROR in return value", Response.Status.BAD_REQUEST, sterr);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testParseEventMode() {
	emptySentMsg();
	// M mode = (Armed:[0-1])(Alarm:[0-1])(Prg:[0-1])
	alarm.parseEvent('M', "001");
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getArmed());
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getAlarmed());
	Assert.assertEquals("ERROR:", 1, extractAttributeValue().getKeyProgramming());
	alarm.parseEvent('M', "1");
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getArmed());
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getAlarmed());
	Assert.assertEquals("ERROR:", 1, extractAttributeValue().getKeyProgramming());
	alarm.parseEvent('M', "010");
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getArmed());
	Assert.assertEquals("ERROR:", 1, extractAttributeValue().getAlarmed());
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getKeyProgramming());
	alarm.parseEvent('M', "10");
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getArmed());
	Assert.assertEquals("ERROR:", 1, extractAttributeValue().getAlarmed());
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getKeyProgramming());
	alarm.parseEvent('M', "100");
	Assert.assertEquals("ERROR:", 1, extractAttributeValue().getArmed());
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getAlarmed());
	Assert.assertEquals("ERROR:", 0, extractAttributeValue().getKeyProgramming());
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testParseEvent() {
	emptySentMsg();
	// K N
	alarm.parseEvent('K', "");
	Assert.assertEquals("ERROR:", "GAK", getSentMsg());
	Assert.assertEquals("ERROR:", "GAP", getSentMsg());
	alarm.parseEvent('N', "");
	Assert.assertEquals("ERROR:", "GAK", getSentMsg());
	Assert.assertEquals("ERROR:", "GAP", getSentMsg());
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testCreateData() {
	emptySentMsg();
	Response.Status et = Response.Status.BAD_REQUEST;
	emptySentMsg();
	try {
	    et = Response.Status.fromStatusCode(alarm.executeSet("newkey", "128").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "SAK128", getSentMsg());
	Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testCreateDataWrong0() {
	emptySentMsg();
	Response.Status er = Response.Status.BAD_REQUEST;
	emptySentMsg();
	try {
	    er = Response.Status.fromStatusCode(alarm.executeSet("newkey", "0").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR in return value", Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE, er);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testCreateDataWrongEmpty() {
	emptySentMsg();
	Response.Status er2 = Response.Status.BAD_REQUEST;
	emptySentMsg();
	try {
	    er2 = Response.Status.fromStatusCode(alarm.executeSet("newkey", "").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR in return value", Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE, er2);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testDeleteWrongKey0() {
	emptySentMsg();
	Response.Status er = Response.Status.BAD_REQUEST;
	try {
	    er = Response.Status.fromStatusCode(alarm.deleteData("key", "0").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR in return value", Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE, er);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testDeleteWrongKeyEmpty() {
	emptySentMsg();
	Response.Status er = Response.Status.BAD_REQUEST;
	try {
	    er = Response.Status.fromStatusCode(alarm.deleteData("key", "").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR in return value", Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE, er);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testDeleteWrong() {
	emptySentMsg();
	int exc = 0;
	Response.Status er = Response.Status.BAD_REQUEST;
	try {
	    er = Response.Status.fromStatusCode(alarm.deleteData("mucca", "").getStatus());
	} catch (Exception e) {
	    exc = 1;
	}
	Assert.assertEquals("ERROR", 1, exc);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testDeleteAllKey() {
	emptySentMsg();
	Response.Status et = Response.Status.BAD_REQUEST;
	try {
	    et = Response.Status.fromStatusCode(alarm.deleteData("allkeys", "").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "RAX", getSentMsg());
	Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testDeleteAllPins() {
	emptySentMsg();
	Response.Status et = Response.Status.BAD_REQUEST;
	try {
	    et = Response.Status.fromStatusCode(alarm.deleteData("allpins", "").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "RAP", getSentMsg());
	Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testExecuteSetWrong() {
	Response.Status et = Response.Status.BAD_REQUEST;
	emptySentMsg();
	int exc = 0;
	try {
	    et = Response.Status.fromStatusCode(alarm.executeSet("piripicchio", "").getStatus());
	} catch (Exception e) {
	    exc = 1;
	}
	Assert.assertEquals("ERROR", 1, exc);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    public void testExecuteSetAttr(final String attr, final String val, final String expMsg, Response.Status retVal) {
	Response.Status et = Response.Status.BAD_REQUEST;
	emptySentMsg();
	try {
	    et = Response.Status.fromStatusCode(alarm.executeSet(attr, val).getStatus());
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
    public void testExecuteSetSimples() {
	testExecuteSetAttr("armed", "3", "", Response.Status.BAD_REQUEST);
	testExecuteSetAttr("alarm", "3", "", Response.Status.BAD_REQUEST);
	testExecuteSetAttr("program", "3", "", Response.Status.BAD_REQUEST);
	testExecuteSetAttr("campower", "3", "", Response.Status.BAD_REQUEST);
	testExecuteSetAttr("autocampower", "3", "", Response.Status.BAD_REQUEST);
    }

    @Test
    public void testExecuteSetArmed() {
	Response.Status et = Response.Status.BAD_REQUEST;
	emptySentMsg();
	alarm.parseGetAnswer('M', "000");
	try {
	    et = Response.Status.fromStatusCode(alarm.executeSet("armed", "1").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "SAM100", getSentMsg());
	Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testExecuteSetAlarm() {
	Response.Status et = Response.Status.BAD_REQUEST;
	emptySentMsg();
	alarm.parseGetAnswer('M', "000");
	try {
	    et = Response.Status.fromStatusCode(alarm.executeSet("alarm", "1").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "SAM010", getSentMsg());
	Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testExecuteSetProg() {
	Response.Status et = Response.Status.BAD_REQUEST;
	emptySentMsg();
	alarm.parseGetAnswer('M', "000");
	try {
	    et = Response.Status.fromStatusCode(alarm.executeSet("program", "1").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "SAM001", getSentMsg());
	Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testExecuteSetMasterKey() {
	Response.Status et = Response.Status.BAD_REQUEST;
	emptySentMsg();
	try {
	    et = Response.Status.fromStatusCode(alarm.executeSet("masterkey", "34921").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "SAX34921", getSentMsg());
	Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testExecuteSetMasterKeyWrong() {
	Response.Status et = Response.Status.BAD_REQUEST;
	emptySentMsg();
	try {
	    et = Response.Status.fromStatusCode(alarm.executeSet("masterkey", "").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR in return value", Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE, et);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testExecuteSetEnterPin() {
	Response.Status et = Response.Status.BAD_REQUEST;
	emptySentMsg();
	try {
	    et = Response.Status.fromStatusCode(alarm.executeSet("enterpin", "4").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "SAP4", getSentMsg());
	Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testExecuteSetEnterWrongPin() {
	Response.Status et = Response.Status.BAD_REQUEST;
	emptySentMsg();
	try {
	    et = Response.Status.fromStatusCode(alarm.executeSet("enterpin", "10").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR in return value", Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE, et);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

    @Test
    public void testExecuteSetEnterEmptyPin() {
	Response.Status et = Response.Status.BAD_REQUEST;
	emptySentMsg();
	try {
	    et = Response.Status.fromStatusCode(alarm.executeSet("enterpin", "").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR in return value", Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE, et);
	Assert.assertTrue("ERROR:", noMsgSent());
    }
    
    @Test
    public void testExecuteSetCamPowerOn() {
	Response.Status et = Response.Status.BAD_REQUEST;
	emptySentMsg();
	alarm.parseGetAnswer('C', "0");
	try {
	    et = Response.Status.fromStatusCode(alarm.executeSet("campower", "1").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "SAC1", getSentMsg());
	Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	Assert.assertTrue("ERROR:", noMsgSent());
    }
    
    @Test
    public void testExecuteSetCamPowerOff() {
	Response.Status et = Response.Status.BAD_REQUEST;
	emptySentMsg();
	alarm.parseGetAnswer('C', "1");
	try {
	    et = Response.Status.fromStatusCode(alarm.executeSet("campower", "0").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "SAC0", getSentMsg());
	Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	Assert.assertTrue("ERROR:", noMsgSent());
    }
    
    @Test
    public void testExecuteSetAutoCamPowerOn() {
	Response.Status et = Response.Status.BAD_REQUEST;
	emptySentMsg();
	alarm.parseGetAnswer('A', "0");
	try {
	    et = Response.Status.fromStatusCode(alarm.executeSet("autocampower", "1").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "SAA1", getSentMsg());
	Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	Assert.assertTrue("ERROR:", noMsgSent());
    }
    
    @Test
    public void testExecuteSetAutoCamPowerOff() {
	Response.Status et = Response.Status.BAD_REQUEST;
	emptySentMsg();
	alarm.parseGetAnswer('A', "1");
	try {
	    et = Response.Status.fromStatusCode(alarm.executeSet("autocampower", "0").getStatus());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("exception");
	}
	Assert.assertEquals("ERROR:", "SAA0", getSentMsg());
	Assert.assertEquals("ERROR in return value", Response.Status.OK, et);
	Assert.assertTrue("ERROR:", noMsgSent());
    }

}
