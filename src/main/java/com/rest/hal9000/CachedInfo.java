package com.rest.hal9000;

import java.util.function.Supplier;

public class CachedInfo<T> extends CacheRefreshManager {
    private T info = null;

    private final Supplier<T> updCallBck;

    public CachedInfo(T initial_info, Supplier<T> updateCallBack, long refresh_period_ms) {
	super(refresh_period_ms);
	this.updCallBck = updateCallBack;
	assignCallBack(() -> info = updCallBck.get());
	updateInfo(initial_info);
    }

    public CachedInfo(Supplier<T> updateCallBack, long refresh_period_ms) {
	super(refresh_period_ms);
	this.updCallBck = updateCallBack;
	assignCallBack(() -> info = updCallBck.get());
	// requestForUpdate();
    }

    public T getInfo() {
	requestForUpdate();
	return info;
    }

    public void updateInfo(T val) {
	updateLock.lock();
	info = val;
	update();
	updateLock.unlock();
    }

}
