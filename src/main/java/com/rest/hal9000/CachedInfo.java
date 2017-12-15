package com.rest.hal9000;

import java.util.Calendar;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedInfo<T> {
    private T info = null;

    protected static final Logger log = LoggerFactory.getLogger(CachedInfo.class);

    private final Supplier<T> updateCallBack;
    private final long refresh_period_ms;

    private volatile long lastUpdateTime = 0;

    private Lock updateLock = new ReentrantLock();

    public CachedInfo(T initial_info, Supplier<T> updateCallBack, long refresh_period_ms) {
	this.updateCallBack = updateCallBack;
	this.refresh_period_ms = refresh_period_ms;
	updateInfo(initial_info);
    }

    public CachedInfo(Supplier<T> updateCallBack, long refresh_period_ms) {
	this.updateCallBack = updateCallBack;
	this.refresh_period_ms = refresh_period_ms;
	refreshInfoIfRequired();
    }

    public void update_info(T val) {
	updateLock.lock();
	updateInfo(val);
	updateLock.unlock();
    }

    public T getInfo() {
	refreshInfoIfRequired();
	return info;
    }

    private void updateInfo(T val) {
	info = val;
	lastUpdateTime = Calendar.getInstance().getTimeInMillis();
    }

    private void refreshInfoIfRequired() {
	final long actualTime = Calendar.getInstance().getTimeInMillis();
	updateLock.lock();
	if ((info == null) || ((actualTime - lastUpdateTime) > refresh_period_ms)) {
	    log.debug("Force realignment");
	    try {
		updateInfo(updateCallBack.get());
	    } catch (Exception e) {
		log.error("refreshInfoIfRequired: interrupted!");
		e.printStackTrace();
	    }
	}
	updateLock.unlock();
    }
}
