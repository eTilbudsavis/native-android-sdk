package com.shopgun.android.sdk.eventskit;

import android.util.Base64;

import java.security.MessageDigest;
import java.util.Arrays;

public class EventUtils {

    public static final String TAG = EventUtils.class.getSimpleName();
    
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
}
