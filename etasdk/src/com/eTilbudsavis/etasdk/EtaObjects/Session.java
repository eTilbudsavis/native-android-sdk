package com.eTilbudsavis.etasdk.EtaObjects;

import android.annotation.SuppressLint;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;

public class Session implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/** API v2 Session endpoint */
	public static final String ENDPOINT = "/v2/sessions";
	
	@SuppressLint("SimpleDateFormat")
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss+SSSS");
	
	private String mJson = null;
	private String mToken = null;
	private long mExpires = 0L;
	private String mUser = null;
	private Permission mPermission = null;
	
	public Session(JSONObject session) {
		update(session);
	}
	
	public void update(JSONObject newSession) {
			mJson = newSession.toString();
		try {
			mToken = newSession.getString("token");
		    setExpires(newSession.getString("expires"));
		    mUser = newSession.getString("user");
		    mPermission = new Permission(newSession.getJSONObject("permissions"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public String getJson() {
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

	public Session setUser(String user) {
		mUser = user;
		return this;
	}

	public String getUser() {
		return mUser;
	}

	public Session setPermission(Permission permission) {
		mPermission = permission;
		return this;
	}

	public Permission getPermission() {
		return mPermission;
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

}
