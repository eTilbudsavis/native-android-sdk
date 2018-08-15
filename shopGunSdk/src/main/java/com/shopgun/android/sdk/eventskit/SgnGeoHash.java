package com.shopgun.android.sdk.eventskit;

/**
 * Class to pass around the geohash information
 */
public class SgnGeoHash {

    public String geoHash;
    public long timestamp;

    SgnGeoHash() {
        geoHash = "";
        timestamp = 0;
    }
}
