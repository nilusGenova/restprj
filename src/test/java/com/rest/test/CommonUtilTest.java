package com.rest.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.rest.hal9000.CommonUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ CommonUtils.class })
public class CommonUtilTest {

    private final static String myIP = "192.168.10.5";

    private final static String[] mask8IP = { "192.168.11.9", "192.168.10.0", "192.168.10.255" };
    private final static String[] mask2IP = { "192.168.10.2", "192.168.10.0", "192.168.10.4", "192.168.10.7",
	    "192.168.10.255" };
    private final static String[] mask16IP = { "192.169.10.2", "192.168.0.0", "192.168.255.255" };
    private final static String[] mask24IP = { "193.168.10.2", "192.0.0.0", "192.255.255.255" };

    @Before
    public void setUp() throws Exception {
	MockitoAnnotations.initMocks(this);
    }

    private void execTestIpAddresses(Short subNetLen, String ipInNet, String[] ipNotInNet) throws Exception {
	PowerMockito.mockStatic(CommonUtils.class);
	PowerMockito.when(CommonUtils.class, "get_CIDR_IpAddress").thenReturn(myIP + "/" + subNetLen);
	Assert.assertEquals("Wrong IP:", myIP, CommonUtils.getMyIpAddress());
	Assert.assertEquals("Wrong prefix:", subNetLen, CommonUtils.getMyIpNetworkPrefixLength());
	Assert.assertEquals("Wrong CIDR:", myIP + "/" + subNetLen, CommonUtils.getMy_CIDR_IpAddress());
	Assert.assertTrue("Failed isInLocalSubnet", CommonUtils.isInLocalSubnet(ipInNet));
	for (int i = 0; i < ipNotInNet.length; i++) {
	    Assert.assertFalse("Failed isInLocalSubnet", CommonUtils.isInLocalSubnet(ipNotInNet[i]));
	}
    }

    @Test
    public void testIpAddresses() throws Exception {
	execTestIpAddresses((short) 8, "192.168.10.234", mask8IP);
	execTestIpAddresses((short) 2, "192.168.10.6", mask2IP);
	execTestIpAddresses((short) 16, "192.168.150.234", mask16IP);
	execTestIpAddresses((short) 24, "192.10.10.234", mask24IP);
    }

}
