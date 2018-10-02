package com.shopgun.android.sdk.demo;

import android.location.Location;
import android.support.test.runner.AndroidJUnit4;

import com.fonfon.geohash.GeoHash;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Simple tests for the geohash method we're using to convert the location
 */

@RunWith(AndroidJUnit4.class)
public class GeoHashTest {

    private final int PRECISION = 4;

    private Location location1;
    private Location location2;
    private Location location3;

    @Before
    public void setUp() {

        location1 = new Location("geohash");
        location1.setLatitude(55.679633);
        location1.setLongitude(12.577850);

        location2 = new Location("geohash");
        location2.setLatitude(38.354052);
        location2.setLongitude(-99.571113);

        location3 = new Location("geohash");
        location3.setLatitude(-31.386644);
        location3.setLongitude(131.341086);
    }

    @Test
    public void test_1() {
        GeoHash geoHash = GeoHash.fromLocation(location1, PRECISION);
        Assert.assertEquals("u3bu", geoHash.toString());
    }

    @Test
    public void test_2() {
        GeoHash geoHash = GeoHash.fromLocation(location2, PRECISION);
        Assert.assertEquals("9yc4", geoHash.toString());
    }

    @Test
    public void test_3() {
        GeoHash geoHash = GeoHash.fromLocation(location3, PRECISION);
        Assert.assertEquals("qfmm", geoHash.toString());
    }
}
