package com.shopgun.android.sdk.eventskit;

import android.content.Context;
import android.location.Location;
import android.util.Base64;

import com.fonfon.geohash.GeoHash;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.utils.SgnUtils;
import com.shopgun.android.utils.LocationUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.Arrays;

public class EventUtils {

    public static final String TAG = EventUtils.class.getSimpleName();
    public static final int GEO_HASH_PRECISION = 4;

    private EventUtils() {

    }

    public static String generateViewToken(byte[] data, String salt) {
        try {
            // get the bytes of the salt
            byte[] id = salt.getBytes("UTF-8");

            // create the byte array with all of data -> salt + data
            byte[] payload = new byte[data.length + id.length];
            System.arraycopy(id, 0, payload, 0, id.length);
            System.arraycopy(data, 0, payload, id.length, data.length);

            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(payload);
            byte digest_result[] = digest.digest();

            // take the first 8 bytes
            byte md5[] = Arrays.copyOfRange(digest_result, 0, 8);

            // encode to base 64
            return Base64.encodeToString(md5, Base64.NO_WRAP);

        }catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void addLocationInformation(Context context, AnonymousEvent event) {

        // if the app has the location permissions, ask the location to the system
        Location location = LocationUtils.getLastKnownLocation(context);

        // set the data only if the accuracy < 2km
        if(location != null && location.getAccuracy() < 2000) {

            GeoHash geoHash = GeoHash.fromLocation(location, GEO_HASH_PRECISION);

            event.addUserLocation(geoHash.toString(), location.getTime());
        }
    }

    /**
     * For the events that need to concatenate ppid and page number (string + int) and pass it
     * to the view token generator
     * @param ppid page publication id
     * @param pageNumber page number
     * @return byte array that contains the byte version of ppid concatenated with page number
     */
    public static byte[] getDataBytes(String ppid, int pageNumber) {
        byte[] pageBytes = SgnUtils.intToByteArray(pageNumber);
        byte[] ppidBytes;
        try {
            ppidBytes = ppid.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            ppidBytes = ppid.getBytes();
        }

        byte[] data = new byte[ppidBytes.length + pageBytes.length];

        System.arraycopy(ppidBytes, 0, data, 0, ppidBytes.length);
        System.arraycopy(pageBytes, 0, data, ppidBytes.length, pageBytes.length);

        return data;
    }

}
