package com.shopgun.android.sdk.eventskit;

import android.content.Context;
import android.location.Location;
import android.util.Base64;

import com.fonfon.geohash.GeoHash;
import com.shopgun.android.utils.LocationUtils;

import java.security.MessageDigest;
import java.util.Arrays;

public class EventUtils {

    public static final String TAG = EventUtils.class.getSimpleName();
    public static final int GEO_HASH_PRECISION = 4;

    private EventUtils() {

    }

    public static String generateViewToken(String data, String clientId) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update((clientId + data).getBytes());
            byte digest_result[] = digest.digest();

            // take the first 8 bytes
            byte md5[] = Arrays.copyOfRange(digest_result, 0, 8);

            // encode to base 64
            return Base64.encodeToString(md5, Base64.DEFAULT);

        }catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static SgnGeoHash getLocation(Context context){
        SgnGeoHash sgnGeoHash = new SgnGeoHash();

        Location location = LocationUtils.getLastKnownLocation(context);

        // set the data only if the accuracy < 2km
        if(location != null && location.getAccuracy() < 2000) {
            sgnGeoHash.timestamp = location.getTime();

            GeoHash geoHash = GeoHash.fromLocation(location, GEO_HASH_PRECISION);
            sgnGeoHash.geoHash = geoHash.toString();
        }

        return sgnGeoHash;
    }

}
