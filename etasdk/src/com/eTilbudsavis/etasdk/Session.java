package com.eTilbudsavis.etasdk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.util.Log;

import com.eTilbudsavis.etasdk.Api.JsonObjectListener;
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

	/** API v2 Session endpoint */
	public static final String ENDPOINT = Endpoint.SESSIONS;
	
	private JSONObject mJson = null;
	private String mToken = null;
	private Date mExpires = new Date(0L);
	private User mUser = null;
	private Permission mPermission = null;
	private String mProvider;
	private String mUserStr = null;
	private String mPassStr = null;
	private String mFacebookToken = null;
	
	private Eta mEta;
	private boolean mIsUpdating = false;
	private ArrayList<SessionListener> mSubscribers = new ArrayList<Session.SessionListener>();
	private List<Api> mQueue = Collections.synchronizedList(new ArrayList<Api>());
	
	public Session(Eta eta) {
		mEta = eta;
		mUser = new User();
	}
	
	public void init() {

		JSONObject json = mEta.getSettings().getSessionJson();
		mUserStr = mEta.getSettings().getSessionUser();
		mPassStr = mEta.getSettings().getSessionPass();
		mFacebookToken = mEta.getSettings().getSessionFacebook();
		if (json == null) {
			update();
		} else {
			set(json);
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
		    mEta.getSettings().setSessionJson(mJson);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	    if (isExpired()) {
	    	update();
	    } else {
	    	for (Api a : mQueue) {
				mQueue.remove(a);
				a.execute();
			}
	    }
		
	}
	
	public void login(String user, String password, JsonObjectListener listener) {
		mUserStr = user;
		mPassStr = password;
		mEta.getSettings().setSessionUser(mUserStr);
		mEta.getSettings().setSessionPass(mPassStr);
		update(listener);
	}
	
	public void loginFacebook(String facebookAccessToken, JsonObjectListener listener) {
		mFacebookToken = facebookAccessToken;
		mEta.getSettings().setSessionFacebook(facebookAccessToken);
		update(listener);
	}
	
	public void forgotPassword(String email, final JsonObjectListener listener) {
		// TODO: Forgot password implementation
	}

	private void sessionUpdate(int statusCode, JSONObject data, EtaError error) {

		if (Utils.isSuccess(statusCode)) {
			set(data);
			if (mUser.isLoggedIn()) {
				mEta.getShoppinglistManager().startSync();
			}
		} else {
			Utils.logd(TAG, "Error: " + String.valueOf(statusCode) + " - " + error.toString());
		}
		mIsUpdating = false;
		
		notifySubscribers();
	}
	
	public void update() {
		update(null);
	}
	
	private synchronized void update(final JsonObjectListener listener) {

		if (mIsUpdating) {
			return;
		}

		mIsUpdating = true;

		Bundle b = new Bundle();
		if (mUserStr != null && mPassStr != null) {
			b.putString(Params.EMAIL, mUserStr);
			b.putString(Params.PASSWORD, mPassStr);
		} else if (mFacebookToken != null) {
			b.putString(Params.FACEBOOK_TOKEN, mFacebookToken);
		}
		JsonObjectListener session = new JsonObjectListener() {
			
			public void onComplete(boolean isCache, int statusCode, JSONObject data, EtaError error) {

				sessionUpdate(statusCode, data, error);
				if (listener != null) listener.onComplete(isCache, statusCode, data, error);
			}
		};
		mEta.getApi().post(Session.ENDPOINT, session, b).execute();
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
	public boolean createUser(String email, String password, String name, int birthYear, String gender, String successRedirect, String errorRedirect, final JsonObjectListener listener) {
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
		
		JsonObjectListener userCreate = new JsonObjectListener() {
			
			public void onComplete(boolean isCache, int statusCode, JSONObject data, EtaError error) {

				if (Utils.isSuccess(statusCode)) {
					Utils.logd(TAG, "Success: " + String.valueOf(statusCode) + " - " + data.toString());
				} else {
					Utils.logd(TAG, "Error: " + String.valueOf(statusCode) + " - " + error.toString());
				}
				if (listener != null) listener.onComplete(isCache, statusCode, data, error);
			}
		};

		mEta.getApi().post(Session.ENDPOINT, userCreate, b).execute();
		return true;
	}
	
	public boolean isExpired() {
		return mExpires.getTime() < (System.currentTimeMillis() + Utils.MINUTE_IN_MILLIS);
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
			if ( ( Utils.parseDate(headerExpires).getTime() - Utils.DAY_IN_MILLIS ) < System.currentTimeMillis()) {
				update();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Signs a user out, and cleans all references to the user.<br><br>
	 * A new {@link #login(String, String) login} is needed to get access to user stuff again.
	 */
	public synchronized void signout(final JsonObjectListener listener) {
		
		Utils.logd(TAG, "signout");
		
		mIsUpdating = true;
		
		clearUser();
		Bundle b = new Bundle();
		b.putString(Params.EMAIL, "");
		JsonObjectListener session = new JsonObjectListener() {
			
			public void onComplete(boolean isCache, int statusCode, JSONObject data, EtaError error) {
				sessionUpdate(statusCode, data, error);
				if (listener != null) listener.onComplete(isCache, statusCode, data, error);
			}
		};

		mEta.getApi().put(ENDPOINT, session, b).execute();
	}
	
	private void clearUser() {
		mUserStr = null;
		mPassStr = null;
		mFacebookToken = null;
		mEta.getSettings().setSessionUser(mUserStr);
		mEta.getSettings().setSessionPass(mPassStr);
		mEta.getSettings().setSessionFacebook(mFacebookToken);
		mUser = new User();
	}
	
	/**
	 * Destroys this session.<br><br>
	 * And returns a new session, completely clean session.
	 */
	public void invalidate(final JsonObjectListener listener) {
		mJson = null;
		mToken = null;
		mExpires = null;
		mPermission = null;
		mProvider = null;
		mSubscribers = new ArrayList<Session.SessionListener>();
		mEta.getSettings().setSessionJson(mJson);
		clearUser();
		JsonObjectListener session = new JsonObjectListener() {
			
			public void onComplete(boolean isCache, int statusCode, JSONObject data, EtaError error) {
				sessionUpdate(statusCode, data, error);
				if (listener != null) listener.onComplete(isCache, statusCode, data, error);
			}
		};

		mEta.getApi().delete(ENDPOINT, session, new Bundle()).execute();
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
	    mExpires = Utils.parseDate(time);
	    return this;
	}

	public Session setExpires(Date time) {
	    mExpires = time;
	    return this;
	}

	public Date getExpire() {
		return mExpires;
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
		.append(", expires=").append(Utils.formatDate(getExpire()));
		
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
