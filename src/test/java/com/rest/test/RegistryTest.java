package com.rest.test;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.core.classloader.annotations.PrepareForTest;

import com.rest.hal9000.HalObjAgent;
import com.rest.hal9000.Registry;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest({ HalObjAgent.class, Registry.class })
public class RegistryTest {

    @Mock
    private HalObjAgent objA;

    @Mock
    private HalObjAgent objB;

    @Mock
    private HalObjAgent objC;

    @InjectMocks
    private final Registry registry = new Registry();

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
	when(objA.getId()).thenReturn('A');
	when(objB.getId()).thenReturn('B');
	when(objC.getId()).thenReturn('C');
	doNothing().when(objA).alignAll();
	doNothing().when(objB).alignAll();
	doNothing().when(objC).alignAll();
	doNothing().when(objA).one_min_tick();
	doNothing().when(objB).one_min_tick();
	doNothing().when(objC).one_min_tick();
    }

    @Test
    public void testRegistry() {

	Assert.assertEquals("Wrong empty size", 0, registry.numOfRegisteredObj());
	registry.registerObj(objA);
	registry.registerObj(objB);
	registry.registerObj(objC);
	Assert.assertEquals("Wrong size", 3, registry.numOfRegisteredObj());
	// test wrong Add
	registry.registerObj(objB);
	Assert.assertEquals("Wrong size", 3, registry.numOfRegisteredObj());

	// test call AlignAll
	registry.callAlignAllForAllRegistered();

	verify(objC, times(1)).alignAll();
	verify(objB, times(1)).alignAll();
	verify(objA, times(1)).alignAll();

	// test call timer
	registry.callOneMinTickForAllRegistered();

	verify(objC, times(1)).one_min_tick();
	verify(objB, times(1)).one_min_tick();
	verify(objA, times(1)).one_min_tick();

	// correct association
	Assert.assertEquals("wrong association for B", 'B', registry.getRegisteredObj('B').getId());
	Assert.assertEquals("wrong association for A", 'A', registry.getRegisteredObj('A').getId());
	Assert.assertEquals("wrong association for C", 'C', registry.getRegisteredObj('C').getId());

	Assert.assertNull("wrong association for not existing obj", registry.getRegisteredObj('D'));

    }

}
