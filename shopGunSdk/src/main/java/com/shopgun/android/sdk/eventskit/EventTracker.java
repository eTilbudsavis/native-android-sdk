package com.shopgun.android.sdk.eventskit;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.google.gson.JsonObject;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.corekit.SgnPreferences;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.utils.PackageUtils;
import com.shopgun.android.utils.TextUtils;

public abstract class EventTracker {

    public static final String TAG = EventTracker.class.getSimpleName();

    public static final String META_APPLICATION_TRACK_ID = "com.shopgun.android.sdk.eventskit.application_track_id";
    public static final String META_APPLICATION_TRACK_ID_DEBUG = "com.shopgun.android.sdk.develop.eventskit.application_track_id";

    private static EventTracker mGlobalInstance;

    private String applicationTrackId;

    public static EventTracker globalTracker() {
        if (mGlobalInstance == null) {
            synchronized (EventTracker.class) {
                if (mGlobalInstance == null) {
                    Context c = ShopGun.getInstance().getContext();
                    Bundle b = PackageUtils.getMetaData(c);

                    // Get the application track id
                    String trackerId = b.getString(ShopGun.getInstance().isDevelop() && b.containsKey(META_APPLICATION_TRACK_ID_DEBUG) ?
                            META_APPLICATION_TRACK_ID_DEBUG :
                            META_APPLICATION_TRACK_ID);

                    if (TextUtils.isEmpty(trackerId)) {
                        SgnLog.w(TAG, "Application track id not found");
                        mGlobalInstance = new EventTrackerNoOp();
                    } else {
                        mGlobalInstance = new EventTrackerImpl(trackerId);
                    }
                }
            }
        }
        return mGlobalInstance;
    }

    public static void setGlobalTracker(EventTracker tracker) {
        synchronized (EventTracker.class) {
            mGlobalInstance = tracker;
        }
    }

    protected EventTracker(String trackId) {
        applicationTrackId = trackId;
    }

    public static EventTracker newTracker(String trackerId) {
        EventTracker tracker = new EventTrackerImpl(trackerId);
        EventManager.getInstance().registerTracker(tracker);
        return tracker;
    }

    public String getApplicationTrackId() {
        return applicationTrackId;
    }

    public abstract void track(AnonymousEvent event);

    public void track(int type) {
        AnonymousEvent event = new AnonymousEvent(type, applicationTrackId);
        track(event);
    }

    public void flush() {
        EventManager.getInstance().flush();
    }

}
