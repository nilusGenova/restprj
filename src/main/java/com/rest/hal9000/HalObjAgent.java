package com.rest.hal9000;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class HalObjAgent {

    protected static final Logger log = LoggerFactory.getLogger(HalObjAgent.class);

    private final char id; // id used on hal9000 MUST be the first letter of pathName

    private final String pathName; // name used to identify obj in the REST call

    private ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Consumer<String> sendMsgCallBack;

    public HalObjAgent(String pathName, Consumer<String> sendMsgCallBack) {
	super();
	this.id = Character.toUpperCase(pathName.charAt(0));
	this.pathName = pathName;
	this.sendMsgCallBack = sendMsgCallBack;
    }

    public char getId() {
	return id;
    }

    public String getPathName() {
	return pathName;
    }

    protected abstract Object getExposedData();

    protected abstract String getExposedAttribute(String attr) throws Exception;

    protected abstract void specializedParseGetAnswer(char attribute, String msg);

    protected abstract void specializedParseEvent(char event, String msg);

    public abstract void alignAll();

    public void parseGetAnswer(char attribute, String msg) {
	synchWrite(new Callable<Boolean>() {
	    public Boolean call() throws Exception {
		specializedParseGetAnswer(attribute, msg);
		return true;
	    }
	});
    }

    public void parseEvent(char event, String msg) {
	synchWrite(new Callable<Boolean>() {
	    public Boolean call() throws Exception {
		specializedParseEvent(event, msg);
		return true;
	    }
	});
    }

    public Response exposeJsonData() throws Exception {
	ObjectMapper mapper = new ObjectMapper();
	mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
	mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

	String jsonInString = synchRead(new Callable<String>() {
	    public String call() throws Exception {
		return mapper.writeValueAsString(getExposedData());
	    }
	});

	log.debug("exposed json:{}", jsonInString);
	return Response.ok(jsonInString, MediaType.APPLICATION_JSON).build();
    }

    public Response exposeJsonAttribute(String attr) throws Exception {

	String value = synchRead(new Callable<String>() {
	    public String call() throws Exception {
		return getExposedAttribute(attr);
	    }
	});
	if (value == null) {
	    return Response.status(Response.Status.BAD_REQUEST).build();
	} else {
	    log.debug("exposed json attribute {}:{}", attr, value);
	    return Response.ok(value, MediaType.TEXT_PLAIN).build();
	}
    }

    public Response executeSet(String attr, String val) throws Exception {
	throw new Exception();
    }

    public Response deleteData(String cmd, String prm) throws Exception {
	throw new Exception();
    }

    public Response createData(String cmd, String prm) throws Exception {
	throw new Exception();
    }

    protected void wrongAttribute() {
	log.error("Wrong attribute");
    }

    protected void wrongEvent() {
	log.error("Wrong event");
    }

    protected void wrongValue(int i) {
	log.error("Wrong value {}", i);
    }

    protected int getBooleanVal(String val) throws Exception {
	if ("0".equals(val)) {
	    return 0;
	}
	if ("1".equals(val)) {
	    return 1;
	} else {
	    throw new Exception();
	}
    }

    protected void sendMsgToHal(String msg) {
	this.sendMsgCallBack.accept(msg);
    }

    @SuppressWarnings("finally")
    protected <T> T synchWrite(Callable<T> func) {
	T retVal = null;
	lock.writeLock().lock();
	try {
	    retVal = func.call();
	} finally {
	    lock.writeLock().unlock();
	    return retVal;
	}
    }

    @SuppressWarnings("finally")
    protected <T> T synchRead(Callable<T> func) {
	T retVal = null;
	lock.readLock().lock();
	try {
	    retVal = func.call();
	} finally {
	    lock.readLock().unlock();
	    return retVal;
	}
    }
}
