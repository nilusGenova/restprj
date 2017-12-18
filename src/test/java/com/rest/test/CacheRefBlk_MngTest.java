package com.rest.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.rest.hal9000.CacheRefreshBlockingManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CacheRefreshBlockingManager.class })
public class CacheRefBlk_MngTest {
    private static final long REFRESH_TIME = 3000;
    private static final long TIMEOUT = 2000;

    private int cnt = 0;

    @InjectMocks
    private final CacheRefreshBlockingManager objUTQuick = new CacheRefreshBlockingManager(() -> quickUpdater(),
	    REFRESH_TIME, TIMEOUT);

    @InjectMocks
    private final CacheRefreshBlockingManager objUT = new CacheRefreshBlockingManager(() -> updater(), REFRESH_TIME,
	    TIMEOUT);

    @InjectMocks
    private final CacheRefreshBlockingManager objUTTooSlow = new CacheRefreshBlockingManager(() -> slowUpdater(),
	    REFRESH_TIME, TIMEOUT);

    @InjectMocks
    private final CacheRefreshBlockingManager objUTFail = new CacheRefreshBlockingManager(() -> noUpdater(),
	    REFRESH_TIME, TIMEOUT);

    private void quickUpdater() {
	cnt++;
	objUTQuick.updateCompleted();
    }

    private void updater() {
	Thread testThread = new Thread(() -> {
	    try {
		Thread.sleep(REFRESH_TIME / 2);
	    } catch (InterruptedException e) {
	    }
	    cnt++;
	    objUT.updateCompleted();
	});
	testThread.start();
    }

    private void slowUpdater() {
	Thread testThread = new Thread(() -> {
	    try {
		Thread.sleep(REFRESH_TIME * 3);
	    } catch (InterruptedException e) {
	    }
	    cnt++;
	    objUTTooSlow.updateCompleted();
	});
	testThread.start();
    }

    private void noUpdater() {
	// Do Nothing
    }

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testUpdateCompleted() {
	// Quick case
	cnt = 0;
	Assert.assertEquals("ERROR:", 0, cnt);
	objUTQuick.requestForUpdate();
	Assert.assertEquals("ERROR:", 1, cnt);
	// Normal case
	cnt = 0;
	Assert.assertEquals("ERROR:", 0, cnt);
	objUT.requestForUpdate();
	Assert.assertEquals("ERROR:", 1, cnt);
	// Fail case
	cnt = 0;
	Assert.assertEquals("ERROR:", 0, cnt);
	objUTFail.requestForUpdate();
	Assert.assertEquals("ERROR:", 0, cnt);
	// Too Slow case
	cnt = 0;
	Assert.assertEquals("ERROR:", 0, cnt);
	objUTTooSlow.requestForUpdate();
	Assert.assertEquals("ERROR:", 0, cnt);
    }
}
