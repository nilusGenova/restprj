package com.rest.hal9000;

import java.util.Calendar;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CacheRefreshManager {

    protected static final Logger log = LoggerFactory.getLogger(CacheRefreshManager.class);

    protected Runnable updateCallBack;
    protected long refresh_period_ms;

    protected volatile long lastUpdateTime = 0;

    protected Lock updateLock = new ReentrantLock();

    public CacheRefreshManager(Runnable updateCallBack, long refresh_period_ms) {
	this.updateCallBack = updateCallBack;
	this.refresh_period_ms = refresh_period_ms;
    }

    protected CacheRefreshManager(long refresh_period_ms) {
	this.refresh_period_ms = refresh_period_ms;
    }

    protected void setCallBack(Runnable updateCallBack) {
	updateLock.lock();
	this.updateCallBack = updateCallBack;
	updateLock.unlock();
    }

    public void doRefresh() {
	updateLock.lock();
	updateWithCallBack();
	updateLock.unlock();
    }

    public void forceRefresh() {
	updateLock.lock();
	lastUpdateTime = 0;
	updateLock.unlock();
    }

    public void refreshIfRequired() {
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

    public boolean needsRefresh() {
	return ((Calendar.getInstance().getTimeInMillis() - lastUpdateTime) > refresh_period_ms);
    }

    public void setRefreshPeriod(long refresh_period_ms) {
	updateLock.lock();
	this.refresh_period_ms = refresh_period_ms;
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
