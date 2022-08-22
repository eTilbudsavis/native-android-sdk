package com.tjek.sdk.legacy;

import io.realm.RealmModel;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

// Wrapper used by the old db system
@RealmClass
public class AnonymousEventWrapper implements RealmModel {

    public static final String TAG = AnonymousEventWrapper.class.getSimpleName();

    @PrimaryKey private String id;
    private long timestamp;
    private int version;
    private String event;

    public AnonymousEventWrapper() { }

    public AnonymousEventWrapper(String id, int version, long timestamp, String event) {
        setId(id);
        setVersion(version);
        setTimestamp(timestamp);
        setEvent(event);
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
