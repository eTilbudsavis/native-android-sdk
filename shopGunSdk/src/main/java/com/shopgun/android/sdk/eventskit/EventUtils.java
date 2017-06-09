package com.shopgun.android.sdk.eventskit;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.location.Location;
import android.os.Build;

import com.google.gson.JsonObject;
import com.shopgun.android.sdk.utils.SgnUserAgent;
import com.shopgun.android.utils.DateUtils;
import com.shopgun.android.utils.DeviceUtils;
import com.shopgun.android.utils.DisplayUtils;
import com.shopgun.android.utils.LocationUtils;
import com.shopgun.android.utils.PackageUtils;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class EventUtils {

    public static final String TAG = EventUtils.class.getSimpleName();
    
    private EventUtils() {

    }

    public static JsonObject getContext(Context context) {
        JsonObject object = new JsonObject();
        object.add("application", application(context));
        object.add("device", device(context));
        object.addProperty("locale", Locale.getDefault().toString());
        object.add("location", location(context));
        object.add("network", network(context));
        object.add("os", os(context));
        object.add("session", session(context));
        object.add("timeZone", timezone(context));
        object.addProperty("userAgent", SgnUserAgent.getUserAgent(context));
        return object;
    }

    public static JsonObject network(Context context) {
        JsonObject object = new JsonObject();
        object.add("bluetooth", null);
        object.add("carrier", null);
        object.add("cellular", null);
        object.add("ip", null);
        object.add("wifi", null);
        object.add("bluetooth", null);
        return object;
    }

    public static JsonObject os(Context context) {
        JsonObject object = new JsonObject();
        object.addProperty("name", "Android");
        object.addProperty("version", Build.VERSION.RELEASE);
        return object;
    }

    public static JsonObject timezone(Context context) {
        JsonObject object = new JsonObject();
        int seconds = TimeZone.getDefault().getRawOffset()/1000;
        object.addProperty("utcOffsetSeconds", seconds);
        return object;
    }

    public static JsonObject session(Context context) {
        JsonObject object = new JsonObject();
        object.add("id", null);
        object.add("referrer", null);
        return object;
    }

    public static JsonObject device(Context context) {
        JsonObject screen = new JsonObject();
        screen.addProperty("width", DisplayUtils.getScreenWidth(context));
        screen.addProperty("height", DisplayUtils.getScreenHeight(context));

        JsonObject object = new JsonObject();
        object.addProperty("manufacturer", DeviceUtils.getManufacturer());
        object.addProperty("model", DeviceUtils.getModel());
        object.add("screen", screen);
        return object;
    }

    public static JsonObject application(Context context) {
        JsonObject object = new JsonObject();
        ApplicationInfo ai = PackageUtils.getApplicationInfo(context);
        object.addProperty("name", (String) context.getPackageManager().getApplicationLabel(ai));
        object.addProperty("version", PackageUtils.getVersionName(context));
        object.addProperty("build", String.valueOf(PackageUtils.getVersionCode(context)));
        object.addProperty("id", context.getPackageName());
        return object;
    }

    public static JsonObject location(Context mContext) {
        Location loc = LocationUtils.getLastKnownLocation(mContext);
        return location(loc);
    }

    public static JsonObject location(Location loc) {
        if (loc == null) {
            return null;
        }
        JsonObject object = new JsonObject();
        object.addProperty("determinedAt", DateUtils.format(new Date(loc.getTime())));
        object.addProperty("latitude", loc.getLatitude());
        object.addProperty("longitude", loc.getLongitude());
        object.addProperty("altitude", loc.hasAltitude() ? loc.getAltitude() : null);
        object.addProperty("speed", loc.hasSpeed() ? loc.getSpeed() : null);
        if (loc.hasAccuracy()) {
            JsonObject accuracy = new JsonObject();
            accuracy.addProperty("horizontal", loc.getAccuracy());
            accuracy.addProperty("vertical", loc.getAccuracy());
            object.add("accuracy", accuracy);
        }
        object.add("floor", null);
        return object;
    }

}
