package com.shopgun.android.sdk;

import com.shopgun.android.sdk.eventskit.EventUtils;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class EventUtilsUnitTest {

    private String clientId = "myhash";

    @Test
    public void testViewTokenGenerator_1() {
        Assert.assertEquals("GKtJxfAxRZI=",
                EventUtils.generateViewToken(EventUtils.getDataBytes("pub1", 1), clientId));
    }

    @Test
    public void testViewTokenGenerator_2() {
        String data = "😁";
        Assert.assertEquals("POcLWv7/N4Q=", EventUtils.generateViewToken(data.getBytes(), clientId));
    }

    @Test
    public void testViewTokenGenerator_3() {
        String data = "my search string";
        Assert.assertEquals("bNOIlf+nAAU=", EventUtils.generateViewToken(data.getBytes(), clientId));
    }

    @Test
    public void testViewTokenGenerator_4() {
        String data = "my search string 😁";
        Assert.assertEquals("+OJqwh68nIk=", EventUtils.generateViewToken(data.getBytes(), clientId));
    }

    @Test
    public void testViewTokenGenerator_5() {
        String data = "øl og æg";
        Assert.assertEquals("NTgj68OWnbc=", EventUtils.generateViewToken(data.getBytes(), clientId));
    }

    @Test
    public void testViewTokenGenerator_6() {
        Assert.assertEquals("VwMOrDD8zMk=",
                EventUtils.generateViewToken(EventUtils.getDataBytes("pub1", 9999), clientId));
    }
}
