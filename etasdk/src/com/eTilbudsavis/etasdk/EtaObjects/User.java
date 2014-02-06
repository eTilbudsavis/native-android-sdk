package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Utils.EtaLog;

public class User extends EtaObject implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String TAG = "User";
	
	/** Parameter for a user e-mail */
	public static final String PARAM_EMAIL = Request.Param.EMAIL;
	
	/** Parameter for a user password */
	public static final String PARAM_PASSWORD = Request.Param.PASSWORD;
	
	/** Parameter for a user birth year */
	public static final String PARAM_BIRTH_YEAR = Request.Param.BIRTH_YEAR;
	
	/** Parameter for a user gender */
	public static final String PARAM_GENDER = Request.Param.GENDER;
	
	/** Parameter for a user success redirect */
	public static final String PARAM_SUCCESS_REDIRECT = Request.Param.SUCCESS_REDIRECT;
	
	/** Parameter for a user error redirect */
	public static final String PARAM_ERROR_REDIRECT = Request.Param.ERROR_REDIRECT;
	
	/** Parameter for a user old password */
	public static final String PARAM_OLD_PASSWORD = Request.Param.OLD_PASSWORD;
	
	/** Parameter for a facebook token */
	public static final String PARAM_FACEBOOK_TOKEN = Request.Param.FACEBOOK_TOKEN;
	
	/** Endpoint for a user resource */
	public static final String ENDPOINT_RESET = Request.Endpoint.USER_RESET;
	
	public static final int NO_USER = -1;
	
	private int mId = NO_USER;
	private String mErn;
	private String mGender;
	private int mBirthYear = 0;
	private String mName;
	private String mEmail;
	private Permission mPermissions;
	private ArrayList<User.UserStatusListener> mSubscribers = new ArrayList<User.UserStatusListener>();

	public User() { }

	public static User fromJSON(String user) {	
		User u = new User();
		try {
			u = fromJSON(u, new JSONObject(user));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return u;
	}

	@SuppressWarnings("unchecked")
	public static User fromJSON(JSONObject user) {	
		return fromJSON(new User(), user);
	}
	
	private static User fromJSON(User u, JSONObject user) {
		if (u == null) u = new User();
		if (user == null) return u;
		
		try {
			u.setId(jsonToInt(user, S_ID, NO_USER));
			u.setErn(getJsonString(user, S_ERN));
			u.setGender(getJsonString(user, S_GENDER));
			u.setBirthYear(jsonToInt(user, S_BIRTH_YEAR, 0));
			u.setName(getJsonString(user, S_NAME));
			u.setEmail(getJsonString(user, S_EMAIL));
			u.setPermissions(Permission.fromJSON(user.getJSONObject(S_PERMISSIONS)));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
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
			EtaLog.d(TAG, e);
		}
		return o;
	}
	
	public boolean isLoggedIn() {
		return mEmail != null && mId > -1;
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
		if (gender != null) {
			gender = gender.toLowerCase();
			if (gender.equals("male") || gender.equals("female") )
				mGender = gender;
		}		
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
		return Request.Endpoint.facebook(mId);
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
	public boolean equals(Object o) {
		if (this == o)
			return true;
		
		if (!(o instanceof User))
			return false;

		User u = (User)o;
		return mId == u.getId() &&
				stringCompare(mErn, u.getErn()) &&
				stringCompare(mGender, u.getGender()) &&
				mBirthYear == u.getBirthYear() &&
				stringCompare(mName, u.getName()) &&
				stringCompare(mEmail, u.getEmail()) &&
				mPermissions.equals(u.getPermissions());
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