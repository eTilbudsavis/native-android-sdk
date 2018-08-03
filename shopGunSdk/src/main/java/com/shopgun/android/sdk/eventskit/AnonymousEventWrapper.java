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

    // Timestamp of the event to evaluate its age at the dispatch time
    private long timestamp;

    // different version of events can co-exists in the same database.
    // Ideally, the dispatcher will be able to send events to different endpoints based on their version
    private int version;

    // string version of the json object saved into the database.
    // These are the info that will be sent to the server
    private String event;

    public AnonymousEventWrapper() { }

    public AnonymousEventWrapper(String id, int version, long timestamp, String event) {
        setId(id);
        setVersion(version);
        setTimestamp(timestamp);
        setEvent(event);
    }

    public JsonObject getJsonEvent() {
        return parse(event);
    }

    private JsonObject parse(String json) {
        try {
            return (JsonObject) new JsonParser().parse(json);
        } catch (Exception e) {
            return new JsonObject();
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
