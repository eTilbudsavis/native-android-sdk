package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import Utils.Endpoint;
import Utils.Params;

public class User implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final String TAG = "User";
	
	/** Parameter for a user e-mail */
	public static final String PARAM_EMAIL = Params.EMAIL;

	/** Parameter for a user password */
	public static final String PARAM_PASSWORD = Params.PASSWORD;

	/** Parameter for a user birth year */
	public static final String PARAM_BIRTH_YEAR = Params.BIRTH_YEAR;

	/** Parameter for a user gender */
	public static final String PARAM_GENDER = Params.GENDER;

	/** Parameter for a user success redirect */
	public static final String PARAM_SUCCESS_REDIRECT = Params.SUCCESS_REDIRECT;

	/** Parameter for a user error redirect */
	public static final String PARAM_ERROR_REDIRECT = Params.ERROR_REDIRECT;

	/** Parameter for a user old password */
	public static final String PARAM_OLD_PASSWORD = Params.OLD_PASSWORD;

	/** Parameter for a facebook token */
	public static final String PARAM_FACEBOOK_TOKEN = Params.FACEBOOK_TOKEN;

	/** Endpoint for a single user resource */
	public static final String ENDPOINT_ID = Endpoint.USER_ID;

	/** Endpoint for a user resource */
	public static final String ENDPOINT_RESET = Endpoint.USER_RESET;

	private int mId;
	private String mErn;
	private String mGender;
	private int mBirthYear;
	private String mName;
	private String mEmail;
	private Permission mPermissions;
	private ArrayList<User.UserStatusListener> mSubscribers = new ArrayList<User.UserStatusListener>();

	public User() {
		
	}
	
	public User(JSONObject user) {
		try {
			mId = user.getInt("id");
			mErn = user.getString("ern");
			mGender = user.getString("gender");
			mBirthYear = user.getInt("birth_year");
			mName = user.getString("name");
			mEmail = user.getString("email");
			mPermissions = new Permission(user.getJSONObject("permissions"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public int getId() {
		return mId;
	}

	public User setId(int id) {
		mId = id;
		return this;
	}

	public String getErn() {
		return mErn;
	}

	public User setErn(String ern) {
		mErn = ern;
		return this;
	}

	public String getGender() {
		return mGender;
	}

	/**
	 * Gender can be either <code>male</code> or <code>female</code>
	 * @param gender of either male or female
	 * @return
	 */
	public User setGender(String gender) {
		String test = gender.toLowerCase();
		if (gender.equals("male") || gender.equals("female") )
			mGender = test;
		return this;
	}

	public int getBirthYear() {
		return mBirthYear;
	}

	public User setBirthYear(int birthYear) {
		mBirthYear = birthYear;
		return this;
	}

	public String getName() {
		return mName;
	}

	public User setName(String name) {
		mName = name;
		return this;
	}

	public String getEmail() {
		return mEmail;
	}

	public User setEmail(String email) {
		mEmail = email;
		return this;
	}

	public Permission getPermissions() {
		return mPermissions;
	}

	public void setPermissions(Permission permissions) {
		this.mPermissions = permissions;
	}

	public String getFacebookEndpoint() {
		return Endpoint.getFacebookEndpoint(mId);
	}
	
	public void subscribe(UserStatusListener statusListener) {
		mSubscribers.add(statusListener);
	}

	public void unsubscribe(UserStatusListener statusListener) {
		mSubscribers.remove(statusListener);
	}
	
	public void notifySubscribers(Integer response, Object object) {
		for (UserStatusListener s : mSubscribers) {
			s.onStatusChange(response, object);
		}
	}
	
	public interface UserStatusListener {
		public void onStatusChange(Integer response, Object object);
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
		.append("User { ")
		.append("Id: ").append(mName)
		.append(", Name: ").append(mName)
		.append(", E-mail: ").append(mEmail)
		.append(" }").toString();
	}

}