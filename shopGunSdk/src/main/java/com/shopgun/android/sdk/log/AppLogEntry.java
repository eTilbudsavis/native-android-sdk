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
import com.shopgun.android.sdk.api.Endpoints;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.impl.IgnoreResponseListener;
import com.shopgun.android.sdk.network.impl.JsonObjectRequest;
import com.shopgun.android.sdk.utils.Device;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AppLogEntry {

    public static final String TAG = Constants.getTag(AppLogEntry.class);

    public static boolean DEBUG = false;

    private final ShopGun mShopgun;
    private String mEmail;
    private List<Event> mEvents = new ArrayList<Event>();
    private boolean mSingleEntry = true;
    private String mEntryName;

    public AppLogEntry(ShopGun sgn, String entryName) {
        this(sgn, entryName, null);
    }

    public AppLogEntry(ShopGun sgn, String entryName, String email) {
        if (sgn == null) {
            throw new IllegalStateException("ShopGun cannot be null");
        }
        mShopgun = sgn;
        mEmail = email;
        mEntryName = entryName;
        if (mEmail == null) {
            mEmail = mShopgun.getUser().getEmail();
        }
    }

    public String getEmail() {
        return mEmail;
    }

    public List<Event> getEvents() {
        return mEvents;
    }

    public AppLogEntry setEvents(List<Event> events) {
        mEvents = events;
        return this;
    }

    public AppLogEntry addEvent(String name, JSONObject data) {
        addEvent(new Event(mShopgun, name).setData(data));
        return this;
    }

    public AppLogEntry addEvent(Event event) {
        mEvents.add(event);
        return this;
    }

    public AppLogEntry addEvents(List<Event> events) {
        mEvents.addAll(events);
        return this;
    }

    private JSONObject toJSON(JSONArray events) {

        try {

            JSONObject device = new JSONObject();
            device.put("useragent", Device.getDeviceInfo());

            JSONObject app = new JSONObject();
            String appVersion = mShopgun.getAppVersion();
            app.put("version", (appVersion == null ? "null" : appVersion));

            JSONObject eventLog = new JSONObject();
            eventLog.put("tag", mEmail);
            eventLog.put("device", device);
            eventLog.put("app", app);
            eventLog.put("events", events);

            return eventLog;

        } catch (JSONException ex) {
            // Blank
        }

        return new JSONObject();
    }

    private JSONObject createJSONArrayWrapper() {
        try {
            JSONObject o = new JSONObject();
            JSONArray a = new JSONArray();
            for (Event e : mEvents) {
                a.put(e.toJSON(false));
            }
            o.put(mEntryName, a);
            return o;
        } catch (JSONException e) {
            SgnLog.e(TAG, "Creating JSON wrapper failed", e);
        }
        return new JSONObject();
    }

    public AppLogEntry setSingleEntry(boolean singleEntry) {
        mSingleEntry = singleEntry;
        return this;
    }

    public JsonObjectRequest post() {

        List<Event> list;
        if (mSingleEntry) {
            Event e = new Event(mShopgun, mEntryName);
            e.setData(createJSONArrayWrapper());
            list = new ArrayList<Event>();
            list.add(e);
        } else {
            list = mEvents;
        }

        JSONArray eventList = Event.toJSON(list, false);

        JSONObject appLogObject = toJSON(eventList);

        JsonObjectRequest r = new JsonObjectRequest(Request.Method.POST, Endpoints.APP_LOG_ENDPOINT, appLogObject, new IgnoreResponseListener<JSONObject>());

        if (DEBUG) {
            mShopgun.add(r);
        }

        return r;
    }

}
