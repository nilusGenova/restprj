package com.rest.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.rest.hal9000.CachedInfo;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({ CachedInfo.class })
public class CachedInfoTest {

    private static final long REFRESH_TIME = 3000;

    @InjectMocks
    private final CachedInfo<Integer> infQuick1 = new CachedInfo<>(() -> mysupplier1(), 1, "testQuick1");

    @InjectMocks
    private final CachedInfo<Integer> infSlow2 = new CachedInfo<>(0, () -> mysupplier2(), REFRESH_TIME / 2,
	    "testSlow2");

    private Integer mysupplier1() {
	return 1;
    }

    private Integer mysupplier2() {
	return 2;
    }

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCachedInfo() {
	Assert.assertEquals("ERROR:", (Integer) 0, infSlow2.getInfo());
	Assert.assertEquals("ERROR:", (Integer) 1, infQuick1.getInfo());
	try {
	    Thread.sleep(REFRESH_TIME);
	} catch (InterruptedException e) {
	}
	Assert.assertEquals("ERROR:", (Integer) 2, infSlow2.getInfo());
	infSlow2.updateInfo(3);
	infQuick1.updateInfo(4);
	Assert.assertEquals("ERROR:", (Integer) 3, infSlow2.getInfo());
	try {
	    Thread.sleep(10);
	} catch (InterruptedException e) {
	}
	Assert.assertEquals("ERROR:", (Integer) 1, infQuick1.getInfo());
    }
}
