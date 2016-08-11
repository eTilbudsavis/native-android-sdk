package com.shopgun.android.sdk.eventskit;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.location.Location;
import android.os.Build;

import com.shopgun.android.sdk.utils.SgnUserAgent;
import com.shopgun.android.sdk.utils.SgnUtils;
import com.shopgun.android.utils.DeviceUtils;
import com.shopgun.android.utils.DisplayUtils;
import com.shopgun.android.utils.LocationUtils;
import com.shopgun.android.utils.PackageUtils;

import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class EventUtils {

    public static final String TAG = EventUtils.class.getSimpleName();
    
    private EventUtils() {
    }

    public static JSONObject getContext(Context context) {
        JsonMap map = new JsonMap();
        map.put("application", application(context));
        map.put("device", device(context));
        map.put("locale", Locale.getDefault().toString());
        map.put("location", location(context));
        map.put("network", network(context));
        map.put("os", os(context));
        map.put("session", session(context));
        map.put("timezone", timezone(context));
        map.put("userAgent", SgnUserAgent.getUserAgent(context));
        return map.toJson();
    }

    public static JSONObject network(Context context) {
        JsonMap map = new JsonMap();
        map.put("bluetooth", null);
        map.put("carrier", null);
        map.put("cellular", null);
        map.put("ip", null);
        map.put("wifi", null);
        map.put("bluetooth", null);
        return map.toJson();
    }

    public static JSONObject os(Context context) {
        JsonMap map = new JsonMap();
        map.put("name", "Android");
        map.put("version", Build.VERSION.RELEASE);
        return map.toJson();
    }

    public static JSONObject timezone(Context context) {
        JsonMap map = new JsonMap();
        int seconds = TimeZone.getDefault().getRawOffset()/1000;
        map.put("utcOffsetSeconds", seconds);
        return map.toJson();
    }

    public static JSONObject session(Context context) {
        JsonMap map = new JsonMap();
        map.put("id", null);
        map.put("referrer", null);
        return map.toJson();
    }

    public static JSONObject device(Context context) {

        JsonMap screen = new JsonMap();
        screen.put("width", DisplayUtils.getScreenWidth(context));
        screen.put("height", DisplayUtils.getScreenHeight(context));

        JsonMap map = new JsonMap();
        map.put("manufacturer", DeviceUtils.getManufacturer());
        map.put("model", DeviceUtils.getModel());
        map.put("screen", screen);
        return map.toJson();
    }

    public static JSONObject application(Context context) {
        JsonMap map = new JsonMap();
        ApplicationInfo ai = PackageUtils.getApplicationInfo(context);
        map.put("name", context.getPackageManager().getApplicationLabel(ai));
        map.put("version", PackageUtils.getVersionName(context));
        map.put("build", String.valueOf(PackageUtils.getVersionCode(context)));
        map.put("id", context.getPackageName());
        return map.toJson();
    }

    public static JSONObject location(Context mContext) {
        Location mLocation = LocationUtils.getLastKnownLocation(mContext);
        if (mLocation != null) {
            JsonMap map = new JsonMap();
            map.put("determinedAt", SgnUtils.dateToString(new Date(mLocation.getTime())));
            map.put("latitude", mLocation.getLatitude());
            map.put("longitude", mLocation.getLongitude());
            map.put("altitude", mLocation.hasAltitude() ? mLocation.getAltitude() : null);
            map.put("speed", mLocation.hasSpeed() ? mLocation.getSpeed() : null);
            if (mLocation.hasAccuracy()) {
                JsonMap accuracy = new JsonMap();
                accuracy.put("horizontal", mLocation.getAccuracy());
                accuracy.put("vertical", mLocation.getAccuracy());
                map.put("accuracy", accuracy);
            }
            map.put("floor", null);
            return map.toJson();
        }
        return null;
    }

}
