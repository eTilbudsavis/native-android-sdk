package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.Serializable;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class Session extends EtaObject implements Serializable {

	public static final String TAG = "Session";

	private static final long serialVersionUID = 1L;

	/** API v2 Session endpoint */
	public static final String ENDPOINT = Endpoint.SESSIONS;
	
	private String mToken = null;
	private Date mExpires = new Date();
	private User mUser = new User();
	private Permission mPermission = null;
	private String mProvider = null;
	
	public Session() {
		
	}

	@SuppressWarnings("unchecked")
	public static Session fromJSON(JSONObject session) {	
		return fromJSON(new Session(), session);
	}
	
	public static Session fromJSON(Session s, JSONObject session) {
		if (s == null) s = new Session();
		if (session == null) return s;
		
		s.setToken(getJsonString(session, S_TOKEN));
		s.setExpires(getJsonString(session, S_EXPIRES));
		s.setUser(User.fromJSON(getJsonString(session, S_USER)));
		s.setPermission(Permission.fromJSON(getJsonString(session, S_PERMISSIONS))) ;
		s.setProvider(getJsonString(session, S_PROVIDER));
		
		return s;
	}
	
	public JSONObject toJSON() {
		return toJSON(this);
	}
	
	public static JSONObject toJSON(Session s) {
		JSONObject o = new JSONObject();
		
		try {
			o.put(S_TOKEN, s.getToken());
			o.put(S_EXPIRES, s.getExpire());
			o.put(S_USER, s.getUser().toJSON());
			o.put(S_PERMISSIONS, s.getPermission());
			o.put(S_PROVIDER, s.getProvider());
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return o;
	}

	public boolean isExpired() {
		return mExpires == null ? true : mExpires.getTime() < (System.currentTimeMillis() + Utils.MINUTE_IN_MILLIS);
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
		mUser = user;
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
