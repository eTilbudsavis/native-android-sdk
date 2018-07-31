package com.shopgun.android.sdk.eventskit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.shopgun.android.sdk.utils.SgnUtils;

import java.util.concurrent.TimeUnit;

import io.realm.RealmModel;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

/**
 * Wrapper for the database operations
 */
@RealmClass
public class AnonymousEventWrapper implements RealmModel {

    public static final String TAG = AnonymousEventWrapper.class.getSimpleName();

    @PrimaryKey private String id;
    private long timestamp;
    private String event; // string version of the json object saved into the database

    public AnonymousEventWrapper(String id, long timestamp, String event) {
        this.event = event;
        this.id = id;
        this.timestamp = timestamp;
    }

    public JsonObject getJsonEvent() {
        return parse(event);
    }

    private JsonObject parse(String json) {
        try {
            return (JsonObject) new JsonParser().parse(json);
        } catch (Exception e) {
            return null;
        }
    }

    /***** Default getters and setters */

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
