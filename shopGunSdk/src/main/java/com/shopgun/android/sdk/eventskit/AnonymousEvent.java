package com.shopgun.android.sdk.eventskit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.shopgun.android.sdk.utils.SgnUtils;
import com.shopgun.android.utils.TextUtils;

import java.util.concurrent.TimeUnit;

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
    public static final int FIRST_OFFER_CLICKED_AFTER_SEARCH = 6;
    public static final int SEARCH_TO_INTERACTION = 7;
    public static final int VIEWED_SEARCH_RESULT = 9;
    public static final int INCITO_PUBLICATION_OPENED = 11;

    private boolean mDoNotTrack;

    private JsonObject json_event;

    private int type;
    private String id;
    private long timestamp;

    /**
     * The following codes are reserved to sdk events:
     *      DEFAULT_TYPE = 0;
     *      PAGED_PUBLICATION_OPENED = 1;
     *      PAGED_PUBLICATION_PAGE_DISAPPEARED = 2;
     *      OFFER_OPENED = 3;
     *      CLIENT_SESSION_OPENED = 4;
     *      SEARCHED = 5;
     *      FIRST_OFFER_CLICKED_AFTER_SEARCH = 6;
     *      SEARCH_TO_INTERACTION = 7;
     *      VIEWED_SEARCH_RESULT = 9;
     *      INCITO_PUBLICATION_OPENED = 11;
     *
     * If you need to use Anonymous event for other purposes, you can use negative integer as type.
     *
     * @param type integer code for the json_event type.
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

    /**
     * Add a property to the internal json object.
     * This can be used for custom events used internally by the app; for events that needs to be sent
     * to SGN servers, please use the predefined methods that will add the expected fields properly.
     * @param property name of the member
     * @param value string value associated to the property
     * @return the updated event
     */
    public AnonymousEvent add(String property, String value) {
        if (property != null && value != null) {
            json_event.addProperty(property, value);
        }
        return this;
    }

    public AnonymousEvent add(String property, JsonArray value) {
        if (property != null && value != null) {
            json_event.add(property, value);
        }
        return this;
    }

    public AnonymousEvent add(String property, Number value) {
        if (property != null && value != null) {
            json_event.addProperty(property, value);
        }
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

    /**
     * Add user location field.
     * @param geohash String with 4 char representing the geohash
     * @param timestamp when the location fix was taken, in seconds
     * @return the updated event
     */
    public AnonymousEvent addUserLocation(String geohash, long timestamp) {
        if (!TextUtils.isEmpty(geohash) && timestamp > 0) {
            json_event.addProperty("l.h", geohash);
            json_event.addProperty("l.ht", timestamp);
        }
        return this;
    }

    /**
     * Add user country code field
     * @param countryCode an ISO 3166-1 alpha-2 encoded string, like "DK"
     * @return the updated event
     */
    public AnonymousEvent addUserCountry(String countryCode) {
        if (!TextUtils.isEmpty(countryCode)) {
            json_event.addProperty("l.c", countryCode);
        }
        return this;
    }

    /**
     * Add the view token field
     * @param viewToken string that uniquely identify the viewed content
     * @return the updated event
     */
    public AnonymousEvent addViewToken(String viewToken) {
        if (!TextUtils.isEmpty(viewToken)) {
            json_event.addProperty("vt", viewToken);
        }
        return this;
    }


    /****** Fields for predefined events */

    /**
     * Add the publication open field
     * @param publicationId String containing the id of the opened catalog
     * @return the updated event
     */
    public AnonymousEvent addPublicationOpened(String publicationId) {
        if (!TextUtils.isEmpty(publicationId)) {
            json_event.addProperty("pp.id", publicationId);
        }
        return this;
    }

    /**
     * Add the page open field
     * @param publicationId String containing the id of the opened catalog
     * @param page page number
     * @return the updated event
     */
    public AnonymousEvent addPageOpened(String publicationId, int page) {
        if (!TextUtils.isEmpty(publicationId) && page > 0) {
            this.addPublicationOpened(publicationId);
            json_event.addProperty("ppp.n", page);
        }
        return this;
    }

    /**
     * Add the offer open field
     * @param offerId String containing the id of the opened offer
     * @return the updated event
     */
    public AnonymousEvent addOfferOpened(String offerId) {
        if (!TextUtils.isEmpty(offerId)) {
            json_event.addProperty("of.id", offerId);
        }
        return this;
    }

    /**
     * Add the field relative to the search
     * @param query the text searched by the user
     * @param language optional: the language. Leave it empty if is not possible to detect it
     * @return the updated event
     */
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

    public AnonymousEvent addIncitoOpened(String publicationId, boolean pagedPublicationIsAvailable) {
        if (!TextUtils.isEmpty(publicationId)) {
            json_event.addProperty("ip.id", publicationId);
            json_event.addProperty("ip.paged", pagedPublicationIsAvailable);
        }
        return this;
    }


    /****** Getters and setters */

    /**
     * Getter for the tracking flag
     * @return true if the event won't be sent to the server, false otherwise.
     * Default value is false, so the event will be sent to the server.
     */
    public boolean doNotTrack() {
        return mDoNotTrack;
    }

    /**
     * Setter for the tracking flag. For custom event that won't be accepted by the server, set it to true
     * @param doNotTrack true if the event should not be sent to the server
     * @return the updated event
     */
    public AnonymousEvent doNotTrack(boolean doNotTrack) {
        mDoNotTrack = doNotTrack;
        return this;
    }

    /**
     * Insert the event into the tracker queue.
     */
    public void track() {
            EventTracker.globalTracker().track(this);
    }

    @Override
    public String toString() {
        return json_event.toString();
    }

    /**
     * Getter for the event type
     * @return integer representing the type
     */
    public int getType() {
        return type;
    }

    /**
     * Get the type in human readable format for logging purposes
     * @param type integer to be translated
     * @return the correspondent string
     */
    public String getType(int type) {
        switch(type) {
            case DEFAULT_TYPE:
                return "default_type";
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
            case INCITO_PUBLICATION_OPENED:
                return "incito_publication_opened";
            case SEARCH_TO_INTERACTION:
                return "search_to_interaction";
            case FIRST_OFFER_CLICKED_AFTER_SEARCH:
                return "first_offer_clicked_after_search";
            case VIEWED_SEARCH_RESULT:
                return "viewed_search_result";
            default:
                return "custom_event";
        }
    }

    /**
     * Get the UUID of the event
     * @return string UUID
     */
    public String getId() {
        return id;
    }

    /**
     * Get the timestamp of when the event was created
     * @return timestamp in seconds
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get the version that the event is compliant with
     * @return integer
     */
    public int getVersion() {
        return VERSION;
    }
}
