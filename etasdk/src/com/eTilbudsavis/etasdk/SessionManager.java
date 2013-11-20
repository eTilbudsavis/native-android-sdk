package com.eTilbudsavis.etasdk;

import java.util.ArrayList;

import org.json.JSONObject;

import android.os.Bundle;

import com.eTilbudsavis.etasdk.EtaObjects.Session;
import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;
import com.eTilbudsavis.etasdk.NetworkHelpers.JsonObjectRequest;
import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Endpoint;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class SessionManager {
	
	public static final String TAG = "SessionManager";
	
	private Eta mEta;
	private Session mSession = new Session();
	private String mUserStr = null;
	private String mPassStr = null;
	private String mFacebookToken = null;
	private boolean mIsUpdating = false;
	private Object LOCK = new Object();
	private ArrayList<OnSessionChangeListener> mSubscribers = new ArrayList<OnSessionChangeListener>();
	
	public SessionManager(Eta eta) {
		mEta = eta;
		Settings s = mEta.getSettings();
		mUserStr = s.getSessionUser();
		mPassStr = s.getSessionPass();
		mFacebookToken = s.getSessionFacebook();
		mSession = Session.fromJSON(s.getSessionJson());
		if (mSession.isExpired()) {
			sendSessionRequest(getParams(), null);
		}
	}

	private void sendSessionRequest(Bundle params, final Listener<String> l) {
		
		mIsUpdating = true;
		// TODO: Actually do the request and set mIsUpdating = false
		
	}

	public void update(Listener<String> l) {
		
		synchronized (LOCK) {
			
			if (!mIsUpdating) {
				sendSessionRequest(getParams(), l);
			}
			
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
	public void createUser(String email, String password, String name, int birthYear, String gender, String successRedirect, String errorRedirect, Listener<String> listener) {
		
		Bundle b = new Bundle();
		b.putString(Request.Param.EMAIL, email);
		b.putString(Request.Param.PASSWORD, password);
		b.putString(Request.Param.NAME, name);
		b.putInt(Request.Param.BIRTH_YEAR, birthYear);
		b.putString(Request.Param.GENDER, gender);
		b.putString(Request.Param.SUCCESS_REDIRECT, successRedirect);
		b.putString(Request.Param.ERROR_REDIRECT, errorRedirect);
		sendSessionRequest(b, listener);
	}
	
	public void login(String user, String password, Listener<String> l) {
		
		synchronized (LOCK) {
			
			mUserStr = user;
			mPassStr = password;
			mEta.getSettings().setSessionUser(mUserStr);
			mEta.getSettings().setSessionPass(mPassStr);
			sendSessionRequest(getParams(), l);
			
		}
	}
	
	public void loginFacebook(String facebookAccessToken, Listener<String> l) {
		
		synchronized (LOCK) {
			
			mFacebookToken = facebookAccessToken;
			mEta.getSettings().setSessionFacebook(facebookAccessToken);
			sendSessionRequest(null, l);
			
		}
		
	}
	
	public void forgotPassword(String email, String successRedirect, String errorRedirect, Listener<String> listener) {
		
		Bundle b = new Bundle();
		b.putString(Request.Param.EMAIL, email);
		b.putString(Request.Param.SUCCESS_REDIRECT, successRedirect);
		b.putString(Request.Param.ERROR_REDIRECT, errorRedirect);
		
		JsonObjectRequest forgot = new JsonObjectRequest(Endpoint.USER_RESET, new Listener<JSONObject>() {

			public void onComplete(boolean isCache, JSONObject response, EtaError error) {
				
			}
		});
						
	}
	
	/**
	 * Signs a user out, and cleans all references to the user.<br><br>
	 * A new {@link #login(String, String) login} is needed to get access to user stuff again.
	 */
	public void signout(final Listener<String> listener) {
		
		synchronized (LOCK) {
			// On signout, clear current user details, then get new from server
			clearUser();
//			mEta.getShoppinglistManager().clearUserDB();
			Bundle b = new Bundle();
			b.putString(Request.Param.EMAIL, "");
			sendSessionRequest(getParams(), listener);
			
		}
		
	}
	
	public Session getSession() {
		return mSession;
	}
	
	public SessionManager setSession(Session s) {
		mSession = s;
		mEta.getSettings().setSessionJson(mSession.toJSON());
		return this;
	}
	
	public Bundle getParams() {
		
		Bundle b = new Bundle();
		if (mUserStr != null && mPassStr != null) {
			b.putString(Request.Param.EMAIL, mUserStr);
			b.putString(Request.Param.PASSWORD, mPassStr);
		} else if (mFacebookToken != null) {
			b.putString(Request.Param.FACEBOOK_TOKEN, mFacebookToken);
		}
		return b;
	}
	
	/**
	 * @param token
	 * @param expires
	 * @return
	 */
	public void updateTokens(String headerToken, String headerExpires) {
		
		synchronized (LOCK) {
			
			if (!mSession.getToken().equals(headerToken) ) {
				mSession.setToken(headerToken);
				mSession.setExpires(headerExpires);
				mEta.getSettings().setSessionJson(mSession.toJSON());
				return;
			} else if ( ( Utils.parseDate(headerExpires).getTime() - Utils.DAY_IN_MILLIS ) < System.currentTimeMillis()) {
				sendSessionRequest(getParams(), null);
			}
			
		}
		
	}
	

	/**
	 * Destroys this session.<br>
	 * A new session will be generated, on first request to server.
	 */
	public void invalidate() {
		synchronized (LOCK) {
			mSession = new Session();
			mEta.getSettings().setSessionJson(mSession.toJSON());
			clearUser();
		}
	}
	
	/**
	 * Clear all eta-user details
	 */
	private void clearUser() {
		mUserStr = null;
		mPassStr = null;
		mFacebookToken = null;
		mEta.getSettings().setSessionUser(mUserStr);
		mEta.getSettings().setSessionPass(mPassStr);
		mEta.getSettings().setSessionFacebook(mFacebookToken);
	}
	
	public SessionManager subscribe(OnSessionChangeListener l) {
		if (!mSubscribers.contains(l)) {
			mSubscribers.add(l);
		}
		return this;
	}
	
	public void unSubscribe(OnSessionChangeListener l) {
		mSubscribers.remove(l);
	}
	
	public SessionManager notifySubscribers() {
		for (OnSessionChangeListener sl : mSubscribers) {
			try {
				sl.onUpdate();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this;
	}

	public interface OnSessionChangeListener {
		public void onUpdate();
	}
	
}
