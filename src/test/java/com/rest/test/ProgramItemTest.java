package com.rest.test;

import org.junit.Assert;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.rest.hal9000.ProgramItem;

@PrepareForTest({ ProgramItem.class })
public class ProgramItemTest {

    @Test
    public void testProgramItemIntIntIntIntBoolean1() {
	ProgramItem progItem = new ProgramItem(18, 48, 3, 2, true);
	Assert.assertEquals("wrong Prog Item constructor", 18, progItem.getHour());
	Assert.assertEquals("wrong Prog Item constructor", 45, progItem.getMin());
	Assert.assertEquals("wrong Prog Item constructor", 3, progItem.getDay());
	Assert.assertEquals("wrong Prog Item constructor", 2, progItem.getTempLevel());
	Assert.assertEquals("wrong Prog Item constructor", true, progItem.isInterpolation());
    }

    @Test
    public void testProgramItemIntIntIntIntBoolean2() {
	ProgramItem progItem = new ProgramItem(18, 5, 7, 0, false);
	Assert.assertEquals("wrong Prog Item constructor", 18, progItem.getHour());
	Assert.assertEquals("wrong Prog Item constructor", 5, progItem.getMin());
	Assert.assertEquals("wrong Prog Item constructor", 7, progItem.getDay());
	Assert.assertEquals("wrong Prog Item constructor", 0, progItem.getTempLevel());
	Assert.assertEquals("wrong Prog Item constructor", false, progItem.isInterpolation());
    }

    @Test
    public void testGetHalFormat() {
	ProgramItem progItem = new ProgramItem(18, 5, 7, 0, false);
	Assert.assertEquals("wrong conversion", Integer.toString(0xf161), progItem.getHalFormat());
	ProgramItem progItem2 = new ProgramItem(7, 50, 2, 2, true);
	Assert.assertEquals("wrong conversion", Integer.toString(0x5c7a), progItem2.getHalFormat());
    }

    @Test
    public void testIsValidFormat() {
	ProgramItem progItem = new ProgramItem();
	Assert.assertTrue("wrong conversion", progItem.isValidFormat(Integer.toString(0x5c7a)));
    }

    @Test
    public void testNotValidFormat() {
	ProgramItem progItem = new ProgramItem();
	Assert.assertFalse("wrong validation", progItem.isValidFormat(Integer.toString(0x4c7a)));
	Assert.assertFalse("wrong validation", progItem.isValidFormat(Integer.toString(0x5cca)));
	Assert.assertFalse("wrong validation", progItem.isValidFormat(Integer.toString(0x5c7c)));
    }

    @Test
    public void testSetFromHalFormat() {
	ProgramItem progItem = new ProgramItem();
	progItem.setFromHalFormat(Integer.toString(0x5c7a));
	Assert.assertEquals("wrong set", 7, progItem.getHour());
	Assert.assertEquals("wrong set", 50, progItem.getMin());
	Assert.assertEquals("wrong set", 2, progItem.getDay());
	Assert.assertEquals("wrong set", 2, progItem.getTempLevel());
	Assert.assertEquals("wrong set", true, progItem.isInterpolation());
	progItem.setFromHalFormat(Integer.toString(0xf161));
	Assert.assertEquals("wrong set", 18, progItem.getHour());
	Assert.assertEquals("wrong set", 5, progItem.getMin());
	Assert.assertEquals("wrong set", 7, progItem.getDay());
	Assert.assertEquals("wrong set", 0, progItem.getTempLevel());
	Assert.assertEquals("wrong set", false, progItem.isInterpolation());
    }

}
