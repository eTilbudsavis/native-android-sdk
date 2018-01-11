package com.shopgun.android.sdk.eventskit;

import com.google.gson.JsonObject;
import com.shopgun.android.sdk.log.SgnLog;

public class EzEvent {

    public static final String TAG = EzEvent.class.getSimpleName();

    public static final String PAGED_PUBLICATION_OPENED = "paged-publication-opened";
    public static final String PAGED_PUBLICATION_APPEARED = "paged-publication-appeared";
    public static final String PAGED_PUBLICATION_DISAPPEARED = "paged-publication-disappeared";
    public static final String PAGED_PUBLICATION_PAGE_APPEARED = "paged-publication-page-appeared";
    public static final String PAGED_PUBLICATION_PAGE_DISAPPEARED = "paged-publication-page-disappeared";
    public static final String PAGED_PUBLICATION_PAGE_LOADED = "paged-publication-page-loaded";
    public static final String PAGED_PUBLICATION_PAGE_CLICKED = "paged-publication-page-clicked";
    public static final String PAGED_PUBLICATION_PAGE_DOUBLE_CLICKED = "paged-publication-page-double-clicked";
    public static final String PAGED_PUBLICATION_PAGE_HOTSPOT_CLICKED = "paged-publication-page-hotspots-clicked";
    public static final String PAGED_PUBLICATION_PAGE_LONG_CLICKED = "paged-publication-page-long-pressed";
    public static final String PAGED_PUBLICATION_PAGE_SPREAD_APPEARED = "paged-publication-page-spread-appeared";
    public static final String PAGED_PUBLICATION_PAGE_SPREAD_DISAPPEARED = "paged-publication-page-spread-disappeared";
    public static final String PAGED_PUBLICATION_PAGE_SPREAD_ZOOM_IN = "paged-publication-page-spread-zoomed-in";
    public static final String PAGED_PUBLICATION_PAGE_SPREAD_ZOOM_OUT = "paged-publication-page-spread-zoomed-out";
    public static final String PAGED_PUBLICATION_OUTRO_APPEARED = "x-paged-publication-outro-appeared";

    public static final String CLIENT_SESSION_OPENED = "client-session-opened";
    public static final String CLIENT_SESSION_CLOSED = "x-client-session-closed";
    public static final String FIRST_CLIENT_SESSION_OPENED = "first-client-session-opened";

    protected Event mEvent;
    private boolean mDebug;

    public static EzEvent create(String type, JsonObject properties) {
        return new EzEvent(type, properties);
    }

    public static EzEvent create(String type) {
        return new EzEvent(type);
    }

    protected EzEvent(String type) {
        this(type, new JsonObject());
    }

    protected EzEvent(String type, JsonObject properties) {
        if (properties == null) {
            properties = new JsonObject();
        }
        mEvent = new Event(type, properties);
    }

    public EzEvent setDebug(boolean debug) {
        mDebug = debug;
        return this;
    }

    public void track() {
        // avoid duplicates
        if (mEvent != null) {
            if (mDebug) {
                SgnLog.d(TAG, mEvent.toString());
            }
            EventTracker.globalTracker().track(mEvent);
        }
        mEvent = null;
    }

}
