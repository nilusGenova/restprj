package com.rest.test;

import java.lang.reflect.Field;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.rest.hal9000.CachedInfo;
import com.rest.hal9000.CommonUtils;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("jdk.internal.reflect.*")
@PrepareForTest({ CommonUtils.class, CachedInfo.class })
public class CommonUtilTest {

    private final static String myIP = "192.168.10.5";

    private final static String[] mask24IP = { "192.168.11.9", "192.168.10.0", "192.168.10.255" };
    private final static String[] mask30IP = { "192.168.10.2", "192.168.10.0", "192.168.10.4", "192.168.10.7",
	    "192.168.10.255" };
    private final static String[] mask16IP = { "192.169.10.2", "192.168.0.0", "192.168.255.255" };
    private final static String[] mask8IP = { "193.168.10.2", "192.0.0.0", "192.255.255.255" };

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
    }

    @Mock
    private CachedInfo ipAddMock;

    private void execTestIpAddresses(Short subNetLen, String ipInNet, String[] ipNotInNet) throws Exception {
	PowerMockito.mockStatic(CommonUtils.class);
	PowerMockito.spy(CommonUtils.class);
	PowerMockito.when(CommonUtils.class, "get_CIDR_IpAddress").thenReturn(myIP + "/" + subNetLen);
	Field fieldAddress = PowerMockito.field(CommonUtils.class, "ipCIDRaddress");
	try {
	    fieldAddress.set(CommonUtils.class, ipAddMock);
	} catch (IllegalArgumentException e1) {
	    e1.printStackTrace();
	} catch (IllegalAccessException e1) {
	    e1.printStackTrace();
	}
	PowerMockito.spy(CachedInfo.class);
	PowerMockito.when(ipAddMock.getInfo()).thenReturn(myIP + "/" + subNetLen);

	Assert.assertEquals("Wrong IP:", myIP, CommonUtils.getMyIpAddress());
	Assert.assertEquals("Wrong prefix:", subNetLen, CommonUtils.getMyIpNetworkPrefixLength());
	Assert.assertEquals("Wrong CIDR:", myIP + "/" + subNetLen, CommonUtils.getMy_CIDR_IpAddress());
	Assert.assertTrue("Failed isInLocalSubnet", CommonUtils.isInLocalSubnet(ipInNet));
	for (int i = 0; i < ipNotInNet.length; i++) {
	    Assert.assertFalse("Failed isInLocalSubnet on:" + ipNotInNet[i],
		    CommonUtils.isInLocalSubnet(ipNotInNet[i]));
	}
    }

    @Test
    public void testIpAddresses() throws Exception {
	execTestIpAddresses((short) 24, "192.168.10.234", mask24IP);
	execTestIpAddresses((short) 30, "192.168.10.6", mask30IP);
	execTestIpAddresses((short) 16, "192.168.150.234", mask16IP);
	execTestIpAddresses((short) 8, "192.10.10.234", mask8IP);
    }

}
