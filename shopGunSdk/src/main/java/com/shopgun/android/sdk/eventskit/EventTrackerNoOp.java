package com.shopgun.android.sdk.eventskit;

import com.shopgun.android.utils.log.L;

class EventTrackerNoOp extends EventTracker {

    public static final String TAG = EventTrackerNoOp.class.getSimpleName();

    public void track(AnonymousEvent event) {
        L.d(TAG, "Received event: " + event.toString());
        // ignore
    }

}
