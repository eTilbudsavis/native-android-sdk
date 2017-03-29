package com.shopgun.android.sdk.eventskit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shopgun.android.utils.log.L;

public class EzEvent {

    public static final String TAG = EzEvent.class.getSimpleName();

    public static final String CLIENT_SESSION_OPENED = "client-session-opened";
    public static final String CLIENT_SESSION_CLOSED = "x-client-session-closed";
    public static final String FIRST_CLIENT_SESSION_CLOSED = "first-client-session-opened";

    protected Event mEvent;

    protected EzEvent(String type, JsonObject properties) {
        mEvent = new Event(type, properties);
    }

    public void track() {
        // avoid duplicates
        if (mEvent != null) {
            EventTracker.globalTracker().track(mEvent);
        }
        mEvent = null;
    }

    public static EzEvent clientSessionOpened() {
        return new EzEvent(CLIENT_SESSION_OPENED, new JsonObject());
    }

    public static EzEvent firstClientSessionOpened() {
        return new EzEvent(FIRST_CLIENT_SESSION_CLOSED, new JsonObject());
    }

    public static EzEvent clientSessionClosed() {
        return new EzEvent(CLIENT_SESSION_CLOSED, new JsonObject());
    }

}
