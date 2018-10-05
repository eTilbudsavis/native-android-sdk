package com.shopgun.android.sdk.eventskit;

import android.content.Context;
import android.os.Bundle;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.utils.PackageUtils;
import com.shopgun.android.utils.TextUtils;

/**
 * Global tracker for events. It searches for meta-data in the app manifest:
 * "com.shopgun.android.sdk.eventskit.application_track_id" for production
 * "com.shopgun.android.sdk.develop.eventskit.application_track_id" for development
 * This is the application track id that will be inserted into every event.
 *
 * If none of these is found, it will start a "NoOperative" tracker that will just log the event in the console
 * without sending anything to the server.
 */
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
                    String appTrackId = b.getString(ShopGun.getInstance().isDevelop() && b.containsKey(META_APPLICATION_TRACK_ID_DEBUG) ?
                            META_APPLICATION_TRACK_ID_DEBUG :
                            META_APPLICATION_TRACK_ID);

                    if (TextUtils.isEmpty(appTrackId)) {
                        SgnLog.w(TAG, "Application track id not found");
                        mGlobalInstance = new EventTrackerNoOp();
                    } else {
                        mGlobalInstance = new EventTrackerImpl();
                        mGlobalInstance.setApplicationTrackId(appTrackId);
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

    // used by EventTrackerImpl to add the track id to each event
    String getApplicationTrackId() {
        return applicationTrackId;
    }

    void setApplicationTrackId(String applicationTrackId) {
        this.applicationTrackId = applicationTrackId;
    }

    public void track(int type) {
        AnonymousEvent event = new AnonymousEvent(type);
        track(event);
    }

    public void flush() {
        EventManager.getInstance().flush();
    }

    /** Abstract methods */

    public abstract void track(AnonymousEvent event);
}
