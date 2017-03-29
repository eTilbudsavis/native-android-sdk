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
//            log();
            EventTracker.globalTracker().track(mEvent);
        }
        mEvent = null;
    }

    private void log() {
        StringBuilder sb = new StringBuilder();
        String type = mEvent.getType().substring(18);
        JsonObject p = mEvent.getProperties();
        if (p.has("pagedPublicationPage")) {
            JsonObject ppp = p.getAsJsonObject("pagedPublicationPage");
            sb.append("[").append(ppp.get("pageNumber").getAsString()).append("]");
        } else if (p.has("pagedPublicationPageSpread")) {
            JsonArray ppps = p.getAsJsonArray("pagedPublicationPageSpread");
            sb.append(ppps.toString());
        } else {
            sb.append(p.getAsJsonObject("pagedPublication").get("id").getAsString());
        }
        sb.append(" ").append(type);
        L.d(TAG, sb.toString());
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
