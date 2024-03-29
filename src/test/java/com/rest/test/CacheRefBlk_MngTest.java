package com.rest.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;
import com.rest.hal9000.CacheRefreshBlockingManager;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({ CacheRefreshBlockingManager.class })
public class CacheRefBlk_MngTest {
    private static final long REFRESH_TIME = 3000;
    private static final long TIMEOUT = 2000;

    private int cnt = 0;
    private int cnt2 = 0;

    @InjectMocks
    private final CacheRefreshBlockingManager objUTQuick = new CacheRefreshBlockingManager(() -> quickUpdater(),
	    REFRESH_TIME, TIMEOUT, "testQuick");

    @InjectMocks
    private final CacheRefreshBlockingManager objUT = new CacheRefreshBlockingManager(() -> updater(), REFRESH_TIME,
	    TIMEOUT, "testUt");

    @InjectMocks
    private final CacheRefreshBlockingManager objUTTooSlow = new CacheRefreshBlockingManager(() -> slowUpdater(),
	    REFRESH_TIME, TIMEOUT, "testSlow");

    private void quickUpdater() {
	cnt++;
    }

    private void updater() {
	final Thread testThread = new Thread(() -> {
	    try {
		Thread.sleep(REFRESH_TIME / 2);
	    } catch (final InterruptedException e) {
	    }
	    cnt++;
	});
	testThread.start();
    }

    private void slowUpdater() {
	final Thread testThread = new Thread(() -> {
	    try {
		Thread.sleep(REFRESH_TIME * 3);
	    } catch (final InterruptedException e) {
	    }
	    cnt2++;
	});
	testThread.start();
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
	objUTQuick.refreshIfRequired();
	Assert.assertEquals("ERROR:", 1, cnt);
	objUTQuick.doRefresh();
	Assert.assertEquals("ERROR:", 2, cnt);
	// Normal case
	cnt = 0;
	Assert.assertEquals("ERROR:", 0, cnt);
	objUT.refreshIfRequired();
	Assert.assertEquals("ERROR:", 1, cnt);
	try {
	    Thread.sleep(REFRESH_TIME * 2);
	} catch (final InterruptedException e) {
	}
	objUT.refreshCompleted();
	Assert.assertEquals("ERROR:", 1, cnt);
	// Too Slow case
	cnt2 = 0;
	Assert.assertEquals("ERROR:", 0, cnt2);
	objUTTooSlow.refreshIfRequired();
	Assert.assertEquals("ERROR:", 0, cnt2);
    }
}
