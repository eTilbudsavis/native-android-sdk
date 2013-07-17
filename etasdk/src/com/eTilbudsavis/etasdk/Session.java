package com.eTilbudsavis.etasdk;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.location.GpsStatus.Listener;
import android.os.Bundle;

import com.eTilbudsavis.etasdk.Api.CallbackString;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.EtaObjects.Permission;
import com.eTilbudsavis.etasdk.EtaObjects.User;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.Params;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class Session implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final String S_TOKEN = "token";
	private static final String S_EXPIRES = "expires";
	private static final String S_USER = "user";
	private static final String S_PERMISSIONS = "permissions";
	private static final String S_PROVIDER = "provider";
	
	public static final String TAG = "Session";

	public static final String PREFS_SESSION = "session";
	public static final String PREFS_SESSION_USER = "session_user";
	public static final String PREFS_SESSION_PASS = "session_pass";

	/** API v2 Session endpoint */
	public static final String ENDPOINT = Endpoint.SESSION;
	
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat(Eta.DATE_FORMAT);
	
	private JSONObject mJson = null;
	private String mToken = null;
	private long mExpires = 0L;
	private User mUser = null;
	private Permission mPermission = null;
	private String mProvider;
	private String mUserStr = null;
	private String mPassStr = null;
	
	private Eta mEta;
	private boolean mIsUpdating = false;
	private ArrayList<SessionListener> mSubscribers = new ArrayList<Session.SessionListener>();
	private List<Api> mQueue = Collections.synchronizedList(new ArrayList<Api>());
	
	public Session(Eta eta) {
		
		mEta = eta;
		mUser = new User();
		
		// Try to get a session from SharedPreferences, and check if it's okay
		// If it doesn't exist or is invalid then update it.
		String sessionJson = mEta.getPrefs().getString(PREFS_SESSION, null);
		mUserStr = mEta.getPrefs().getString(PREFS_SESSION_USER, null);
		mPassStr = mEta.getPrefs().getString(PREFS_SESSION_PASS, null);
		if (sessionJson == null) {
			update();
		} else {
			set(sessionJson);
			if (isExpired()) {
				update();
			} 
		}
	}
	
	public void set(String session) {
		try {
			set(new JSONObject(session));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void set(JSONObject session) {
		
		try {
			mToken = session.getString(S_TOKEN);
		    setExpires(session.getString(S_EXPIRES));
		    if (!session.getString(S_USER).equals("null")) {
		    	mUser = User.fromJSON(session.getString(S_USER));
		    }
		    mPermission = Permission.fromJSON(session.getJSONObject(S_PERMISSIONS));
		    mProvider = session.getString(S_PROVIDER);
		    mJson = session;
			saveJSON();

			if (!mQueue.isEmpty()) {
				for (Api a : mQueue) {
					mQueue.remove(a);
					a.execute();
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	private void saveJSON() {
		new Thread() {
	        public void run() {
	        	mEta.getPrefs().edit().putString(PREFS_SESSION, mJson.toString()).commit();
	        }
		}.start();
	}
	
	public void login(String user, String password, final CallbackString listener) {
		// Save user and pass to preferences
		mEta.getPrefs().edit().putString(PREFS_SESSION_USER, user).putString(PREFS_SESSION_PASS, password).commit();
		mUserStr = user;
		mPassStr = password;
		update(listener);
	}
	
	public void forgotPassword(String email, final CallbackString listener) {
		// TODO: Forgot password implementation
	}

	private void sessionUpdate(int statusCode, String data, EtaError error) {

		if (Utils.isSuccess(statusCode)) {
			set(data);
		} else {
			Utils.logd(TAG, "Error: " + String.valueOf(statusCode) + " - " + error.toString());
		}
		mIsUpdating = false;
		notifySubscribers();
	}
	
	public void update() {
		update(null);
	}
	
	private synchronized void update(final CallbackString listener) {
		if (mIsUpdating)
			return;
		
		mIsUpdating = true;
		Bundle b = new Bundle();
		if (mUserStr != null && mPassStr != null) {
			b.putString(Params.EMAIL, mUserStr);
			b.putString(Params.PASSWORD, mPassStr);
		}
		CallbackString session = new CallbackString() {

			public void onComplete(int statusCode, String data, EtaError error) {
				sessionUpdate(statusCode, data, error);
				if (listener != null) listener.onComplete(statusCode, data, error);
			}

		};
		mEta.api().setUseLocation(false).post(Session.ENDPOINT, session, b).execute();
	}
	
	/**
	 * 
	 * @param email
	 * @param password
	 * @param name
	 * @param birthYear
	 * @param gender
	 * @param successRedirect
	 * @param errorRedirect
	 * @return true if all arguments are valid, false otherwise
	 */
	public boolean createUser(String email, String password, String name, int birthYear, String gender, String successRedirect, String errorRedirect, final CallbackString listener) {
		if ( !Utils.isEmailValid(email) || 
				!Utils.isPasswordValid(password) || 
				!Utils.isNameValid(name) || 
				!Utils.isBirthyearValid(birthYear) || 
				!Utils.isGenderValid(gender))
			return false;
		
		Bundle b = new Bundle();
		b.putString(Params.EMAIL, email);
		b.putString(Params.PASSWORD, password);
		b.putString(Params.NAME, name);
		b.putInt(Params.BIRTH_YEAR, birthYear);
		b.putString(Params.GENDER, gender);
		b.putString(Params.SUCCESS_REDIRECT, successRedirect);
		b.putString(Params.ERROR_REDIRECT, errorRedirect);
		
		CallbackString userCreate = new CallbackString() {

			public void onComplete(int statusCode, String data, EtaError error) {
				
				if (Utils.isSuccess(statusCode)) {
					Utils.logd(TAG, "Success: " + String.valueOf(statusCode) + " - " + data);
				} else {
					Utils.logd(TAG, "Error: " + String.valueOf(statusCode) + " - " + error);
				}
				if (listener != null) listener.onComplete(statusCode, data, error);
			}

		};
		
		mEta.api().post(Session.ENDPOINT, userCreate, b).execute();
		return true;
	}
	
	public boolean isExpired() {
		return mExpires < (System.currentTimeMillis() + Utils.MINUTE_IN_MILLIS);
	}

	public void addToQueue(Api api) {
		mQueue.add(api);
	}
	
	/**
	 * @param token
	 * @param expires
	 * @return
	 */
	public synchronized void update(String headerToken, String headerExpires) {
		
		if (mIsUpdating)
			return;
		
		if (mJson == null) {
			update();
			return;
		}
		
		try {
			if (!mToken.equals(headerToken) ) {
				mJson.put(S_TOKEN, headerToken);
				mJson.put(S_EXPIRES, headerExpires);
				set(mJson);
				return;
			}
			long expire = ( sdf.parse(headerExpires).getTime() - Utils.DAY_IN_MILLIS );
			if ( expire < System.currentTimeMillis()) {
				update();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Signs a user out, and cleans all references to the user.<br><br>
	 * A new {@link #login(String, String) login} is needed to get access to user stuff again.
	 */
	public synchronized void signout(final CallbackString listener) {
		mIsUpdating = true;
		clearUser();
		Bundle b = new Bundle();
		b.putString(Params.EMAIL, "");
		CallbackString session = new CallbackString() {

			public void onComplete(int statusCode, String data, EtaError error) {
				sessionUpdate(statusCode, data, error);
				if (listener != null) listener.onComplete(statusCode, data, error);
			}

		};
		mEta.api().put(ENDPOINT, session, b).execute();
	}
	
	private void clearUser() {
		mEta.getPrefs().edit()
		.putString(PREFS_SESSION_USER, null)
		.putString(PREFS_SESSION_PASS, null).commit();
		mUserStr = null;
		mPassStr = null;
		mUser = new User();
	}
	
	/**
	 * Destroys this session.<br><br>
	 * And returns a new session, completely clean session.
	 */
	public void invalidate(final CallbackString listener) {
		mJson = null;
		mToken = null;
		mExpires = 0L;
		mPermission = null;
		mProvider = null;
		mSubscribers = new ArrayList<Session.SessionListener>();
		mEta.getPrefs().edit().putString(PREFS_SESSION, null).commit();
		clearUser();
		CallbackString session = new CallbackString() {

			public void onComplete(int statusCode, String data, EtaError error) {
				sessionUpdate(statusCode, data, error);
				if (listener != null) listener.onComplete(statusCode, data, error);
			}

		};
		mEta.api().delete(ENDPOINT, session, new Bundle()).execute();
	}
	
	public JSONObject toJSON() {
		return mJson;
	}
	
	/**
	 * Get this Sessions token. Used for headers in API calls
	 * @return token as String if session is active, otherwise null.
	 */
	public String getToken() {
		return mToken;
	}

	public User getUser() {
		return mUser;
	}

	public Session setPermission(Permission permission) {
		mPermission = permission;
		return this;
	}

	public Permission getPermission() {
		return mPermission;
	}

	public Session setProvider(String provider) {
		mProvider = provider;
		return this;
	}

	public String getProvider() {
		return mProvider;
	}

	public Session setExpires(String time) {
	    try {
			mExpires = sdf.parse(time).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	    return this;
	}

	public Session setExpires(long time) {
	    mExpires = time;
	    return this;
	}

	public long getExpire() {
		return mExpires;
	}

	public String getExpireString() {
		return sdf.format(mExpires);
	}

	public Session subscribe(SessionListener listener) {
		if (!mSubscribers.contains(listener)) {
			mSubscribers.add(listener);
		}
		return this;
	}
	
	public void unSubscribe(SessionListener listener) {
		mSubscribers.remove(listener);
	}
	
	public Session notifySubscribers() {
		for (SessionListener sl : mSubscribers) {
			try {
				sl.onUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
			

		return this;
	}

	@Override
	public String toString() {
		return toString(false);
	}
	
	public String toString(boolean everything) {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append("[")
		.append("token=").append(mToken)
		.append(", expires=").append(getExpireString());
		
		if (everything) {
			sb.append(", user=").append(mUser == null ? "null" : mUser.toString(everything))
			.append(", permission=").append(mPermission == null ? null : mPermission.toString())
			.append(", provider=").append(mProvider);
		}
		return sb.append("]").toString();
	}
	
	public interface SessionListener {
		public void onUpdate();
	}
	
	
}
