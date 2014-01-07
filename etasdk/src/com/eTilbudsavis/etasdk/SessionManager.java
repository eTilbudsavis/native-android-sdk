package com.eTilbudsavis.etasdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.eTilbudsavis.etasdk.Api.JsonObjectListener;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.EtaObjects.Session;
import com.eTilbudsavis.etasdk.EtaObjects.User;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.Request.Endpoint;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class SessionManager {
	
	public static final String TAG = "SessionManager";
	
    public static final String ETA_COOKIE_DOMAIN = "etilbudsavis.dk";
    public static final String COOKIE_AUTH_ID = "auth[id]";
    public static final String COOKIE_AUTH_TIME = "auth[time]";
    public static final String COOKIE_AUTH_HASH = "auth[hash]";
    
	/** Token time to live i'm requesting, 45days */
    public static int TTL = 3888000;
    
    private static final int SESSION_RETRIES = 2;
	
	private Eta mEta;
	private Session mSession;
	private Object LOCK = new Object();
	
	private int mSessionRetryCount = 0;

	// Queue of ApiRequests to execute, when session is okay
	private List<Api> mQueue = Collections.synchronizedList(new ArrayList<Api>());

	// Queue of session requests
	private List<Api> mSessionQueue = Collections.synchronizedList(new ArrayList<Api>());
	
	private ArrayList<OnSessionChangeListener> mSubscribers = new ArrayList<OnSessionChangeListener>();
	
	private Api mSIF;

	private JsonObjectListener putListener = new JsonObjectListener() {
		public void onComplete(boolean isCache, int statusCode, JSONObject item, EtaError error) { }
	};
	
	private JsonObjectListener postListener = new JsonObjectListener() {
		public void onComplete(boolean isCache, int statusCode, JSONObject item, EtaError error) { }
	};
	
	public SessionManager(Eta eta) {
		mEta = eta;
		JSONObject session = mEta.getSettings().getSessionJson();
		
		mSession = Session.fromJSON(session);
		// Make sure, that the session isn't null
		mSession = mSession == null ? new Session() : mSession;
		
	}
	
	private void runSessionQueue() {

		mSessionRetryCount++;
		
		mSIF = mSessionQueue.get(0);
		mSessionQueue.remove(mSIF);
		
		final JsonObjectListener original = (JsonObjectListener) mSIF.getListener();
		
		JsonObjectListener tmp = new JsonObjectListener() {
			
			public void onComplete(boolean isCache, int statusCode, JSONObject item,EtaError error) {
				
				mSIF = null;
				
				if (item != null) {
					
					setSession(item);
					
				} else if (error != null && 
						(error.getCode() == 1101 || error.getCode() == 1104 || error.getCode() == 1108) 
						&& mSessionRetryCount < SESSION_RETRIES ) {
					
					postSession();
					
				} else {
					
					performNextRequest();
					
				}
				
				if (original != null) {
					original.onComplete(isCache, statusCode, item, error);
				}
				
			}
		};
		
		mSIF.setListener(tmp);
		mSIF.setFlag(Api.FLAG_PRINT_DEBUG);
		mSIF.runThread();
		
	}
	
	private void runRequestQueue() {
		
		synchronized (mQueue) {
			List<Api> tmp = new ArrayList<Api>(mQueue.size());
			for (Api a : mQueue) {
				tmp.add(a);
				a.runThread();
			}
			mQueue.removeAll(tmp);
			
			// Reset session queue, so user can force a new session refresh if necessary
			mSessionRetryCount = 0;
		}
		
	}
	
	private boolean shouldPut() {
		int ttl = Eta.getInstance().getSettings().getSessionTTL();
		int halfTTL = (ttl/2)*1000;
		return mSession.getExpire().getTime()-System.currentTimeMillis() < halfTTL;
	}
	
	/**
	 * Requests to the api, should be added here. This will ensure that all calls will be queued for later execution
	 * if no valid session exists.
	 * @param api to execute on session-ok
	 */
	public synchronized void performRequest(Api api) {
		
		if (api.getUrl().contains(Request.Endpoint.SESSIONS)) {
			mSessionQueue.add(api);
		} else {
			mQueue.add(api);
		}
		performNextRequest();
		
	}
	
	private void performNextRequest() {
		
		// If no session inflight, we continue
		if (mSIF != null) {
			EtaLog.d(TAG, "Session in flight, waiting for call to finish");
			return;
		}
		
		// If session queue is empty
		if (mSessionQueue.isEmpty()) {
			
			boolean canRetry = mSessionRetryCount < SESSION_RETRIES;
			
			if (canRetry && mSession.isExpired()) {
				
				postSession();
				
			} else if (canRetry && shouldPut() ) {
				
				putSession(new Bundle());
				
			} else {
				
				runRequestQueue();
				
			}
			
		} else if (Eta.getInstance().isResumed()) {
			
			runSessionQueue();
			
		}
			
		
	}
	
	/**
	 * Method for ensuring that there is a valid session on every resume event.
	 */
	public void onResume() {
		
		// Make sure, that the session is up to date
		if (mSession.isExpired()) {
			postSession();
		} else {
			putSession(new Bundle());
		}
		
	}
	
	public void setSession(JSONObject session) {

		synchronized (LOCK) {
			
			Session s = Session.fromJSON(session);
			
			// If SessionManager does a session change, and propagates it to pageflip
			// Then pageflip propagate the session back, making nasty recursion
			if (s.getToken().equals(mSession.getToken())) {
				return;
			}
			
			mSession = s;
			mEta.getSettings().setSessionJson(session);
			
			// Dynamically set the ttl, so we won't have any odd errors
			int ttl = (int)(mSession.getExpire().getTime() - (new Date()).getTime()) / 1000;
			mEta.getSettings().setSessionTTL(ttl);
			
			notifySubscribers();
			for (PageflipWebview p : PageflipWebview.pageflips) {
				p.updateSession();
			}
			
			// Reset session counter
			mSessionRetryCount = 0;
			
			performNextRequest();
		}
		
	}
	
	private void postSession() {
		
		Bundle args = new Bundle();
		args.putInt(Request.Param.TOKEN_TTL, TTL);
		
	    CookieSyncManager.createInstance(mEta.getContext());
	    CookieManager cm = CookieManager.getInstance();
	    String cookieString = cm.getCookie(ETA_COOKIE_DOMAIN);
	    
	    if (cookieString != null) {
	    	
			// No session yet, check cookies for old token
			String authId = null;
			String authTime = null;
			String authHash = null;
			
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
	        	args.putString(Request.Param.V1_AUTH_ID, authId);
	        	args.putString(Request.Param.V1_AUTH_HASH, authHash);
	        	args.putString(Request.Param.V1_AUTH_TIME, authTime);

	        }
	        
        	cm.setCookie(ETA_COOKIE_DOMAIN, null);
        	
	    }
	    
        mEta.getApi().post(Request.Endpoint.SESSIONS, postListener, args).execute();
        
	}
	
	private void putSession(final Bundle args){
    	mEta.getApi().put(Request.Endpoint.SESSIONS, putListener, args).execute();
	}
	
	/**
	 * Perform a standard login, using an existing eTilbudsavis user.
	 * @param user - etilbudsavis user name (e-mail)
	 * @param password for user
	 * @param l for callback on complete
	 */
	public void login(String user, String password, JsonObjectListener l) {
		
		Bundle args = new Bundle();
		args.putString(Request.Param.EMAIL, user);
		args.putString(Request.Param.PASSWORD, password);
		mEta.getSettings().setSessionUser(user);
		mEta.getApi().put(Request.Endpoint.SESSIONS, l, args).execute();
		
	}
	
	/**
	 * Login to eTilbudsavis, using a Facebook token.<br>
	 * This requires you to implement the Facebook SDK, and relay the Facebook token.
	 * @param facebookAccessToken
	 * @param l
	 */
	public void loginFacebook(String facebookAccessToken, JsonObjectListener l) {
		
		Bundle args = new Bundle();
		args.putString(Request.Param.FACEBOOK_TOKEN, facebookAccessToken);
		mEta.getSettings().setSessionFacebook(facebookAccessToken);
		mEta.getApi().put(Request.Endpoint.SESSIONS, l, args).execute();
		
	}
	
	/**
	 * Signs a user out, and cleans all references to the user.<br><br>
	 * A new {@link #login(String, String) login} is needed to get access to user stuff again.
	 */
	public void signout(final JsonObjectListener l) {
		
		final User u = mSession.getUser();
        mEta.getListManager().clear(u.getId());
		Bundle b = new Bundle();
		b.putString(Request.Param.EMAIL, "");

		JsonObjectListener sessionListener = new JsonObjectListener() {
			
			public void onComplete(boolean isCache, int statusCode, JSONObject item, EtaError error) {

				if (Utils.isSuccess(statusCode)) {
	                mEta.getListManager().clear(u.getId());
					setSession(item);
				} else {
					performNextRequest();
				}
				if (l != null) { l.onComplete(isCache, statusCode, item, error); }

			}
			
		};
		
		mEta.getApi().put(Request.Endpoint.SESSIONS, sessionListener, b).execute();
		
	}
	
	/**
	 * @param email
	 * @param password
	 * @param name
	 * @param birthYear
	 * @param gender
	 * @param successRedirect
	 * @param errorRedirect
	 * @return true if all arguments are valid, false otherwise
	 */
	public void createUser(String email, String password, String name, int birthYear, String gender, String locale, String successRedirect, String errorRedirect, final JsonObjectListener l) {
		
		Bundle b = new Bundle();
		b.putString(Request.Param.EMAIL, email);
		b.putString(Request.Param.PASSWORD, password);
		b.putString(Request.Param.NAME, name);
		b.putInt(Request.Param.BIRTH_YEAR, birthYear);
		b.putString(Request.Param.GENDER, gender);
		b.putString(Request.Param.SUCCESS_REDIRECT, successRedirect);
		b.putString(Request.Param.ERROR_REDIRECT, errorRedirect);
		b.putString(Request.Param.LOCALE, locale);
		mEta.getApi().post(Request.Endpoint.USER, l, b).setFlag(Api.FLAG_PRINT_DEBUG).execute();
		
	}
	
	/**
	 * Method for requesting a password reset.
	 * @param email of the user
	 * @param successRedirect 
	 * @param errorRedirect
	 * @param l
	 */
	public void forgotPassword(String email, String successRedirect, String errorRedirect, JsonObjectListener l) {
		
		Bundle b = new Bundle();
		b.putString(Request.Param.EMAIL, email);
		b.putString(Request.Param.SUCCESS_REDIRECT, successRedirect);
		b.putString(Request.Param.ERROR_REDIRECT, errorRedirect);

		mEta.getApi().post(Endpoint.USER_RESET, l, b).execute();
		
	}
	
	/**
	 * Get the current session
	 * @return a session.
	 */
	public Session getSession() {
		return mSession;
	}
	
	/**
	 * Update current session, with new headers from server (these are given as return headers, on all requests)
	 * @param headerToken
	 * @param headerExpires
	 */
	public void updateTokens(String headerToken, String headerExpires) {
		
		synchronized (LOCK) {
			
			if (mSession.getToken() == null || !mSession.getToken().equals(headerToken) ) {
				mSession.setToken(headerToken);
				mSession.setExpires(headerExpires);
				mEta.getSettings().setSessionJson(mSession.toJSON());
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
		mEta.getSettings().setSessionUser(null);
		mEta.getSettings().setSessionFacebook(null);
	}
	
	public SessionManager subscribe(OnSessionChangeListener l) {
		synchronized (mSubscribers) {
			if (!mSubscribers.contains(l)) {
				mSubscribers.add(l);
			}
		}
		return this;
	}
	
	public void unSubscribe(OnSessionChangeListener l) {
		synchronized (mSubscribers) {
			mSubscribers.remove(l);
		}
	}
	
	public SessionManager notifySubscribers() {
		synchronized (mSubscribers) {
			for (final OnSessionChangeListener sl : mSubscribers) {
				try {
					mEta.getHandler().post(new Runnable() {
						
						public void run() {
							sl.onUpdate();
						}
					});
				} catch (Exception e) {
					EtaLog.d(TAG, e);
				}
			}
		}
		return this;
	}

	public interface OnSessionChangeListener {
		public void onUpdate();
	}
	
}