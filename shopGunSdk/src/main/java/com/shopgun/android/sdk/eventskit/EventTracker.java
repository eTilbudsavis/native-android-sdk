package com.shopgun.android.sdk.eventskit;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.corekit.SgnPreferences;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.utils.SgnUtils;
import com.shopgun.android.utils.PackageUtils;
import com.shopgun.android.utils.TextUtils;

public abstract class EventTracker {

    public static final String TAG = EventTracker.class.getSimpleName();

    public static final String META_GLOBAL_TRACKER = "com.shopgun.android.sdk.eventskit.global_tracker_id";

    private static EventTracker mGlobalInstance;

    private JsonObject mClient;
    private JsonObject mView;

    public static EventTracker globalTracker() {
        if (mGlobalInstance == null) {
            synchronized (EventTracker.class) {
                if (mGlobalInstance == null) {
                    Context c = ShopGun.getInstance().getContext();
                    Bundle b = PackageUtils.getMetaData(c);
                    String trackerId = b.getString(META_GLOBAL_TRACKER);
                    if (TextUtils.isEmpty(trackerId)) {
                        SgnLog.w(TAG, "No tracker id found for global tracker instance.");
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
        mClient = new JsonObject();
        mClient.addProperty("id", SgnPreferences.getInstance().getInstallationId());
        mClient.addProperty("trackId", trackId);
    }

    public static EventTracker newTracker(String trackerId) {
        EventTracker tracker = new EventTrackerImpl(trackerId);
        EventManager.getInstance().registerTracker(tracker);
        return tracker;
    }

    public abstract void setView(String[] path);

    public abstract void setView(String[] path, String[] previousPath, Uri uri);

    public abstract void track(Event event);

    /**
     * Meta-data about "where" the event was triggered visually in the app.
     * So, what "page" did the event come from.
     * "path": ["some", "namespaced", "path"], // required
     * "previousPath": ["the", "previous", "path"], // optional
     * "uri": "sgn://offers/sg32rmfsd", // optional
     */
    public void setView(JsonObject view) {
        mView = view;
    }

    public JsonObject getView() {
        return mView;
    }

    public void track(String type, JsonObject properties) {
        track(new Event(type, properties));
    }

    public JsonObject getClient() {
        return mClient;
    }

    public void flush() {
        EventManager.getInstance().flush();
    }

}
