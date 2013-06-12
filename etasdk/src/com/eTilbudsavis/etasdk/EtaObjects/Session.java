package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import Utils.Endpoint;
import Utils.Params;
import Utils.Utilities;
import android.annotation.SuppressLint;
import android.os.Bundle;

import com.eTilbudsavis.etasdk.Api.CallbackString;
import com.eTilbudsavis.etasdk.Eta;

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

	private static final long EXPIRE_MINUTE = 60*1000;
	
	private static final long EXPIRE_DAY = 24*60*EXPIRE_MINUTE;

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
	
	private Eta mEta;
	private boolean mIsUpdatingSession = false;
	private ArrayList<SessionListener> mSubscribers = new ArrayList<Session.SessionListener>();

	CallbackString session = new CallbackString() {

		public void onComplete(int statusCode, String data, EtaError error) {
			
			if (200 <= statusCode || statusCode < 300 ) {
				set(data);
			} else {
				mEta.addError(error);
				Utilities.logd(TAG, "Error: " + String.valueOf(statusCode) + " - " + error.toString());
			}
			mIsUpdatingSession = false;
			notifySubscribers();
		}

	};

	CallbackString userCreate = new CallbackString() {

		public void onComplete(int statusCode, String data, EtaError error) {
			
			if (200 <= statusCode || statusCode < 300 ) {
				Utilities.logd(TAG, "Success: " + String.valueOf(statusCode) + " - " + data);
			} else {
				mEta.addError(error);
				Utilities.logd(TAG, "Error: " + String.valueOf(statusCode) + " - " + error);
			}
			notifySubscribers();
		}

	};
	
	public Session(Eta eta) {
		mEta = eta;
	}
	
	public void set(String session) {
		try {
			set(new JSONObject(session));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void set(JSONObject session) {
		mJson = session;
		try {
			mToken = session.getString(S_TOKEN);
		    setExpires(session.getString(S_EXPIRES));
		    mUser = session.getString(S_USER).equals("null") ? null : User.fromJSON(session.getJSONObject(S_USER));
		    mPermission = Permission.fromJSON(session.getJSONObject(S_PERMISSIONS));
		    mProvider = session.getString(S_PROVIDER);
			saveJSON();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void saveJSON() {
		mEta.getPrefs().edit().putString(PREFS_SESSION, mJson.toString()).commit();
	}
	
	/**
	 * This method will instantiate a new session.<br>
	 * 
	 * By letting users choose when to start a session, the user has a chance to
	 * set a SessionListener before executing() any calls, thereby avoiding errors.
	 */
	public void start() {
		// Try to get a session from SharedPreferences, and check if it's okay
		// If it doesn't exist or is invalid then update it.
		String sessionJson = mEta.getPrefs().getString(PREFS_SESSION, null);
		if (sessionJson != null) {
			
			set(sessionJson);
			if (isExpired()) {
				update();
			} else {
				notifySubscribers();
			}

		} else {
			update();
		}
	}

	public void update() {
		if (mIsUpdatingSession)
			return;
		
		mIsUpdatingSession = true;
		Bundle b = new Bundle();
		String u = mEta.getPrefs().getString(PREFS_SESSION_USER, null);
		String p = mEta.getPrefs().getString(PREFS_SESSION_PASS, null);
		if (u != null && p != null) {
			b.putString(Params.EMAIL, u);
			b.putString(Params.PASSWORD, p);
		}
		mEta.api().setUseLocation(false).post(Session.ENDPOINT, session, b).execute();
	}
	
	public void login(String user, String password) {
		mEta.getPrefs().edit().putString(PREFS_SESSION_USER, user).putString(PREFS_SESSION_PASS, password).commit();
		if (isExpired()) {
			Bundle b = new Bundle();
			b.putString(Params.EMAIL, user);
			b.putString(Params.PASSWORD, password);
			mEta.api().put(Session.ENDPOINT, session , b).execute();
		} else {
			update();
		}
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
	public boolean createUser(String email, String password, String name, int birthYear, String gender, String successRedirect, String errorRedirect) {
		if ( !Utilities.isEmailValid(email) || 
				!Utilities.isPasswordValid(password) || 
				!Utilities.isNameValid(name) || 
				!Utilities.isBirthyearValid(birthYear) || 
				!Utilities.isGenderValid(gender))
			return false;
		
		Bundle b = new Bundle();
		b.putString(Params.EMAIL, email);
		b.putString(Params.PASSWORD, password);
		b.putString(Params.NAME, name);
		b.putInt(Params.BIRTH_YEAR, birthYear);
		b.putString(Params.GENDER, gender);
		b.putString(Params.SUCCESS_REDIRECT, successRedirect);
		b.putString(Params.ERROR_REDIRECT, errorRedirect);
		mEta.api().post(Session.ENDPOINT, userCreate, b).execute();
		return true;
	}
	
	public boolean isExpired() {
		return mExpires < (System.currentTimeMillis() + EXPIRE_MINUTE);
	}
	
	/**
	 * @param token
	 * @param expires
	 * @return
	 */
	public synchronized void update(String headerToken, String headerExpires) {
		if (mIsUpdatingSession)
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
			long exp = 0L;
			exp = sdf.parse(headerExpires).getTime();
			if (exp < (System.currentTimeMillis()+EXPIRE_DAY)) {
				update();
				return;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * TODO: Implement this...
	 */
	public void signout() {
		mEta.getPrefs().edit()
		.putString(PREFS_SESSION_USER, null)
		.putString(PREFS_SESSION_PASS, null).commit();
	}
	
	/**
	 * TODO: Implement this...
	 */
	public Session invalidate() {
		mJson = null;
		mToken = null;
		mExpires = 0L;
		mUser = null;
		mPermission = null;
		mProvider = null;
		mSubscribers = new ArrayList<Session.SessionListener>();
		mEta.getPrefs().edit()
		.putString(PREFS_SESSION, null).commit();
		return this;
	}
	
	public JSONObject getJson() {
		return mJson;
	}
	
	public Session setToken(String token) {
		mToken = token;
		return this;
	}

	/**
	 * Get this Sessions token. Used for headers in API calls
	 * @return token as String if session is active, otherwise null.
	 */
	public String getToken() {
		return mToken;
	}

	public Session setUser(User user) {
		mUser = user;
		return this;
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
		mSubscribers.add(listener);
		return this;
	}
	
	public void unSubscribe(SessionListener listener) {
		mSubscribers.remove(listener);
	}
	
	public Session notifySubscribers() {
		for (SessionListener sl : mSubscribers)
			sl.onUpdate();

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
			.append(", permission=").append(mPermission.toString())
			.append(", provider=").append(mProvider);
		}
		return sb.append("]").toString();
	}
	
	public interface SessionListener {
		public void onUpdate();
	}

}
