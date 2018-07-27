package com.shopgun.android.sdk.eventskit;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.utils.SgnUtils;
import com.shopgun.android.utils.PackageUtils;
import com.shopgun.android.utils.TextUtils;

import java.util.concurrent.TimeUnit;

import io.realm.RealmModel;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * New anonymous format for events
 */
public class AnonymousEvent {

    public static final String TAG = AnonymousEvent.class.getSimpleName();
    public static final String META_APPLICATION_TRACK_ID = "com.shopgun.android.sdk.eventskit.application_track_id";
    public static final String META_APPLICATION_TRACK_ID_DEBUG = "com.shopgun.android.sdk.develop.eventskit.application_track_id";

    /* The event version scheme to use */
    private static final String VERSION = "2";

    /* Default event type = empty event */
    public static final int DEFAULT_TYPE = 0;

    private boolean mDoNotTrack;

    private JsonObject event;

    public AnonymousEvent(int type) {
        Context c = ShopGun.getInstance().getContext();
        Bundle b = PackageUtils.getMetaData(c);
        String trackerId = b.getString(ShopGun.getInstance().isDevelop() && b.containsKey(META_APPLICATION_TRACK_ID_DEBUG) ?
                                        META_APPLICATION_TRACK_ID_DEBUG :
                                        META_APPLICATION_TRACK_ID);

        event = new JsonObject();
        event.addProperty("_v", VERSION);
        event.addProperty("_i", SgnUtils.createUUID());
        event.addProperty("_e", type);
        event.addProperty("_t", getTimestamp());
        event.addProperty("_a", trackerId);
    }

    private long getTimestamp() {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    }

    public AnonymousEvent add(String property, String value) {
        event.addProperty(property, value);
        return this;
    }


    public boolean doNotTrack() {
        return mDoNotTrack;
    }

    public void doNotTrack(boolean doNotTrack) {
        mDoNotTrack = doNotTrack;
    }

    @Override
    public String toString() {
        return event.toString();
    }

}
