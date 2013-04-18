package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

public class User implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String TAG = "UserClass";
	private boolean mLoggedIn = false;
	private String mName;
	private String mEmail;
	private String mToken;
	private ArrayList<User.UserStatusListener> mSubscribers = new ArrayList<User.UserStatusListener>();

	public User() {
		
	}
	
	public void setUser(JSONObject userInfo) {
		try {
			mName = userInfo.getJSONObject("user").getString("name");
			mEmail = userInfo.getJSONObject("user").getString("email");
			mToken = userInfo.getString("token");
			mLoggedIn = true;
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void setFromHeartbeat(JSONObject userInfo) {
		try {
			mName = userInfo.getString("name");
			mEmail = userInfo.getString("email");
			mLoggedIn = true;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

	public User setToken(String token) {
		mToken = token;
		return this;
	}
	
	public User setName(String name) {
		mName = name;
		return this;
	}
	
	public User setEmail(String email) {
		mEmail = email;
		return this;
	}

	public User setLoggedIn(Boolean state) {
		mLoggedIn = state;
		return this;
	}
	
	public Boolean isLoggedIn() {
		return mLoggedIn;
	}

	public String getName() {
		return mName;
	}

	public String getEmail() {
		return mEmail;
	}

	public String getToken() {
		return mToken;
	}
	
	public void signin(UserStatusListener listener, String email, String password, Boolean remember) {
		
	}
	
	public void signout(UserStatusListener listener) {

		// Reset this user
		mLoggedIn = false;
		mEmail = null;
		mToken = null;
		mName = null;
		
	}
	
	public void signup(UserStatusListener listener, String name, String email, String password, Integer gender, Integer birthYear) {
		Bundle apiParams = new Bundle();
		apiParams.putString("name", name);
		apiParams.putString("email", email);
		apiParams.putString("password", password);
		apiParams.putString("gender", String.valueOf(gender));
		apiParams.putString("birthYear", String.valueOf(birthYear));
	}
	
	public Boolean heartbeat() {
		return heartbeat(statusListener);
	}
	
	/**
	 * Sends heartbeat to server if token is present.
	 * 
	 * The method will return true or false based on 
	 * whether or not a token is present, but transaction will not be complete
	 * until a callback is received via the provided listener.
	 * @param listener for callback.
	 * @return <li> True if token is present, else false.
	 */
	public Boolean heartbeat(UserStatusListener listener) {
		
		return false;
	}
	
	public void subscribe(UserStatusListener statusListener) {
		mSubscribers.add(statusListener);
	}

	public void unsubscribe(UserStatusListener statusListener) {
		mSubscribers.remove(statusListener);
	}
	
	private void notifySubscribers(Integer response, Object object) {
		for (UserStatusListener s : mSubscribers) {
			s.onStatusChange(response, object);
		}
	}
	
	public interface UserStatusListener {
		public void onStatusChange(Integer response, Object object);
	}
	
	/* An empty status change listener, for when no callback is needed */
	private UserStatusListener statusListener = new UserStatusListener() {
		 
		public void onStatusChange(Integer response, Object object) {
			// Empty listener
		}
	};

	@Override
	public String toString() {
		return new StringBuilder()
		.append("User { ")
		.append("Name: ").append(mName)
		.append(", E-mail: ").append(mEmail)
		.append(", LoggedIn: ").append(mLoggedIn)
		.append(", Token: ").append(mToken)
		.append(" }").toString();
	}

}