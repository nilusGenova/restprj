package com.rest.hal9000;

import java.util.concurrent.Callable;
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

    private static final long REFRESH_TIME = (1000 * 10); // in milliseconds
    private static final long WAIT_REFRESH_TIMEOUT = (1000 * 5); // in milliseconds

    protected static final Logger log = LoggerFactory.getLogger(HalObjAgent.class);

    private final char id; // id used on hal9000 MUST be the first letter of pathName

    private final String pathName; // name used to identify obj in the REST call

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final Consumer<String> sendMsgCallBack;

    private CacheRefreshBlockingManager cacheRefreshManager = new CacheRefreshBlockingManager(() -> alignAll(),
	    REFRESH_TIME, WAIT_REFRESH_TIMEOUT);

    public HalObjAgent(final String pathName, final Consumer<String> sendMsgCallBack) {
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
		    cacheRefreshManager.refreshCompleted();
		}
		return true;
	    }
	});
    }

    public void parseEvent(final char event, final String msg) {
	synchWrite(new Callable<Boolean>() {
	    public Boolean call() throws Exception {
		if (specializedParseEvent(event, msg)) {
		    cacheRefreshManager.refreshCompleted();
		}
		return true;
	    }
	});
    }

    public Response exposeJsonData() throws Exception {
	ObjectMapper mapper = new ObjectMapper();
	mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
	mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

	cacheRefreshManager.refreshIfRequired();
	String jsonInString = synchRead(new Callable<String>() {
	    public String call() throws Exception {
		return mapper.writeValueAsString(getExposedData());
	    }
	});

	log.debug("exposed json:{}", jsonInString);
	return Response.ok(jsonInString, MediaType.APPLICATION_JSON).build();
    }

    public Response exposeJsonAttribute(final String attr) throws Exception {

	cacheRefreshManager.refreshIfRequired();
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
}
