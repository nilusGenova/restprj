package com.rest.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.rest.hal9000.CachedInfo;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CachedInfo.class })
public class CachedInfoTest {

    private static final long REFRESH_TIME = 3000;

    @InjectMocks
    private final CachedInfo<Integer> inf1 = new CachedInfo<>(() -> mysupplier1(), 1);

    @InjectMocks
    private final CachedInfo<Integer> inf2 = new CachedInfo<>(0, () -> mysupplier2(), REFRESH_TIME);

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
	Assert.assertEquals("ERROR:", (Integer) 0, inf2.getInfo());
	Assert.assertEquals("ERROR:", (Integer) 1, inf1.getInfo());
	try {
	    Thread.sleep(REFRESH_TIME);
	} catch (InterruptedException e) {
	}
	Assert.assertEquals("ERROR:", (Integer) 2, inf2.getInfo());
	inf2.update_info(3);
	Assert.assertEquals("ERROR:", (Integer) 3, inf2.getInfo());
	inf1.update_info(4);
	Assert.assertEquals("ERROR:", (Integer) 1, inf1.getInfo());
    }
}
