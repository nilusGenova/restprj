package com.rest.hal9000;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class CacheRefreshBlockingManager extends CacheRefreshManager {

    protected final long wait_timeout;

    protected final Condition isUpdated = updateLock.newCondition();

    public CacheRefreshBlockingManager(Runnable updateCallBack, long refresh_period_ms, long wait_timeout) {
	super(updateCallBack, refresh_period_ms);
	this.wait_timeout = wait_timeout;
    }

    protected CacheRefreshBlockingManager(long refresh_period_ms, long wait_timeout) {
	super(refresh_period_ms);
	this.wait_timeout = wait_timeout;
    }

    @Override
    public void updateCache() {
	updateLock.lock();
	updateWithCallBack();
	isUpdated.signalAll();
	updateLock.unlock();
    }

    public void updateCompleted() {
	updateLock.lock();
	update();
	isUpdated.signalAll();
	updateLock.unlock();
    }

    @Override
    public void requestForUpdate() {
	final long actualTime = Calendar.getInstance().getTimeInMillis();
	updateLock.lock();
	if ((actualTime - lastUpdateTime) > refresh_period_ms) {
	    log.debug("Force realignment");
	    try {
		updateWithCallBack();
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
