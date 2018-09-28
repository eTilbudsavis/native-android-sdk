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
    private String regression_clientId = "0c0bba80-65cf-480b-9340-3add6725d5bf";

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

    @Test
    public void regressionTest_1() {
        String pp_id = "920fujf";
        Assert.assertEquals("6vZz4FedqNQ=", EventUtils.generateViewToken(pp_id.getBytes(), regression_clientId));
    }

    @Test
    public void regressionTest_2() {
        String of_id = "8818fZWd";
        Assert.assertEquals("mGzI8JcNv+Y=", EventUtils.generateViewToken(of_id.getBytes(), regression_clientId));
    }

    @Test
    public void regressionTest_3() {
        Assert.assertEquals("Xgl5XTmr2Tw=",
                EventUtils.generateViewToken(EventUtils.getDataBytes("3114tkf", 2), regression_clientId));
    }

    @Test
    public void regressionTest_4() {
        String query = "myDog&cat :)";
        Assert.assertEquals("PS00xHQ7Oxo=", EventUtils.generateViewToken(query.getBytes(), regression_clientId));
    }

    @Test
    public void regressionTest_5() {
        String query = " <> %$#$6843135%%^%&";
        Assert.assertEquals("35nRscRCCwE=", EventUtils.generateViewToken(query.getBytes(), regression_clientId));
    }

    @Test
    public void regressionTest_6() {
        Assert.assertEquals("dd9/Kp1699E=",
                EventUtils.generateViewToken(EventUtils.getDataBytes("9b47F8f", 676), regression_clientId));
    }

}
