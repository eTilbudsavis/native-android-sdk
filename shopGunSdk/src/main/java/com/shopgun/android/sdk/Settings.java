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

package com.shopgun.android.sdk;

import android.content.Context;
import android.content.SharedPreferences;

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.utils.SharedPreferencesUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Settings {

    public static final String TAG = Constants.getTag(Settings.class);

    /** Name for the SDK SharedPreferences file */
    private static final String PREFS_NAME = "com.shopgun.android.sdk_preferences";
    private static final String LAST_USED_VERSION = "last_used_version";
    private static final String LAST_USED_TIME = "last_used_time";
    private static final String USAGE_COUNT = "usage_count";
    private static final String SESSION_JSON = "session_json";
    private static final String SESSION_USER = "session_user";
    private static final String SESSION_FACEBOOK = "session_facebook";
    private static final String LOCATION = "location_json";
    private static final String CLIENT_ID = "client_id";

    private SharedPreferences mSharedPrefs;
    private static boolean mMovedSharedPrefs = false;

    public Settings(Context context) {
        mSharedPrefs = getPrefs(context);
        performMigration();
    }

    public static SharedPreferences getPrefs(Context context) {
        if (!mMovedSharedPrefs) {
            mMovedSharedPrefs = true;
            SharedPreferencesUtils.moveSharedPreferences(context, "eta_sdk", PREFS_NAME);
        }
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean clear() {
        return mSharedPrefs.edit().clear().commit();
    }

    public JSONObject getSessionJson() {
        try {
            String json = mSharedPrefs.getString(SESSION_JSON, null);
            return json == null ? null : new JSONObject(json);
        } catch (JSONException e) {
            // ignore
        }
        return null;
    }

    public void incrementUsageCount() {
        mSharedPrefs.edit().putInt(USAGE_COUNT, mSharedPrefs.getInt(USAGE_COUNT, 0)).apply();
    }

    public int getUsageCount() {
        return mSharedPrefs.getInt(USAGE_COUNT, 0);
    }

    public long getLastUsedTime() {
        return mSharedPrefs.getLong(LAST_USED_TIME, 0L);
    }

    public void setLastUsedTimeNow() {
        setLastUsedTime(new Date());
    }

    public void setLastUsedTime(Date lastUsed) {
        mSharedPrefs.edit().putLong(LAST_USED_TIME, lastUsed.getTime()).apply();
    }

    public void setSessionJson(JSONObject session) {
        mSharedPrefs.edit().putString(SESSION_JSON, session.toString()).apply();
    }

    public String getSessionUser() {
        return mSharedPrefs.getString(SESSION_USER, null);
    }

    public boolean setSessionUser(String user) {
        return mSharedPrefs.edit().putString(SESSION_USER, user).commit();
    }

    public boolean setSessionFacebook(String token) {
        return mSharedPrefs.edit().putString(SESSION_FACEBOOK, token).commit();
    }

    public String getSessionFacebook() {
        return mSharedPrefs.getString(SESSION_FACEBOOK, null);
    }

    public boolean saveLocation(SgnLocation l) {
        String loc = l.toJSON().toString();
        return mSharedPrefs.edit().putString(LOCATION, loc).commit();
    }

    public SgnLocation getLocation() {
        try {
            String s = mSharedPrefs.getString(LOCATION, null);
            if (s != null) {
                return SgnLocation.fromJSON(new JSONObject(s));
            }
        } catch (JSONException e) {
            SgnLog.e(TAG, "Not able to parse location json from SharedPreferences", e);
        }
        return new SgnLocation();
    }

    public String getClientId() {
        return mSharedPrefs.getString(CLIENT_ID, null);
    }

    public void setClientId(String clientId) {
        mSharedPrefs.edit().putString(CLIENT_ID, clientId).apply();
    }

    private void performMigration() {
        int version = mSharedPrefs.getInt(LAST_USED_VERSION, 0);
        if (version == ShopGun.VERSION.getCode()) {
            // no migration needed
            return;
        }
        SharedPreferences.Editor e = mSharedPrefs.edit();
        if (version == 0) {
            // The first time we create s new setting with the shopgun namespace, there if no keys, hence
            // the version number returned is 0. So we'll only have to run this code once.
            migrateLocation();
            // migrate last-used-key to a new name
            e.putLong(LAST_USED_TIME, mSharedPrefs.getLong("last_usage", 0));
            e.remove("last_usage");
        }
        e.putInt(LAST_USED_VERSION, ShopGun.VERSION.getCode());
        e.apply();
    }

    private void migrateLocation() {

        if (!mSharedPrefs.contains("loc_radius")) {
            return;
        }

        SgnLocation l = new SgnLocation();
        l.setSensor(mSharedPrefs.getBoolean("loc_sensor", false));
        l.setRadius(mSharedPrefs.getInt("loc_radius", Integer.MAX_VALUE));
        l.setLatitude(mSharedPrefs.getFloat("loc_latitude", 0.0f));
        l.setLongitude(mSharedPrefs.getFloat("loc_longitude", 0.0f));
        double east = mSharedPrefs.getFloat("loc_b_east", 0.0f);
        double west = mSharedPrefs.getFloat("loc_b_west", 0.0f);
        double north = mSharedPrefs.getFloat("loc_b_north", 0.0f);
        double south = mSharedPrefs.getFloat("loc_b_south", 0.0f);
        l.setBounds(north, east, south, west);
        l.setAddress(mSharedPrefs.getString("loc_address", null));
        l.setTime(mSharedPrefs.getLong("loc_time", System.currentTimeMillis()));
        saveLocation(l);

        SharedPreferences.Editor e = mSharedPrefs.edit();
        e.remove("loc_address");
        e.remove("loc_b_east");
        e.remove("loc_b_north");
        e.remove("loc_b_south");
        e.remove("loc_b_west");
        e.remove("loc_latitude");
        e.remove("loc_longitude");
        e.remove("loc_radius");
        e.remove("loc_sensor");
        e.remove("loc_time");
        e.apply();
    }

}
