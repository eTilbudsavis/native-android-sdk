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

import android.content.Context;
import android.content.SharedPreferences;

import com.eTilbudsavis.etasdk.log.EtaLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class Settings {

	public static final String TAG = Constants.getTag(Settings.class);
	
	/** Name for the SDK SharedPreferences file */
	private static final String PREFS_NAME = "eta_sdk";
	
	private static final String SETTING_LAST_USAGE	= "last_usage";
	
	private static final String SESSION_JSON		= "session_json";
	private static final String SESSION_USER		= "session_user";
	private static final String SESSION_FACEBOOK	= "session_facebook";
	private static final String LOCATION			= "location_json";
	
	private static final String LOC_SENSOR			= "loc_sensor";
	private static final String LOC_LATITUDE		= "loc_latitude";
	private static final String LOC_LONGITUDE		= "loc_longitude";
	private static final String LOC_RADIUS			= "loc_radius";
	private static final String LOC_BOUND_EAST		= "loc_b_east";
	private static final String LOC_BOUND_NORTH		= "loc_b_north";
	private static final String LOC_BOUND_SOUTH		= "loc_b_south";
	private static final String LOC_BOUND_WEST		= "loc_b_west";
	private static final String LOC_ADDRESS			= "loc_address";
	private static final String LOC_TIME			= "loc_time";

	private static SharedPreferences mPrefs;
	
	public Settings(Context context) {
		mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
	
	private EtaLocation migrateLocation() {

		EtaLocation l = null;
		
		if ( mPrefs.contains(LOC_RADIUS) &&
				mPrefs.contains(LOC_LATITUDE) && 
				mPrefs.contains(LOC_LONGITUDE) ) {
			
			l = new EtaLocation();
			l.setSensor(mPrefs.getBoolean(LOC_SENSOR, false));
			l.setRadius(mPrefs.getInt(LOC_RADIUS, Integer.MAX_VALUE));
			l.setLatitude(mPrefs.getFloat(LOC_LATITUDE, 0.0f));
			l.setLongitude(mPrefs.getFloat(LOC_LONGITUDE, 0.0f));
			double east = mPrefs.getFloat(LOC_BOUND_EAST, 0.0f);
			double west = mPrefs.getFloat(LOC_BOUND_WEST, 0.0f);
			double north = mPrefs.getFloat(LOC_BOUND_NORTH, 0.0f);
			double south = mPrefs.getFloat(LOC_BOUND_SOUTH, 0.0f);
			l.setBounds(north, east, south, west);
			l.setAddress(mPrefs.getString(LOC_ADDRESS, null));
			l.setTime(mPrefs.getLong(LOC_TIME, System.currentTimeMillis()));
			
			mPrefs.edit()
			.remove(LOC_ADDRESS)
			.remove(LOC_BOUND_EAST)
			.remove(LOC_BOUND_NORTH)
			.remove(LOC_BOUND_SOUTH)
			.remove(LOC_BOUND_WEST)
			.remove(LOC_LATITUDE)
			.remove(LOC_LONGITUDE)
			.remove(LOC_RADIUS)
			.remove(LOC_SENSOR)
			.remove(LOC_TIME)
			.commit();
			
			saveLocation(l);
			
		}
		return l;
		
	}
	
	public boolean saveLocation(EtaLocation l) {
		String loc = l.toJSON().toString();
		return mPrefs.edit().putString(LOCATION, loc).commit();
	}
	
	public EtaLocation getLocation() {
		
		if (mPrefs.contains(LOCATION)) {
			
			try {
				String loc = mPrefs.getString(LOCATION, null);
				
				if (loc != null) {
					JSONObject jLoc = new JSONObject(loc);
					return new EtaLocation(jLoc);
				}
			} catch (JSONException e) {
				EtaLog.e(TAG, "Not able to parse location json from SharedPreferances", e);
			}
			
		}
		
		EtaLocation loc = migrateLocation();
		return loc == null ? new EtaLocation() : loc;
	}
	
}
