package com.rest.hal9000;

import java.util.Calendar;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheRefreshManager {

    protected static final Logger log = LoggerFactory.getLogger(CacheRefreshManager.class);

    protected Runnable updateCallBack;
    protected final long refresh_period_ms;

    protected volatile long lastUpdateTime = 0;

    protected Lock updateLock = new ReentrantLock();

    public CacheRefreshManager(Runnable updateCallBack, long refresh_period_ms) {
	this.updateCallBack = updateCallBack;
	this.refresh_period_ms = refresh_period_ms;
    }

    protected CacheRefreshManager(long refresh_period_ms) {
	this.refresh_period_ms = refresh_period_ms;
    }

    protected void assignCallBack(Runnable updateCallBack) {
	this.updateCallBack = updateCallBack;
    }

    public void updateCache() {
	updateLock.lock();
	updateWithCallBack();
	updateLock.unlock();
    }

    public void requestForUpdate() {
	final long actualTime = Calendar.getInstance().getTimeInMillis();
	updateLock.lock();
	if ((actualTime - lastUpdateTime) > refresh_period_ms) {
	    log.debug("Force realignment");
	    try {
		updateWithCallBack();
	    } catch (Exception e) {
		log.error("requestForUpdate: interrupted!");
		e.printStackTrace();
	    }
	}
	updateLock.unlock();
    }

    protected void updateWithCallBack() {
	if (updateCallBack != null) {
	    updateCallBack.run();
	}
	update();
    }

    protected void update() {
	lastUpdateTime = Calendar.getInstance().getTimeInMillis();
    }
}
