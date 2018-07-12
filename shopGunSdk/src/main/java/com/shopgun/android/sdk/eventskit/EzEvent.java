package com.shopgun.android.sdk.eventskit;

import com.google.gson.JsonObject;
import com.shopgun.android.sdk.SgnLocation.SgnGeoHash;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.log.SgnLog;

import java.util.Locale;

/**
 * Class that encapsulate a generic event for events that
 * - have a specific type
 * - require the common fields to be set
 *
 * Adds a method to send the event to the global tracker.
 */
public class EzEvent {

    public static final String TAG = EzEvent.class.getSimpleName();

    public static final int PAGED_PUBLICATION_OPENED = 1;
    public static final int PAGED_PUBLICATION_PAGE_OPENED = 2;
    public static final int OFFER_OPENED = 3;
    public static final int CLIENT_SESSION_OPENED = 4;
    public static final int SEARCHED = 5;

    protected Event mEvent;
    private boolean mDebug;

    public static EzEvent create(int type) {
        return new EzEvent(type);
    }

    protected EzEvent(int type) {
        mEvent = new Event();
        String type_to_string = typeToString(type);
        mEvent.setType(type, type_to_string);
        setUserLocationFields();
    }

    private String typeToString(int type) {
        switch(type) {
            case PAGED_PUBLICATION_OPENED:
                return "paged_publication_opened";
            case PAGED_PUBLICATION_PAGE_OPENED:
                return "paged_publication_page_opened";
            case OFFER_OPENED:
                return "offer_opened";
            case CLIENT_SESSION_OPENED:
                return "client_session_opened";
            case SEARCHED:
                return "searched";
            default:
                return "";
        }
    }

    private void setUserLocationFields() {
        SgnGeoHash sgnGeoHash = ShopGun.getInstance().getLocation().getGeoHash();
        mEvent.setGeoHash(sgnGeoHash.geoHash, sgnGeoHash.timestamp);

        String country = Locale.getDefault().getCountry();
        // in case we have the 3-digit area code, leave it empty
        mEvent.setCountry(country.length() > 2 ? "" : country);
    }

    public void setViewToken(String contentIdentity) {

        String localSecret = getLocalDeviceSecret();

        mEvent.setViewToken(generateToken(localSecret.concat(contentIdentity)));
    }

    private String generateToken(String data) {
        //todo
        return "";
    }

    private String getLocalDeviceSecret() {
        //todo: get the local secret
        return "";
    }

    public void setPayload(JsonObject payload) {
        mEvent.setPayload(payload);
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
