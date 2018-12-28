package com.rest.hal9000;

import java.util.function.Supplier;

public class CachedInfo<T> extends CacheRefreshManager {
    private T info = null;

    private final Supplier<T> updCallBck;

    public CachedInfo(final T initial_info, final Supplier<T> updateCallBack, final long refresh_period_ms,
	    final String debug_name) {
	super(refresh_period_ms, debug_name);
	this.updCallBck = updateCallBack;
	setCallBack(() -> info = updCallBck.get());
	updateInfo(initial_info);
    }

    public CachedInfo(final Supplier<T> updateCallBack, final long refresh_period_ms, final String debug_name) {
	super(refresh_period_ms, debug_name);
	this.updCallBck = updateCallBack;
	setCallBack(() -> info = updCallBck.get());
	// refreshIfRequired();
    }

    public T getInfo() {
	refreshIfRequired();
	return info;
    }

    public void updateInfo(final T val) {
	updateLock.lock();
	info = val;
	update();
	updateLock.unlock();
    }

}
