package com.rest.hal9000;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class CacheRefreshBlockingManager extends CacheRefreshManager {

    protected final long wait_timeout;

    protected final Condition isUpdated = updateLock.newCondition();

    public CacheRefreshBlockingManager(Runnable updateCallBack, long refresh_period_ms, long wait_timeout,
	    final String debug_name) {
	super(updateCallBack, refresh_period_ms, debug_name);
	this.wait_timeout = wait_timeout;
    }

    protected CacheRefreshBlockingManager(long refresh_period_ms, long wait_timeout, final String debug_name) {
	super(refresh_period_ms, debug_name);
	this.wait_timeout = wait_timeout;
    }

    @Override
    public void doRefresh() {
	updateLock.lock();
	updateWithCallBack();
	isUpdated.signalAll();
	updateLock.unlock();
    }

    public void refreshCompleted() {
	updateLock.lock();
	update();
	isUpdated.signalAll();
	updateLock.unlock();
    }

    @Override
    public void refreshIfRequired() {
	final long actualTime = Calendar.getInstance().getTimeInMillis();
	updateLock.lock();
	if ((actualTime - lastUpdateTime) > refresh_period_ms) {
	    log.debug("Force realignment");
	    try {
		updateWithCallBack();
		if (!isUpdated.await(wait_timeout, TimeUnit.MILLISECONDS)) {
		    log.error("refreshIfRequired {}: timeout caused update failure", debug_name);
		    isUpdated.signalAll(); // sblocco la faccenda
		    lastUpdateTime = 0; // forzo un realign
		}
	    } catch (InterruptedException e) {
		log.error("refreshIfRequired {}: interrupted!", debug_name);
		e.printStackTrace();
	    }
	}
	updateLock.unlock();
    }
}
