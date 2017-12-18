package com.rest.hal9000;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheRefreshBlockingManager {

    protected static final Logger log = LoggerFactory.getLogger(CacheRefreshBlockingManager.class);

    private Runnable updateCallBack;
    private final long refresh_period_ms;
    private final long wait_timeout;

    private volatile long lastUpdateTime = 0;

    private Lock updateLock = new ReentrantLock();
    private final Condition isUpdated = updateLock.newCondition();

    public CacheRefreshBlockingManager(Runnable updateCallBack, long refresh_period_ms, long wait_timeout) {
	this.updateCallBack = updateCallBack;
	this.refresh_period_ms = refresh_period_ms;
	this.wait_timeout = wait_timeout;
    }
    
    public CacheRefreshBlockingManager(long refresh_period_ms, long wait_timeout) {
	this.refresh_period_ms = refresh_period_ms;
	this.wait_timeout = wait_timeout;
    }
    
    public void assignCallBack(Runnable updateCallBack) {
	this.updateCallBack = updateCallBack;
    }

    public void updateCompleted() {
	updateLock.lock();
	lastUpdateTime = Calendar.getInstance().getTimeInMillis();
	isUpdated.signalAll();
	updateLock.unlock();
    }

    public void requestForUpdate() {
	final long actualTime = Calendar.getInstance().getTimeInMillis();
	updateLock.lock();
	if ((actualTime - lastUpdateTime) > refresh_period_ms) {
	    log.debug("Force realignment");
	    try {
		updateCallBack.run();
		if (!isUpdated.await(wait_timeout, TimeUnit.MILLISECONDS)) {
		    log.error("requestForUpdate: timeout caused update failure");
		}
	    } catch (InterruptedException e) {
		log.error("requestForUpdate: interrupted!");
		e.printStackTrace();
	    }
	}
	updateLock.unlock();
    }
}
