package com.shopgun.android.sdk.eventskit;

import com.google.gson.JsonObject;
import com.shopgun.android.sdk.utils.SgnUtils;
import com.shopgun.android.utils.TextUtils;

import java.util.concurrent.TimeUnit;

import io.realm.RealmModel;

/**
 * New anonymous format for events
 */
public class AnonymousEvent {

    public static final String TAG = AnonymousEvent.class.getSimpleName();

    /* The json_event version scheme to use */
    private static final int VERSION = 2;

    /* Default json_event type = empty json_event */
    public static final int DEFAULT_TYPE = 0;

    /* Predefined events */
    public static final int PAGED_PUBLICATION_OPENED = 1;
    public static final int PAGED_PUBLICATION_PAGE_DISAPPEARED = 2;
    public static final int OFFER_OPENED = 3;
    public static final int CLIENT_SESSION_OPENED = 4;
    public static final int SEARCHED = 5;

    private boolean mDoNotTrack;

    private JsonObject json_event;

    // additional custom data that the event could carry around
    // to be used inside the application.
    // It won't be sent to the server or stored anywhere
    private Object data;

    private int type;
    private String id;
    private long timestamp;

    /**
     * @param type integer code for the json_event type. The sdk defines a few basics types
     *             DEFAULT_TYPE = 0;
     *             PAGED_PUBLICATION_OPENED = 1;
     *             PAGED_PUBLICATION_PAGE_DISAPPEARED = 2;
     *             OFFER_OPENED = 3;
     *             CLIENT_SESSION_OPENED = 4;
     *             SEARCHED = 5;
     */
    public AnonymousEvent(int type) {
        id = SgnUtils.createUUID();
        timestamp = timestamp();
        this.type = type;

        json_event = new JsonObject();
        json_event.addProperty("_v", VERSION);
        json_event.addProperty("_i", id);
        json_event.addProperty("_e", type);
        json_event.addProperty("_t", timestamp);
    }

    private long timestamp() {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    }

    public AnonymousEvent add(String property, String value) {
        json_event.addProperty(property, value);
        return this;
    }

    /* Set by the EventTracker that will read it from the manifest metadata */
    protected AnonymousEvent setApplicationTrackId(String applicationTrackId) {
        if (!TextUtils.isEmpty(applicationTrackId)) {
            json_event.addProperty("_a", applicationTrackId);
        }
        return this;
    }


    /****** Common fields */

    public AnonymousEvent addUserLocation(String geohash, long timestamp) {
        if (!TextUtils.isEmpty(geohash) && timestamp > 0) {
            json_event.addProperty("l.h", geohash);
            json_event.addProperty("l.ht", timestamp);
        }
        return this;
    }

    public AnonymousEvent addUserCountry(String countryCode) {
        if (!TextUtils.isEmpty(countryCode)) {
            json_event.addProperty("l.c", countryCode);
        }
        return this;
    }

    public AnonymousEvent addViewToken(String viewToken) {
        if (!TextUtils.isEmpty(viewToken)) {
            json_event.addProperty("vt", viewToken);
        }
        return this;
    }


    /****** Fields for predefined events */

    public AnonymousEvent addPublicationOpened(String publicationId) {
        if (!TextUtils.isEmpty(publicationId)) {
            json_event.addProperty("pp.id", publicationId);
        }
        return this;
    }

    public AnonymousEvent addPageOpened(String publicationId, int page) {
        if (!TextUtils.isEmpty(publicationId) && page > 0) {
            this.addPublicationOpened(publicationId);
            json_event.addProperty("ppp.n", page);
        }
        return this;
    }

    public AnonymousEvent addOfferOpened(String offerId) {
        if (!TextUtils.isEmpty(offerId)) {
            json_event.addProperty("of.id", offerId);
        }
        return this;
    }

    public AnonymousEvent addSearch(String query, String language) {
        if (!TextUtils.isEmpty(query)) {
            json_event.addProperty("sea.q", query);
        }
        // language is optional in case is not possible to detect it
        if (!TextUtils.isEmpty(language)) {
            json_event.addProperty("sea.l", language);
        }
        return this;
    }


    /****** Getters and setters */

    public boolean doNotTrack() {
        return mDoNotTrack;
    }

    public AnonymousEvent doNotTrack(boolean doNotTrack) {
        mDoNotTrack = doNotTrack;
        return this;
    }

    public void track() {
            EventTracker.globalTracker().track(this);
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return json_event.toString();
    }

    public int getType() {
        return type;
    }

    public String getType(int type) {
        switch(type) {
            case PAGED_PUBLICATION_OPENED:
                return "paged_publication_opened";
            case PAGED_PUBLICATION_PAGE_DISAPPEARED:
                return "paged_publication_page_disappeared";
            case OFFER_OPENED:
                return "offer_opened";
            case CLIENT_SESSION_OPENED:
                return "client_session_opened";
            case SEARCHED:
                return "searched";
            default:
                return "custom_event";
        }
    }

    public String getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getVersion() {
        return VERSION;
    }
}
