package com.eTilbudsavis.etasdk.EtaObjects;

import Utils.Endpoint;
import Utils.Params;
import Utils.Utilities;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Api;
import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.Api.RequestListener;

public class Session implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public static final String TAG = "Session";

	public static final String PREFS_SESSION = "session";
	public static final String PREFS_SESSION_USER = "session_user";
	public static final String PREFS_SESSION_PASS = "session_pass";

	private static final long EXPIRE_MINUTE = 60*1000;
	
	private static final long EXPIRE_DAY = 24*60*EXPIRE_MINUTE;

	/** API v2 Session endpoint */
	public static final String ENDPOINT = Endpoint.SESSION;
	
	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat(Eta.ETA_DATE_FORMAT);
	
	private JSONObject mJson = null;
	private String mToken = null;
	private long mExpires = 0L;
	private User mUser = null;
	private Permission mPermission = null;
	private String mProvider;
	
	private Eta mEta;
	private String mUsername = null;
	private String mPassword = null;
	private boolean mIsUpdatingSession = false;
	private ArrayList<SessionListener> mSubscribers = new ArrayList<Session.SessionListener>();

	RequestListener session = new RequestListener() {

		public void onComplete(int statusCode, Object object) {
			try {
				if (200 <= statusCode || statusCode < 300 ) {
					set(new JSONObject(object.toString()));
					Utilities.logd(TAG, "Success: " + String.valueOf(statusCode) + " - " + object.toString());
				} else {
					mEta.addError(new EtaError(new JSONObject(object.toString())));
					Utilities.logd(TAG, "Error: " + String.valueOf(statusCode) + " - " + object.toString());
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			mIsUpdatingSession = false;
			notifySubscribers();
		}
	};

	RequestListener userCreate = new RequestListener() {

		public void onComplete(int statusCode, Object object) {
			try {
				if (200 <= statusCode || statusCode < 300 ) {
					Utilities.logd(TAG, "Success: " + String.valueOf(statusCode) + " - " + object.toString());
				} else {
					mEta.addError(new EtaError(new JSONObject(object.toString())));
					Utilities.logd(TAG, "Error: " + String.valueOf(statusCode) + " - " + object.toString());
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			notifySubscribers();
		}
	};
	
	public Session(Eta eta) {
		mEta = eta;
	}
	
	public void set(JSONObject session) {
		mJson = session;
		try {
			mToken = session.getString("token");
		    setExpires(session.getString("expires"));
		    mUser = session.getString("user").equals("null") ? null : new User(session.getJSONObject("user"));
		    mPermission = new Permission(session.getJSONObject("permissions"));
		    mProvider = session.getString("provider");
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
			try {
				set(new JSONObject(sessionJson));
				if (isSessionGood()) {
					update();
				}
			} catch (JSONException e) {
				e.printStackTrace();
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
		mUsername = mEta.getPrefs().getString(PREFS_SESSION_USER, null);
		mPassword = mEta.getPrefs().getString(PREFS_SESSION_PASS, null);
		if (mUsername != null && mPassword != null) {
			b.putString(Params.EMAIL, mUsername);
			b.putString(Params.PASSWORD, mPassword);
		}
		new Api(mEta).setUseLocation(false).post(Session.ENDPOINT, session, b).execute();
	}
	
	public void login(String user, String password) {
		mUsername = user;
		mPassword = password;
		mEta.getPrefs().edit().putString(PREFS_SESSION_USER, mUsername).putString(PREFS_SESSION_PASS, mPassword).commit();
		if (isSessionGood()) {
			Bundle b = new Bundle();
			b.putString(Params.EMAIL, mUsername);
			b.putString(Params.PASSWORD, mPassword);
			new Api(mEta).put(Session.ENDPOINT, session , b).execute();
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
		new Api(mEta).post(Session.ENDPOINT, userCreate, b).execute();
		return true;
	}
	
	public boolean isSessionGood() {
		return mExpires < (System.currentTimeMillis() + EXPIRE_MINUTE);
	}
	
	/**
	 * @param token
	 * @param expires
	 * @return
	 */
	public boolean updateOnInvalidToken(String headerToken, String headerExpires) {
		if (mIsUpdatingSession)
			return false;

		try {
			if (!mToken.equals(headerToken) ) {
				mJson.put("token", headerToken);
				mJson.put("expires", headerExpires);
				set(mJson);
				return true;
			}
			long exp = 0L;
			exp = sdf.parse(headerExpires).getTime();
			if (exp < (System.currentTimeMillis()+EXPIRE_DAY)) {
				update();
				return true;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void signout() {
		mUsername = null;
		mPassword = null;
		mEta.getPrefs().edit()
		.putString(PREFS_SESSION_USER, null)
		.putString(PREFS_SESSION_PASS, null).commit();
	}
	
	public void invalidate() {
		mJson = null;
		mToken = null;
		mExpires = 0L;
		mUser = null;
		mPermission = null;
		mProvider = null;
		mSubscribers = new ArrayList<Session.SessionListener>();
		mEta.getPrefs().edit()
		.putString(PREFS_SESSION, null).commit();

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

	public void subscribe(SessionListener listener) {
		mSubscribers.add(listener);
	}
	
	public void unSubscribe(SessionListener listener) {
		mSubscribers.remove(listener);
	}
	
	public void notifySubscribers() {
		for (SessionListener sl : mSubscribers)
			sl.onUpdate();
	}
	
	/**
	 * Prints this object
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ ")
		.append("Token: ").append(mToken)
		.append(", Expires: ").append(mExpires)
		.append(", User: ").append(mUser)
//		.append(", Permissions: ").append(mPermission.toString())
		.append(" }");
		return sb.toString();
	}
	
	public interface SessionListener {
		public void onUpdate();
	}

}
