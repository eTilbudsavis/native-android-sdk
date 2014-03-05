package com.eTilbudsavis.etasdk.EtaObjects;


import java.io.Serializable;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Json;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class Session extends EtaObject implements Serializable {
	
	public static final String TAG = "Session";
	
	private static final long serialVersionUID = 1L;
	
	private String mToken = null;
	private Date mExpires = new Date(1000);
	private User mUser = new User();
	private Permission mPermission = null;
	private String mProvider = null;
	
	public Session() { }
	
	public static Session fromJSON(JSONObject session) {	
		return fromJSON(new Session(), session);
	}
	
	public static Session fromJSON(Session s, JSONObject session) {
		if (s == null) s = new Session();
		if (session == null) return s;
		
		s.setToken(Json.valueOf(session, ServerKey.TOKEN));
		s.setExpires(Json.valueOf(session, ServerKey.EXPIRES));
		String user = Json.valueOf(session, ServerKey.USER);
		s.setUser(user == null ? new User() : User.fromJSON(user));
		s.setPermission(Permission.fromJSON(Json.valueOf(session, ServerKey.PERMISSIONS))) ;
		s.setProvider(Json.valueOf(session, ServerKey.PROVIDER));
		
		return s;
	}

	@Override
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Session s) {
		JSONObject o = new JSONObject();
		
		try {
			o.put(ServerKey.TOKEN, Json.nullCheck(s.getToken()));
			o.put(ServerKey.EXPIRES, Json.nullCheck(Utils.parseDate(s.getExpire())));
			o.put(ServerKey.USER, s.getUser().getUserId() == User.NO_USER ? JSONObject.NULL : s.getUser().toJSON());
			o.put(ServerKey.PERMISSIONS, Json.toJson(s.getPermission()));
			o.put(ServerKey.PROVIDER, Json.nullCheck(s.getProvider()));
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o;
	}
	
	/**
	 * Get this Sessions token. Used for headers in API calls
	 * @return token as String if session is active, otherwise null.
	 */
	public String getToken() {
		return mToken;
	}
	
	public Session setToken(String token) {
		mToken = token;
		return this;
	}
	
	public User getUser() {
		return mUser;
	}
	
	public Session setUser(User user) {
		mUser = user == null ? new User() : user;
		return this;
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
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((mExpires == null) ? 0 : mExpires.hashCode());
		result = prime * result
				+ ((mPermission == null) ? 0 : mPermission.hashCode());
		result = prime * result
				+ ((mProvider == null) ? 0 : mProvider.hashCode());
		result = prime * result + ((mToken == null) ? 0 : mToken.hashCode());
		result = prime * result + ((mUser == null) ? 0 : mUser.hashCode());
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
		Session other = (Session) obj;
		if (mExpires == null) {
			if (other.mExpires != null)
				return false;
		} else if (!mExpires.equals(other.mExpires))
			return false;
		if (mPermission == null) {
			if (other.mPermission != null)
				return false;
		} else if (!mPermission.equals(other.mPermission))
			return false;
		if (mProvider == null) {
			if (other.mProvider != null)
				return false;
		} else if (!mProvider.equals(other.mProvider))
			return false;
		if (mToken == null) {
			if (other.mToken != null)
				return false;
		} else if (!mToken.equals(other.mToken))
			return false;
		if (mUser == null) {
			if (other.mUser != null)
				return false;
		} else if (!mUser.equals(other.mUser))
			return false;
		return true;
	}
	
}