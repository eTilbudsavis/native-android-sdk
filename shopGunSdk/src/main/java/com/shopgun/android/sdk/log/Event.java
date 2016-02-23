/*******************************************************************************
 * Copyright 2015 ShopGun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.shopgun.android.sdk.log;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.utils.SgnJson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Date;
import java.util.Comparator;
import java.util.List;

public class Event {

    public static final String TAG = Constants.getTag(Event.class);

    public static final String TYPE_REQUEST = "request";
    public static final String TYPE_EXCEPTION = "exception";
    public static final String TYPE_VIEW = "view";
    public static final String TYPE_LOG = "log";

    private long mTime;
    private String mType;
    private String mSessionToken;
    private String mUserErn;
    private String mName;
    private JSONObject mData;

    public Event(ShopGun sgn, String name) {
        this(sgn, name, TYPE_LOG);
    }

    /**
     * @deprecated Use {@link Event#Event(ShopGun, String)}
     */
    @Deprecated
    public Event(String name) {
        this(name, TYPE_LOG);
    }

    /**
     * @deprecated Use {@link Event#Event(ShopGun, String)}
     */
    @Deprecated
    public Event(String name, String type) {
        this(ShopGun.isCreated() ? ShopGun.getInstance() : null, name, type);
    }

    public Event(ShopGun sgn, String name, String type) {
        if (sgn != null) {
            mUserErn = sgn.getUser().getErn();
            mSessionToken = sgn.getSessionManager().getSession().getToken();
        }
        setName(name);
        setType(type);
        mTime = System.currentTimeMillis();
    }

    public long getTime() {
        return mTime;
    }

    public Event setTime(long time) {
        this.mTime = time;
        return this;
    }

    public String getType() {
        return mType;
    }

    public Event setType(String type) {
        this.mType = type == null ? TYPE_LOG : type;
        return this;
    }

    public String getToken() {
        return mSessionToken;
    }

    public Event setToken(String token) {
        this.mSessionToken = token;
        return this;
    }

    public String getUser() {
        return mUserErn;
    }

    public Event setUser(String userErn) {
        this.mUserErn = userErn;
        return this;
    }

    public String getName() {
        return mName;
    }

    public Event setName(String name) {
        this.mName = name == null ? "unknown" : name;
        return this;
    }

    public JSONObject getData() {
        return mData;
    }

    public Event setData(JSONObject data) {
        this.mData = data;
        return this;
    }

    /**
     * Convert this object into a JSONObject representation
     * @param rawTime {@code true} to add timestamps as milliseconds, {@code false} to use a human readable string.
     * @return A JSONObject
     */
    public JSONObject toJSON(boolean rawTime) {
        SgnJson o = new SgnJson();
        if (rawTime) {
            o.put("timestamp", mTime);
        } else {
            o.putDate("timestamp", new Date(mTime));
        }
        o.put("type", mType);
        o.put("token", mSessionToken);
        o.put("userid", mUserErn);
        o.put("name", mName);
        o.put("data", mData);
        return o.toJSON();
    }

    /**
     * Create a JSONArray of the currents events
     *
     * @param events to torn into a JSONArray
     * @param rawTime {@code true} to add timestamps as milliseconds singe 1970, {@code false} to use a human readable string.
     * @return a JSONArray
     */
    public static JSONArray toJSON(List<Event> events, boolean rawTime) {
        JSONArray jArray = new JSONArray();
        if (events != null && !events.isEmpty()) {
            for (Event e : events) {
                jArray.put(e.toJSON(rawTime));
            }
        }
        return jArray;
    }

    public static Comparator<Event> timestamp = new Comparator<Event>() {

        public int compare(Event e1, Event e2) {

            if (e1 == null || e2 == null) {
                return e1 == null ? (e2 == null ? 0 : 1) : -1;
            } else {
                return e1.getTime() < e2.getTime() ? -1 : 1;
            }

        }

    };

}
