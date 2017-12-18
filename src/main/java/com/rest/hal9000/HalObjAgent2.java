package com.rest.hal9000;

import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class HalObjAgent2 {

    protected static final Logger log = LoggerFactory.getLogger(HalObjAgent2.class);

    private final char id; // id used on hal9000 MUST be the first letter of pathName

    private final String pathName; // name used to identify obj in the REST call

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final Consumer<String> sendMsgCallBack;

    private volatile long lastUpdateTime = 0;

    private Lock updateLock = new ReentrantLock();
    private final Condition isUpdated = updateLock.newCondition();

    private static final long OBJ_REFRESH_TIMEOUT = (1000 * 10); // in milliseconds
    private static final long OBJ_WAIT_REFRESH_TIMEOUT = (1000 * 5); // in milliseconds

    public HalObjAgent2(final String pathName, final Consumer<String> sendMsgCallBack) {
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

    protected abstract String getExposedAttribute(final String attr) throws Exception;

    protected abstract boolean specializedParseGetAnswer(final char attribute, final String msg);

    protected abstract boolean specializedParseEvent(final char event, final String msg);

    public abstract void alignAll();

    public void parseGetAnswer(final char attribute, final String msg) {
	synchWrite(new Callable<Boolean>() {
	    public Boolean call() throws Exception {
		if (specializedParseGetAnswer(attribute, msg)) {
		    updateLastUpdTime();
		}
		return true;
	    }
	});
    }

    public void parseEvent(final char event, final String msg) {
	synchWrite(new Callable<Boolean>() {
	    public Boolean call() throws Exception {
		if (specializedParseEvent(event, msg)) {
		    updateLastUpdTime();
		}
		return true;
	    }
	});
    }

    public Response exposeJsonData() throws Exception {
	ObjectMapper mapper = new ObjectMapper();
	mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
	mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

	tryToUpdateIfRequired();
	String jsonInString = synchRead(new Callable<String>() {
	    public String call() throws Exception {
		return mapper.writeValueAsString(getExposedData());
	    }
	});

	log.debug("exposed json:{}", jsonInString);
	return Response.ok(jsonInString, MediaType.APPLICATION_JSON).build();
    }

    public Response exposeJsonAttribute(final String attr) throws Exception {

	tryToUpdateIfRequired();
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

    public Response executeSet(final String attr, final String val) throws Exception {
	throw new Exception();
    }

    public Response deleteData(final String cmd, final String prm) throws Exception {
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

    protected void wrongValue(final String s) {
	log.error("Wrong value {}", s);
    }

    protected int getBooleanVal(final String val) throws Exception {
	if ("0".equals(val)) {
	    return 0;
	}
	if ("1".equals(val)) {
	    return 1;
	} else {
	    throw new Exception();
	}
    }

    protected void sendMsgToHal(final String msg) {
	this.sendMsgCallBack.accept(msg);
    }

    @SuppressWarnings("finally")
    protected <T> T synchWrite(final Callable<T> func) {
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
    protected <T> T synchRead(final Callable<T> func) {
	T retVal = null;
	lock.readLock().lock();
	try {
	    retVal = func.call();
	} finally {
	    lock.readLock().unlock();
	    return retVal;
	}
    }

    protected void updateLastUpdTime() {
	updateLock.lock();
	lastUpdateTime = Calendar.getInstance().getTimeInMillis();
	isUpdated.signalAll();
	updateLock.unlock();
    }

    protected void tryToUpdateIfRequired() {
	final long actualTime = Calendar.getInstance().getTimeInMillis();
	updateLock.lock();
	if ((actualTime - lastUpdateTime) > OBJ_REFRESH_TIMEOUT) {
	    log.debug("Force realignment for {}", pathName);
	    alignAll();
	    try {
		if (!isUpdated.await(OBJ_WAIT_REFRESH_TIMEOUT, TimeUnit.MILLISECONDS)) {
		    log.error("tryToUpdateIfRequired: object {} not refreshed", pathName);
		}
	    } catch (InterruptedException e) {
		log.error("tryToUpdateIfRequired: interrupted!");
		e.printStackTrace();
	    }
	}
	updateLock.unlock();

    }
}
