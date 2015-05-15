package com.eTilbudsavis.etasdk.test;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.EtaLocation;
import com.eTilbudsavis.etasdk.utils.Utils;

import junit.framework.Assert;

/**
 * Created by oizo on 15/05/15.
 */
public class EtaLocationTest {

    public static final String TAG = Constants.getTag(EtaLocationTest.class);

    private EtaLocationTest() {
        // empty
    }

    public static void test() {

        EtaSdkTest.start(TAG);
        testEtaLocation();

    }

    public static void testEtaLocation() {

        double lat = 56.0d;
        double lng = 8.0d;
        int radius = 50000;
        String address = "random";

        EtaLocation l = new EtaLocation();
        l.setLatitude(lat);
        l.setLongitude(lng);
        l.setRadius(radius);
        l.setAddress(address);

        EtaLocation pl = Utils.copyParcelable(l, EtaLocation.CREATOR);

        // android.location.Location doesn't implement equals
        Assert.assertEquals(l.getLatitude(), pl.getLatitude());
        Assert.assertEquals(l.getLongitude(), pl.getLongitude());
        Assert.assertEquals(l.getAddress(), pl.getAddress());
        Assert.assertEquals(l.getRadius(), pl.getRadius());

        EtaSdkTest.logTest(TAG, (new MethodNameHelper() {}).getName());
    }

}
