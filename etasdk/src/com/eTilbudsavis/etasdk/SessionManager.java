package com.eTilbudsavis.etasdk;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.json.JSONObject;

import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.eTilbudsavis.etasdk.EtaObjects.Session;
import com.eTilbudsavis.etasdk.EtaObjects.User;
import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;
import com.eTilbudsavis.etasdk.NetworkHelpers.JsonObjectRequest;
import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Method;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Priority;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.EtaLog.EventLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class SessionManager {
	
	public static final String TAG = "SessionManager";
	
    public static final String ETA_COOKIE_DOMAIN = "etilbudsavis.dk";
    public static final String COOKIE_AUTH_ID = "auth[id]";
    public static final String COOKIE_AUTH_TIME = "auth[time]";
    public static final String COOKIE_AUTH_HASH = "auth[hash]";
    
	/** Token time to live. I'm requesting 45days */
    public static int TTL = 3888000;
    
	private Eta mEta;
	private Session mSession;
	private Object LOCK = new Object();
	
	/** weather or not, the SessionManager should recover from a bad session request */
	boolean mTryToRecover = true;
	
	private LinkedList<Request<?>> mSessionQueue = new LinkedList<Request<?>>();
	
	private Request<?> mReqInFlight;
	
	private ArrayList<OnSessionChangeListener> mSubscribers = new ArrayList<OnSessionChangeListener>();
	SntpClient s = new SntpClient();
	private Runnable mSessionPutter = new Runnable() {
		
		public void run() {
			
			putSession(getSessionListener(null));
			
		}
	};
	
	public SessionManager(Eta eta) {
		mEta = eta;
		JSONObject session = mEta.getSettings().getSessionJson();
		
		if (session == null) {
			mSession = new Session();
		} else {
			mSession = Session.fromJSON(session);
		}
		
		// Make sure, that the session isn't null - we really don't want this to be null
		mSession = (mSession == null ? new Session() : mSession);
		
	}
	
	private Listener<JSONObject> getSessionListener(final Listener<JSONObject> l) {
		
		Listener<JSONObject> sessionListener = new Listener<JSONObject>() {

			public void onComplete(JSONObject response, EtaError error) {
				
				EtaLog.d(TAG, "Session", response, error);
				
				synchronized (LOCK) {
					mReqInFlight = null;
				
					if (response != null) {
						
						setSession(response);
						runQueue();
						
					} else if (mTryToRecover && recoverableError(error) ) {
						
						mTryToRecover = false;
						postSession(null);
						
					} else {
						
						runQueue();
						
					}
				}
				
				if (l != null) {
					l.onComplete( response, error);
				}
			}
		};
		
		return sessionListener;
		
	}
	
	private synchronized void addRequest(JsonObjectRequest r) {
		r.setPriority(Priority.HIGH);
		mSessionQueue.add(r);
		runQueue();
	}
	
	private void runQueue() {
		
		if (isRequestInFlight()) {
			EtaLog.d(TAG, "Session in flight, waiting for session call to finish");
			return;
		}
		
		if (mSessionQueue.isEmpty()) {
			
			// SessionManager is done
			mEta.getRequestQueue().runParkedQueue();
			
		} else {
			
			synchronized (LOCK) {
				mReqInFlight = mSessionQueue.removeFirst();
				mEta.add(mReqInFlight);
			}
			
		}
		
	}
	
	/**
	 * Ask the SessionManager to refresh the session.
	 * @return true if SessionManager is trying, or will try to refresh the session. 
	 * False if no more tries will be attempted.
	 */
	public synchronized boolean recover(EtaError e) {
		
		if (mTryToRecover) {
			if (!recoverableError(e)) {
				postSession(null);
			} else {
				putSession(null);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * Update current session with a JSONObject retrieved from eTilbudsavis API v2.
	 * @param session to update from
	 * @return true if session was updated
	 */
	public synchronized boolean setSession(JSONObject session) {
		
		Session s = Session.fromJSON(session);
		
		// Check that the JSON is actually session JSON
		if (s.getToken() == null) {
			return false;
		}
		
		mSession = s;
		mEta.getSettings().setSessionJson(session);
		
		// Reset session retry boolean
		mTryToRecover = true;
		
		// Send out notifications
		notifySubscribers();
		
		return true;
	}
	
	/**
	 * Method for determining is a given error is an error that the SessionManager.
	 * Should, and can recover from.
	 * @param e - error to check
	 * @return true if SessionManager can recover from this error, else false
	 */
	public static boolean recoverableError(EtaError e) {
		return ( e != null && ( e.getCode() == 1101 || e.getCode() == 1104 || e.getCode() == 1108) );
	}
	
	/**
	 * Method for determining if an error is a session error.
	 * This is determined from the error code given by the API.
	 * Note that SessionManager isn't nescessarily able to recover from all
	 * session errors, so please check recoverableError() before retrying.
	 * @param e - error to check
	 * @return true if it's a session error
	 */
	public static boolean isSessionError(EtaError e) {
		return ( e != null && ( 1100 <= e.getCode() && e.getCode() < 1200 ) );
	}

	public Request<?> getRequestInFlight() {
		synchronized (LOCK) {
			return mReqInFlight;
		}
	}

	public boolean isRequestInFlight() {
		synchronized (LOCK) {
			return mReqInFlight != null;
		}
	}
	
	/**
	 * Method for ensuring that there is a valid session on every resume event.
	 */
	public void onResume() {
		
		// Make sure, that the session is up to date
		if (mSession.getToken() == null) {
			postSession(null);
		} else {
			putSession(null);
		}
		
		
		Thread t = new Thread(new Runnable() {
			
			public void run() {
				
				s.requestTime("0.pool.ntp.org", 15000);
				long ntp = s.getNtpTime();
				long sys = System.currentTimeMillis();
				long delta = ntp - sys;
				long abdDelta = Math.abs(delta);
				
				if (abdDelta < Utils.MINUTE_IN_MILLIS) {
					EtaLog.d(TAG, "Nothing to worry about, time seems fine.");
				} else {
					EtaLog.d(TAG, "NPT: " + new Date(ntp).toGMTString());
					EtaLog.d(TAG, "SYS: " + new Date(sys).toGMTString());
				}
				
				String message;
				if (delta < 0) {
					message = String.format("System time is ahead of NTP by %s seconds", abdDelta / 1000);
				} else if (delta > 0) {
					message = String.format("System time is behind of NTP by %s seconds", abdDelta / 1000);
				} else {
					message = "System time is exactly the same as NTP";
				}
				EtaLog.d(TAG, message);
				
			}
		});
		t.start();
		
	}
	
	public void onPause() {
		
	}
	
	private void postSession(final Listener<JSONObject> l) {
		
		Bundle args = new Bundle();
		args.putInt(Request.Param.TOKEN_TTL, TTL);
	    args.putString(Request.Param.API_KEY, mEta.getApiKey());
    	
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
	            
	            if (value.equals("")) {
	            	continue;
	            }
	            
	            if (key.equals(COOKIE_AUTH_ID)) {
	            	authId = value;
	            } else if (key.equals(COOKIE_AUTH_HASH)) {
	                authHash = value;
	            } else if (key.equals(COOKIE_AUTH_TIME)) {
	            	authTime = value;
	            }
	            
	        }
	        
	        // If all three fields are set, then try to migrate
	        if (authId != null && authHash != null && authTime != null) {
	        	args.putString(Request.Param.V1_AUTH_ID, authId);
	        	args.putString(Request.Param.V1_AUTH_HASH, authHash);
	        	args.putString(Request.Param.V1_AUTH_TIME, authTime);
	        }
	        
	        // Clear all cookie data, just to make sure
	        cm.removeAllCookie();
	        
	    }
	    
	    JsonObjectRequest req = new JsonObjectRequest(Method.POST, Request.Endpoint.SESSIONS, Utils.createJSON(args), getSessionListener(l));
	    addRequest(req);
	    
	}
	
	private void putSession(final Listener<JSONObject> l){
		JsonObjectRequest req = new JsonObjectRequest(Method.PUT, Request.Endpoint.SESSIONS, null, getSessionListener(l));
		addRequest(req);
	}
	
	/**
	 * Perform a standard login, using an existing eTilbudsavis user.
	 * @param email - etilbudsavis user name (e-mail)
	 * @param password for user
	 * @param l for callback on complete
	 */
	public void login(String email, String password, Listener<JSONObject> l) {

		Bundle args = new Bundle();
		args.putString(Request.Param.EMAIL, email);
		args.putString(Request.Param.PASSWORD, password);
		mEta.getSettings().setSessionUser(email);
		JsonObjectRequest req = new JsonObjectRequest(Method.PUT, Request.Endpoint.SESSIONS, Utils.createJSON(args), getSessionListener(l));
		addRequest(req);
		
	}
	
	/**
	 * Login to eTilbudsavis, using a Facebook token.<br>
	 * This requires you to implement the Facebook SDK, and relay the Facebook token.
	 * @param facebookAccessToken
	 * @param l
	 */
	public void loginFacebook(String facebookAccessToken, Listener<JSONObject> l) {
		
		Map<String, String> args = new HashMap<String, String>();
		args.put(Request.Param.FACEBOOK_TOKEN, facebookAccessToken);
		mEta.getSettings().setSessionFacebook(facebookAccessToken);
		JsonObjectRequest req = new JsonObjectRequest(Method.PUT, Request.Endpoint.SESSIONS, new JSONObject(args), getSessionListener(l));
		addRequest(req);
		
	}
	
	/**
	 * Signs a user out, and cleans all references to the user.<br><br>
	 * A new {@link #login(String, String) login} is needed to get access to user stuff again.
	 */
	public void signout(final Listener<JSONObject> l) {
		
		final User u = mSession.getUser();
        mEta.getListManager().clear(u.getId());
        Map<String, String> args = new HashMap<String, String>();
        args.put(Request.Param.EMAIL, "");
        JsonObjectRequest req = new JsonObjectRequest(Method.PUT, Request.Endpoint.SESSIONS, new JSONObject(args), getSessionListener(l));
        addRequest(req);
        
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
	public void createUser(String email, String password, String name, int birthYear, String gender, String locale, String successRedirect, String errorRedirect, Listener<JSONObject> l) {
		
		Map<String, String> args = new HashMap<String, String>();
		args.put(Request.Param.EMAIL, email);
		args.put(Request.Param.PASSWORD, password);
		args.put(Request.Param.NAME, name);
		args.put(Request.Param.BIRTH_YEAR, String.valueOf(birthYear));
		args.put(Request.Param.GENDER, gender);
		args.put(Request.Param.SUCCESS_REDIRECT, successRedirect);
		args.put(Request.Param.ERROR_REDIRECT, errorRedirect);
		args.put(Request.Param.LOCALE, locale);
		JsonObjectRequest req = new JsonObjectRequest(Method.POST, Request.Endpoint.USER, new JSONObject(args), l);
		mEta.add(req);
		
	}
	
	/**
	 * Method for requesting a password reset.
	 * @param email of the user
	 * @param successRedirect 
	 * @param errorRedirect
	 * @param l
	 */
	public void forgotPassword(String email, String successRedirect, String errorRedirect, Listener<JSONObject> l) {
		
		Map<String, String> args = new HashMap<String, String>();
		args.put(Request.Param.EMAIL, email);
		args.put(Request.Param.SUCCESS_REDIRECT, successRedirect);
		args.put(Request.Param.ERROR_REDIRECT, errorRedirect);
		JsonObjectRequest req = new JsonObjectRequest(Method.POST, Request.Endpoint.USER_RESET, new JSONObject(args), l);
		mEta.add(req);
		
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
			
			for (PageflipWebview p : PageflipWebview.pageflips) {
				p.updateSession();
			}
			
		}
		return this;
	}

	public interface OnSessionChangeListener {
		public void onUpdate();
	}
	
}