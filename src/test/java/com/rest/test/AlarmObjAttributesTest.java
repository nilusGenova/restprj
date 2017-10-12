package com.rest.test;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.rest.hal9000.AlarmObjAttributes;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({ AlarmObjAttributes.class })
public class AlarmObjAttributesTest {

    @InjectMocks
    private final AlarmObjAttributes attr = new AlarmObjAttributes();

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testKeys() {
	Assert.assertFalse("ERROR:", attr.keyExists(42));
	attr.storeKey(0, 42);
	Assert.assertTrue("ERROR:", attr.keyExists(42));
	Assert.assertFalse("ERROR:", attr.keyExists(711));
	attr.storeKey(3, 711);
	Assert.assertTrue("ERROR:", attr.keyExists(711));
	Assert.assertTrue("ERROR:", attr.keyExists(42));
	Assert.assertFalse("ERROR:", attr.keyExists(5892));
	attr.storeKey(3, 5892);
	Assert.assertFalse("ERROR:", attr.keyExists(711));
	Assert.assertTrue("ERROR:", attr.keyExists(5892));
	Assert.assertTrue("ERROR:", attr.keyExists(42));	
	attr.storePin(0, 24);
	attr.storePin(3, 9);
	Assert.assertFalse("ERROR:", attr.keyExists(711));
	Assert.assertTrue("ERROR:", attr.keyExists(5892));
	Assert.assertTrue("ERROR:", attr.keyExists(42));
	Assert.assertEquals("ERROR:", 24, attr.getPin(0));
	Assert.assertEquals("ERROR:", 9, attr.getPin(3));
	Assert.assertEquals("ERROR:", -1, attr.getPin(1));
	attr.flushKeys();
	Assert.assertFalse("ERROR:", attr.keyExists(42));
	Assert.assertFalse("ERROR:", attr.keyExists(711));
	Assert.assertFalse("ERROR:", attr.keyExists(5892));
	Assert.assertEquals("ERROR:", -1, attr.getPin(0));
	Assert.assertEquals("ERROR:", -1, attr.getPin(3));
    }
}
