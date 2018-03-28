package com.rest.test;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyChar;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.rest.hal9000.App;
import com.rest.hal9000.EntryPoint;
import com.rest.hal9000.HalObjAgent;
import com.rest.hal9000.ProgramObjAgent;
import com.rest.hal9000.Registry;
import com.rest.hal9000.ThermoObjAgent;

//@RunWith(MockitoJUnitRunner.class)
@RunWith(PowerMockRunner.class)
@PrepareForTest({ EntryPoint.class, App.class, Registry.class, HttpServletRequest.class })
public class EntryPointTest {

    @Mock
    private App app;

    @Mock
    private Registry reg;

    @Mock
    private HttpServletRequest httpReq;

    @Mock
    private HalObjAgent objA;

    @Mock
    private HalObjAgent objB;

    @Mock
    private HalObjAgent objC;

    @Mock
    private ProgramObjAgent objP;

    @Mock
    private ThermoObjAgent objT;

    @InjectMocks
    private final EntryPoint server = new EntryPoint();

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
	PowerMockito.mockStatic(App.class, Mockito.CALLS_REAL_METHODS);
	PowerMockito.mockStatic(EntryPoint.class);
	Field fieldReg = PowerMockito.field(App.class, "registry");
	Field fieldHttpReq = PowerMockito.field(EntryPoint.class, "request");
	try {
	    fieldReg.set(App.class, reg);
	    fieldHttpReq.set(EntryPoint.class, httpReq);
	} catch (IllegalArgumentException e1) {
	    e1.printStackTrace();
	} catch (IllegalAccessException e1) {
	    e1.printStackTrace();
	}
    }

    @Test
    public void testInvalidAccessWith() {
	when(httpReq.getRemoteAddr()).thenReturn("192.168.0.14");
	Response resp;
	resp = server.getOnObject("mypath", null);
	Assert.assertEquals("ERROR:wrong get access test", Response.Status.FORBIDDEN, resp.getStatusInfo());
	resp = server.postOnObject("postpath", null, null);
	Assert.assertEquals("ERROR:wrong post access test", Response.Status.FORBIDDEN, resp.getStatusInfo());
	resp = server.deleteOnObject("delpath", null, null);
	Assert.assertEquals("ERROR:wrong delete access test", Response.Status.FORBIDDEN, resp.getStatusInfo());
    }

    @Test
    public void testGetOnObject() {
	when(httpReq.getRemoteAddr()).thenReturn("127.0.0.1");
	when(reg.getRegisteredObj(anyChar())).thenReturn(objA);
	when(objA.getPathName()).thenReturn("mypath");

	server.getOnObject("connected", null);
	try {
	    verify(objA, never()).exposeJsonData();
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception");
	}

	server.getOnObject("mypath", null);
	try {
	    verify(objA, times(1)).exposeJsonData();
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception");
	}

	server.getOnObject("mypath", "ATTRname");
	try {
	    verify(objA, times(1)).exposeJsonAttribute("attrname");
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception");
	}

	server.getOnObject("mypath", "");
	try {
	    verify(objA, times(1)).exposeJsonAttribute("");
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception");
	}
    }

    @Test
    public void testPostOnObject() {
	when(httpReq.getRemoteAddr()).thenReturn("127.0.0.1");
	when(reg.getRegisteredObj(anyChar())).thenReturn(objB);
	when(objB.getPathName()).thenReturn("postpath");

	server.postOnObject("postpath", null, null);
	try {
	    verify(objB, never()).executeSet(anyString(), anyString());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception");
	}

	server.postOnObject("postpath", "ATTRname", "value");
	try {
	    verify(objB, times(1)).executeSet("attrname", "value");
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception");
	}

	server.postOnObject("postpath", "ATTRname", "");
	try {
	    verify(objB, times(1)).executeSet("attrname", "");
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception");
	}

	server.postOnObject("postpath", "ATTRname", null);
	try {
	    verify(objB, times(2)).executeSet("attrname", "");
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception");
	}

    }

    @Test
    public void testDeleteOnObject() {
	when(httpReq.getRemoteAddr()).thenReturn("127.0.0.1");
	when(reg.getRegisteredObj(anyChar())).thenReturn(objC);
	when(objC.getPathName()).thenReturn("delpath");

	server.deleteOnObject("delpath", null, null);
	try {
	    verify(objC, never()).deleteData(anyString(), anyString());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception");
	}

	server.deleteOnObject("delpath", "CMDname", null);
	try {
	    verify(objC, times(1)).deleteData("cmdname", "");
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception");
	}

	server.deleteOnObject("delpath", "CMDname", "");
	try {
	    verify(objC, times(2)).deleteData("cmdname", "");
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception");
	}

	server.deleteOnObject("delpath", "CMDname", "prm");
	try {
	    verify(objC, times(1)).deleteData("cmdname", "prm");
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception");
	}
    }

    @Test
    public void testChangeTempModeOff() {
	when(httpReq.getRemoteAddr()).thenReturn("127.0.0.1");
	when(reg.getRegisteredObj('P')).thenReturn(objP);
	when(reg.getRegisteredObj('T')).thenReturn(objT);

	when(objP.getMode()).thenReturn("OFF");
	try {
	    server.changeTemp("100");
	    verify(objP, never()).executeSet(anyString(), anyString());
	    verify(objT, never()).executeSet(anyString(), anyString());
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception");
	}
    }

    @Test
    public void testChangeTempModeAuto() {
	when(httpReq.getRemoteAddr()).thenReturn("127.0.0.1");
	when(reg.getRegisteredObj('P')).thenReturn(objP);
	when(reg.getRegisteredObj('T')).thenReturn(objT);

	when(objP.getMode()).thenReturn("AUTO");
	try {
	    // PowerMockito.doCallRealMethod().when(App.class, "setTemp", "String");
	    server.changeTemp("100");
	    verify(objP, never()).executeSet(anyString(), anyString());
	    verify(objT, times(1)).executeSet("required", "100");
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception:" + e.getMessage());
	}
    }

    @Test
    public void testChangeTempModeSpc() {
	when(httpReq.getRemoteAddr()).thenReturn("127.0.0.1");
	when(reg.getRegisteredObj('P')).thenReturn(objP);
	when(reg.getRegisteredObj('T')).thenReturn(objT);

	when(objP.getMode()).thenReturn("SPECIAL");
	try {
	    server.changeTemp("100");
	    verify(objP, never()).executeSet(anyString(), anyString());
	    verify(objT, times(1)).executeSet("required", "100");
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception:" + e.getMessage());
	}
    }

    @Test
    public void testChangeTempModeManOff() {
	when(httpReq.getRemoteAddr()).thenReturn("127.0.0.1");
	when(reg.getRegisteredObj('P')).thenReturn(objP);
	when(reg.getRegisteredObj('T')).thenReturn(objT);

	when(objP.getMode()).thenReturn("MAN_OFF");
	try {
	    server.changeTemp("100");
	    verify(objT, never()).executeSet(anyString(), anyString());
	    verify(objP, times(1)).executeSet("temp_off", "100");
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception:" + e.getMessage());
	}
    }

    @Test
    public void testChangeTempModeManOn() {
	when(httpReq.getRemoteAddr()).thenReturn("127.0.0.1");
	when(reg.getRegisteredObj('P')).thenReturn(objP);
	when(reg.getRegisteredObj('T')).thenReturn(objT);

	when(objP.getMode()).thenReturn("MAN_ON");
	try {
	    server.changeTemp("100");
	    verify(objT, never()).executeSet(anyString(), anyString());
	    verify(objP, times(1)).executeSet("temp_on", "100");
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception:" + e.getMessage());
	}
    }

}
