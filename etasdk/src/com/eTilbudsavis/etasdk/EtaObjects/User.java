package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;


import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.Params;

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
	
	private static final String S_ID = "id";
	private static final String S_ERN = "ern";
	private static final String S_GENDER = "gender";
	private static final String S_BIRTH_YEAR = "birth_year";
	private static final String S_NAME = "name";
	private static final String S_EMAIL = "email";
	private static final String S_PERMISSIONS = "permissions";

	private int mId = 0;
	private String mErn;
	private String mGender;
	private int mBirthYear = 0;
	private String mName;
	private String mEmail;
	private Permission mPermissions;
	private ArrayList<User.UserStatusListener> mSubscribers = new ArrayList<User.UserStatusListener>();

	public User() {
	}

	public static User fromJSON(String user) {	
		User u = new User();
		try {
			u = fromJSON(u, new JSONObject(user));
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
		}
		return u;
	}

	public static User fromJSON(JSONObject user) {	
		return fromJSON(new User(), user);
	}
	
	private static User fromJSON(User u, JSONObject user) {
		if (u == null) u = new User();
		if (user == null) return u;
		
		try {
			u.setId(user.getInt(S_ID));
			u.setErn(user.getString(S_ERN));
			u.setGender(user.getString(S_GENDER));
			u.setBirthYear(user.getInt(S_BIRTH_YEAR));
			u.setName(user.getString(S_NAME));
			u.setEmail(user.getString(S_EMAIL));
			u.setPermissions(Permission.fromJSON(user.getJSONObject(S_PERMISSIONS)));
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
		}
		return u;
	}
	
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(User u) {
		JSONObject o = new JSONObject();
		try {
			o.put(S_ID, u.getId());
			o.put(S_ERN, u.getErn());
			o.put(S_GENDER, u.getGender());
			o.put(S_BIRTH_YEAR, u.getBirthYear());
			o.put(S_NAME, u.getName());
			o.put(S_EMAIL, u.getEmail());
			o.put(S_PERMISSIONS, u.getPermissions() == null ? null : u.getPermissions().toJSON());
		} catch (JSONException e) {
			if (Eta.DEBUG)
				e.printStackTrace();
		}
		return o;
	}
	
	public boolean isLoggedIn() {
		return mEmail != null && mId != 0;
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
		return toString(false);
	}
	
	public String toString(boolean everything) {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append("[")
		.append("id=").append(mId)
		.append(", email=").append(mEmail);
		
		if (everything) {
			sb.append(", name=").append(mName)
			.append(", gender=").append(mGender)
			.append(", birthyear=").append(mBirthYear)
			.append(", ern=").append(mErn)
			.append(", permission=").append(mPermissions == null ? null : mPermissions.toString());
		}
		return sb.append("]").toString();
	}
	
}