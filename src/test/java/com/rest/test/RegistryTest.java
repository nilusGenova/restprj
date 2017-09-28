package com.rest.test;

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
    }

    @Test
    public void testSizeRegistry() {

	Assert.assertEquals("Wrong empty size", 0, registry.numOfRegisteredObj());
	registry.registerObj(objA);
	registry.registerObj(objB);
	registry.registerObj(objC);
	Assert.assertEquals("Wrong size", registry.numOfRegisteredObj(), 3);
	// test wrong Add
	registry.registerObj(objB);
	Assert.assertEquals("Wrong size", 3, registry.numOfRegisteredObj());

    }

    @Test
    public void testCorrectAssociation() {

	Assert.assertEquals("wrong association for B", 'B', registry.getRegisteredObj('B').getId());
	Assert.assertSame("wrong association for A", 'A', registry.getRegisteredObj('A').getId());
	Assert.assertSame("wrong association for C", 'C', registry.getRegisteredObj('C').getId());

	Assert.assertNull("wrong association for not existing obj", registry.getRegisteredObj('D'));

    }

}
