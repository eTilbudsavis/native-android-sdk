/*******************************************************************************
* Copyright 2014 eTilbudsavis
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
*******************************************************************************/
package com.eTilbudsavis.etasdk;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

	public static final String TAG = "Settings";
	
	/** Name for the SDK SharedPreferences file */
	private static final String PREFS_NAME = "eta_sdk";
	
	private static final String SETTING_LAST_USAGE	= "last_usage";
	
	private static final String SESSION_JSON		= "session_json";
	private static final String SESSION_USER		= "session_user";
	private static final String SESSION_FACEBOOK	= "session_facebook";
	
	public static final String LOC_SENSOR			= "loc_sensor";
	public static final String LOC_LATITUDE			= "loc_latitude";
	public static final String LOC_LONGITUDE		= "loc_longitude";
	public static final String LOC_RADIUS			= "loc_radius";
	public static final String LOC_BOUND_EAST		= "loc_b_east";
	public static final String LOC_BOUND_NORTH		= "loc_b_north";
	public static final String LOC_BOUND_SOUTH		= "loc_b_south";
	public static final String LOC_BOUND_WEST		= "loc_b_west";
	public static final String LOC_ADDRESS			= "loc_address";
	public static final String LOC_TIME				= "loc_time";

	private static SharedPreferences mPrefs;
	private static Context mContext;
	
	public Settings(Context context) {
		mContext = context;
		mPrefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
	}

	public boolean clear() {
		return mPrefs.edit().clear().commit();
	}
	
	public JSONObject getSessionJson() {
		String json = mPrefs.getString(SESSION_JSON, null);
		JSONObject session = null;
		if (json != null) {
			try {
				return new JSONObject(json);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return session;
	}
	
	public long getLastUsage() {
		return mPrefs.getLong(SETTING_LAST_USAGE, 0L);
	}
	
	public boolean setLastUsageNow() {
		Date now = new Date();
		return mPrefs.edit().putLong(SETTING_LAST_USAGE, now.getTime()).commit();
	}
	
	public boolean setSessionJson(JSONObject session) {
		return mPrefs.edit().putString(SESSION_JSON, session.toString()).commit();
	}
	
	public String getSessionUser() {
		return mPrefs.getString(SESSION_USER, null);
	}
	
	public boolean setSessionUser(String user) {
		return mPrefs.edit().putString(SESSION_USER, user).commit();
	}
	
	public boolean setSessionFacebook(String token) {
		return mPrefs.edit().putString(SESSION_FACEBOOK, token).commit();
	}
	
	public String getSessionFacebook() {
		return mPrefs.getString(SESSION_FACEBOOK, null);
	}
	
	public SharedPreferences getPrefs() {
		return mPrefs;
	}
}
