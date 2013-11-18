package com.eTilbudsavis.etasdk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.style.EasyEditSpan;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.eTilbudsavis.etasdk.Api.JsonObjectListener;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.EtaObjects.Permission;
import com.eTilbudsavis.etasdk.EtaObjects.User;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Params;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class Session implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "Session";
	
	private static final String S_TOKEN = "token";
	private static final String S_EXPIRES = "expires";
	private static final String S_USER = "user";
	private static final String S_PERMISSIONS = "permissions";
	private static final String S_PROVIDER = "provider";
	
    public static final String COOKIE_DOMAIN = "etilbudsavis.dk";
    public static final String COOKIE_AUTH_ID = "auth[id]";
    public static final String COOKIE_AUTH_TIME = "auth[time]";
    public static final String COOKIE_AUTH_HASH = "auth[hash]";
    
	/** API v2 Session endpoint */
	public static final String ENDPOINT = Endpoint.SESSIONS;
	
	/** Token time to live, 45days */
	private static final int TTL = 3888000;
	
	private String mToken = null;
	private Date mExpires = new Date(0L);
	private User mUser = null;
	private Permission mPermission = null;
	private String mProvider;
	private String mPassStr = null;
	
	private Eta mEta;
	private boolean mIsUpdating = false;
	private ArrayList<SessionListener> mSubscribers = new ArrayList<Session.SessionListener>();
	private List<Api> mQueue = Collections.synchronizedList(new ArrayList<Api>());
	
	public Session(Eta eta) {
		mEta = eta;
		mUser = new User();
	}
	
	public void init() {
		
		JSONObject session = mEta.getSettings().getSessionJson();
		if (session != null) {
			set(session);
		} else {
			update(null);
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
		    mEta.getSettings().setSessionJson(session);

		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		
	    if (isExpired() || !mIsUpdating) {
	    	update(null);
	    } else {
	    	synchronized (mQueue) {
	    		List<Api> tmp = new ArrayList<Api>(mQueue.size());
		    	for (Api a : mQueue) {
		    		tmp.add(a);
					a.execute();
				}
		    	mQueue.removeAll(tmp);
			}
	    }
		
	}

	public void login(String user, String password, JsonObjectListener listener) {
		mPassStr = password;
		mEta.getSettings().setSessionUser(user);
		update(listener);
	}
	
	public void loginFacebook(String facebookAccessToken, JsonObjectListener listener) {
		mEta.getSettings().setSessionFacebook(facebookAccessToken);
		update(listener);
	}
	
	public void forgotPassword(String email, String successRedirect, String errorRedirect, final JsonObjectListener listener) {
		
		Bundle b = new Bundle();
		b.putString(Params.EMAIL, email);
		b.putString(Params.SUCCESS_REDIRECT, successRedirect);
		b.putString(Params.ERROR_REDIRECT, errorRedirect);
		
		mEta.getApi().post(Endpoint.USER_RESET, listener, b).execute();
		
	}

	private void sessionUpdate(int statusCode, JSONObject data, EtaError error) {
		
		if (Utils.isSuccess(statusCode)) {
			set(data);
		} else {
			EtaLog.d(TAG, "Error: " + String.valueOf(statusCode) + " - " + error.toString());
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

		JsonObjectListener sessionListener = new JsonObjectListener() {
			
			public void onComplete(boolean isCache, int statusCode, JSONObject data, EtaError error) {

				sessionUpdate(statusCode, data, error);
				if (listener != null) 
					listener.onComplete(isCache, statusCode, data, error);
				
			}
		};
		
		Bundle args = new Bundle();
		args.putInt(Params.TOKEN_TTL, TTL);

		String user = mEta.getSettings().getSessionUser();
		String facebook = mEta.getSettings().getSessionFacebook();
		
		if (facebook != null) {
			
			// Login with facebook token
			args.putString(Params.FACEBOOK_TOKEN, facebook);
			mEta.getApi().put(Session.ENDPOINT, sessionListener, args).execute();
			return;
			
		} else if (user != null && mPassStr != null) {
			
			// Regulare login
			args.putString(Params.EMAIL, user);
			args.putString(Params.PASSWORD, mPassStr);
			
		} else if (mEta.getSettings().getSessionJson() == null) {
			
			// No session yet, check cookies for old token
			String authId = null;
			String authTime = null;
			String authHash = null;
			
            CookieSyncManager.createInstance(mEta.getContext());
            CookieManager cm = CookieManager.getInstance();
            String cookieString = cm.getCookie(COOKIE_DOMAIN);
            if (cookieString != null) {
            	
	            String[] cookies = cookieString.split(";");
	            for(String cookie : cookies) {
	            	
	                String[] keyValue = cookie.split("=");
	                String key = keyValue[0].trim();
	                String value = keyValue[1];
	                
	                if (key.equals(COOKIE_AUTH_ID)) {
	                	authId = value;
	                } else if (key.equals(COOKIE_AUTH_HASH)) {
		                authHash = value;
	                } else if (key.equals(COOKIE_AUTH_TIME)) {
	                	authTime = value;
	                }
	                
	            }
	            
	            if (authId != null && authHash != null && authTime != null) {
	            	args.putString(Params.V1_AUTH_ID, authId);
	            	args.putString(Params.V1_AUTH_HASH, authHash);
	            	args.putString(Params.V1_AUTH_TIME, authTime);
	            }
	            
            }
	            
		}

		mEta.getApi().post(Session.ENDPOINT, sessionListener, args).execute();
		
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
		
		Bundle b = new Bundle();
		b.putString(Params.EMAIL, email);
		b.putString(Params.PASSWORD, password);
		b.putString(Params.NAME, name);
		b.putInt(Params.BIRTH_YEAR, birthYear);
		b.putString(Params.GENDER, gender);
		b.putString(Params.SUCCESS_REDIRECT, successRedirect);
		b.putString("locale", "da_DK");
		b.putString(Params.ERROR_REDIRECT, errorRedirect);
		
		JsonObjectListener userCreate = new JsonObjectListener() {
			
			public void onComplete(boolean isCache, int statusCode, JSONObject data, EtaError error) {

				if (Utils.isSuccess(statusCode)) {
					EtaLog.d(TAG, "Success: " + String.valueOf(statusCode) + " - " + data.toString());
				} else {
					EtaLog.d(TAG, "Error: " + String.valueOf(statusCode) + " - " + error.toString());
				}
				if (listener != null) listener.onComplete(isCache, statusCode, data, error);
			}
		};

		mEta.getApi().post(Endpoint.Path.USERS, userCreate, b).setFlag(Api.FLAG_PRINT_DEBUG).execute();
		return true;
	}
	
	public boolean isExpired() {
		return mExpires.getTime() < (System.currentTimeMillis() + Utils.MINUTE_IN_MILLIS);
	}

	public void addToQueue(Api api) {
		synchronized (mQueue) {
			mQueue.add(api);
		}
	}
	
	/**
	 * @param token
	 * @param expires
	 * @return
	 */
	public synchronized void update(String headerToken, String headerExpires) {
		
		if (mIsUpdating)
			return;
		
		JSONObject session = mEta.getSettings().getSessionJson();
		
		if (session == null) {
			
			update(null);
			
		} else {
			
			try {
				if (!mToken.equals(headerToken) ) {
					session.put(S_TOKEN, headerToken);
					session.put(S_EXPIRES, headerExpires);
					set(session);
					return;
				}
				if ( ( Utils.parseDate(headerExpires).getTime() - Utils.DAY_IN_MILLIS ) < System.currentTimeMillis()) {
					update(null);
				}
			} catch (JSONException e) {
				EtaLog.d(TAG, e);
			} 
			
		}
		
	}
	
	/**
	 * Signs a user out, and cleans all references to the user.<br><br>
	 * A new {@link #login(String, String) login} is needed to get access to user stuff again.
	 */
	public synchronized void signout(final JsonObjectListener listener) {
		
		mIsUpdating = true;
		int userId = mUser.getId();
		clearUser();
		mEta.getListManager().clear(userId);
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
		mPassStr = null;
		mEta.getSettings().setSessionUser(null);
		mEta.getSettings().setSessionFacebook(null);
		mUser = new User();
	}
	
	/**
	 * Destroys this session.<br><br>
	 * And returns a new session, completely clean session.
	 */
	public void invalidate(final JsonObjectListener listener) {
		mToken = null;
		mExpires = null;
		mPermission = null;
		mProvider = null;
		mSubscribers = new ArrayList<Session.SessionListener>();
		mEta.getSettings().setSessionJson(null);
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
		return mEta.getSettings().getSessionJson();
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
				EtaLog.d(TAG, e);
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
