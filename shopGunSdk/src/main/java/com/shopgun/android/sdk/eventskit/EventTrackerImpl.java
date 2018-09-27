package com.shopgun.android.sdk.eventskit;

class EventTrackerImpl extends EventTracker {

    public void track(AnonymousEvent event) {
        EventManager manager = EventManager.getInstance();
        manager.addEvent(event.setApplicationTrackId(getApplicationTrackId()));
    }

}
