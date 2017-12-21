package com.rest.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.rest.hal9000.CacheRefreshManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CacheRefreshManager.class })
public class CacheRef_MngTest {
    private static final long REFRESH_TIME_LONG = 3000;
    private static final long REFRESH_TIME_SHORT = 10;

    private int cntLong = 0;
    private int cntShort = 0;

    @InjectMocks
    private final CacheRefreshManager objUTLong = new CacheRefreshManager(() -> updateLong(), REFRESH_TIME_LONG);

    @InjectMocks
    private final CacheRefreshManager objUTShort = new CacheRefreshManager(() -> updateShort(), REFRESH_TIME_SHORT);

    private void updateLong() {
	cntLong++;
    }

    private void updateShort() {
	cntShort++;
    }

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testUpdateCompleted() {
	// Quick case
	cntLong = 0;
	Assert.assertEquals("ERROR:", 0, cntLong);
	objUTLong.refreshIfRequired();
	Assert.assertEquals("ERROR:", 1, cntLong);
	try {
	    Thread.sleep(REFRESH_TIME_LONG / 2);
	} catch (InterruptedException e) {
	}
	objUTLong.refreshIfRequired();
	Assert.assertEquals("ERROR:", 1, cntLong);
	objUTLong.doRefresh();
	Assert.assertEquals("ERROR:", 2, cntLong);
	objUTLong.forceRefresh();
	Assert.assertEquals("ERROR:", 2, cntLong);
	objUTLong.refreshIfRequired();
	Assert.assertEquals("ERROR:", 3, cntLong);

	cntShort = 0;
	Assert.assertEquals("ERROR:", 0, cntShort);
	objUTShort.refreshIfRequired();
	Assert.assertEquals("ERROR:", 1, cntShort);
	try {
	    Thread.sleep(REFRESH_TIME_LONG / 2);
	} catch (InterruptedException e) {
	}
	objUTShort.refreshIfRequired();
	Assert.assertEquals("ERROR:", 2, cntShort);
	objUTShort.doRefresh();
	Assert.assertEquals("ERROR:", 3, cntShort);
    }
}
