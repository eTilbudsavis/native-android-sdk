package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;

import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Json;

public class User extends EtaObject implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	public static final String TAG = "User";
	
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
	
	public static User fromJSON(JSONObject user) {	
		return fromJSON(new User(), user);
	}
	
	private static User fromJSON(User u, JSONObject user) {
		if (u == null) u = new User();
		if (user == null) return u;
		
		try {
			u.setId(Json.valueOf(user, ServerKey.ID, User.NO_USER));
			u.setErn(Json.valueOf(user, ServerKey.ERN));
			u.setGender(Json.valueOf(user, ServerKey.GENDER));
			u.setBirthYear(Json.valueOf(user, ServerKey.BIRTH_YEAR, 0));
			u.setName(Json.valueOf(user, ServerKey.NAME));
			u.setEmail(Json.valueOf(user, ServerKey.EMAIL));
			u.setPermissions(Permission.fromJSON(user.getJSONObject(ServerKey.PERMISSIONS)));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return u;
	}

	@Override
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(User u) {
		JSONObject o = new JSONObject();
		try {
			o.put(ServerKey.ID, u.getId());
			o.put(ServerKey.ERN, Json.nullCheck(u.getErn()));
			o.put(ServerKey.GENDER, Json.nullCheck(u.getGender()));
			o.put(ServerKey.BIRTH_YEAR, Json.nullCheck(u.getBirthYear()));
			o.put(ServerKey.NAME, Json.nullCheck(u.getName()));
			o.put(ServerKey.EMAIL, Json.nullCheck(u.getEmail()));
			o.put(ServerKey.PERMISSIONS, Json.toJson(u.getPermissions()));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o;
	}
	
	public boolean isLoggedIn() {
		return mEmail != null && mId > NO_USER;
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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + mBirthYear;
		result = prime * result + ((mEmail == null) ? 0 : mEmail.hashCode());
		result = prime * result + ((mErn == null) ? 0 : mErn.hashCode());
		result = prime * result + ((mGender == null) ? 0 : mGender.hashCode());
		result = prime * result + mId;
		result = prime * result + ((mName == null) ? 0 : mName.hashCode());
		result = prime * result
				+ ((mPermissions == null) ? 0 : mPermissions.hashCode());
		result = prime * result
				+ ((mSubscribers == null) ? 0 : mSubscribers.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (mBirthYear != other.mBirthYear)
			return false;
		if (mEmail == null) {
			if (other.mEmail != null)
				return false;
		} else if (!mEmail.equals(other.mEmail))
			return false;
		if (mErn == null) {
			if (other.mErn != null)
				return false;
		} else if (!mErn.equals(other.mErn))
			return false;
		if (mGender == null) {
			if (other.mGender != null)
				return false;
		} else if (!mGender.equals(other.mGender))
			return false;
		if (mId != other.mId)
			return false;
		if (mName == null) {
			if (other.mName != null)
				return false;
		} else if (!mName.equals(other.mName))
			return false;
		if (mPermissions == null) {
			if (other.mPermissions != null)
				return false;
		} else if (!mPermissions.equals(other.mPermissions))
			return false;
		if (mSubscribers == null) {
			if (other.mSubscribers != null)
				return false;
		} else if (!mSubscribers.equals(other.mSubscribers))
			return false;
		return true;
	}
	
}