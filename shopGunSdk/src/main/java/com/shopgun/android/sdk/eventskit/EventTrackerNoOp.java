package com.shopgun.android.sdk.eventskit;

import android.net.Uri;

import com.shopgun.android.sdk.utils.SgnUtils;
import com.shopgun.android.utils.log.L;

class EventTrackerNoOp extends EventTracker {

    public static final String TAG = EventTrackerNoOp.class.getSimpleName();

    EventTrackerNoOp() {
        super(SgnUtils.createUUID());
        // ignore
    }

    public void track(AnonymousEvent event) {
        L.d(TAG, "Received event: " + event.toString());
        // ignore
    }

}
