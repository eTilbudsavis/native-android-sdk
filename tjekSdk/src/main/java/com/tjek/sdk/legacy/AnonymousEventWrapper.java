package com.tjek.sdk.legacy;
/*
 * Copyright (C) 2022 Tjek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
