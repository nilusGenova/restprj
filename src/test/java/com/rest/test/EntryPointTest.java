package com.rest.test;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyChar;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.rest.hal9000.App;
import com.rest.hal9000.EntryPoint;
import com.rest.hal9000.HalObjAgent;
import com.rest.hal9000.Registry;

//@RunWith(MockitoJUnitRunner.class)
@RunWith(PowerMockRunner.class)
@PrepareForTest({ EntryPoint.class, App.class, Registry.class })
public class EntryPointTest {

    @Mock
    private App app;

    @Mock
    private Registry reg;

    @Mock
    private HalObjAgent objA;

    @Mock
    private HalObjAgent objB;

    @Mock
    private HalObjAgent objC;

    @InjectMocks
    private final EntryPoint server = new EntryPoint();

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetOnObject() {
	PowerMockito.mockStatic(App.class);
	Field field = PowerMockito.field(App.class, "registry");
	try {
	    field.set(App.class, reg);
	} catch (IllegalArgumentException e1) {
	    e1.printStackTrace();
	} catch (IllegalAccessException e1) {
	    e1.printStackTrace();
	}
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
	PowerMockito.mockStatic(App.class);
	Field field = PowerMockito.field(App.class, "registry");
	try {
	    field.set(App.class, reg);
	} catch (IllegalArgumentException e1) {
	    e1.printStackTrace();
	} catch (IllegalAccessException e1) {
	    e1.printStackTrace();
	}
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
	PowerMockito.mockStatic(App.class);
	Field field = PowerMockito.field(App.class, "registry");
	try {
	    field.set(App.class, reg);
	} catch (IllegalArgumentException e1) {
	    e1.printStackTrace();
	} catch (IllegalAccessException e1) {
	    e1.printStackTrace();
	}
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
}
