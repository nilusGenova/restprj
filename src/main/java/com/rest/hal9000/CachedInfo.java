package com.rest.hal9000;

import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedInfo<T> extends CacheRefreshBlockingManager {
    private final static long WAIT_TIMEOUT_MS = 100;
    private T info = null;

    private final Supplier<T> updCallBck;
   
    public CachedInfo(T initial_info, Supplier<T> updateCallBack, long refresh_period_ms) {
	super(refresh_period_ms, WAIT_TIMEOUT_MS);
	this.updCallBck = updateCallBack;
	assignCallBack(()->updateInfo(updCallBck.get()));
	updateInfo(initial_info);
    }

    public CachedInfo(Supplier<T> updateCallBack, long refresh_period_ms) {
	super(refresh_period_ms, WAIT_TIMEOUT_MS);
	this.updCallBck = updateCallBack;
	assignCallBack(()->updateInfo(updCallBck.get()));
	requestForUpdate();
    }

    public T getInfo() {
	requestForUpdate();
	return info;
    }

    public void updateInfo(T val) {
	info = val;
	updateCompleted();
    }

}
