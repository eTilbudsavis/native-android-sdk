package com.eTilbudsavis.etasdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Endpoint;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Method;
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
    
    private final String ENDPOINT = Request.Endpoint.SESSIONS;
    
	private Eta mEta;
	private Session mSession;
	private Object LOCK = new Object();
	
	/** weather or not, the SessionManager should recover from a bad session request */
	boolean mTryToRecover = true;
	
	/** Queue of session requests */
	private List<Request<?>> mSessionQueue = Collections.synchronizedList(new ArrayList<Request<?>>());
	
	private ArrayList<OnSessionChangeListener> mSubscribers = new ArrayList<OnSessionChangeListener>();
	
	private Request<?> mReqInFlight;
	private Listener<JSONObject> mReqInFlightListener;
	
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
	
	Listener<JSONObject> sessionListener = new Listener<JSONObject>() {

		public void onComplete(boolean isCache, JSONObject response, EtaError error) {
			
			EtaLog.d(TAG, "SessionCallback: " + (response != null ? "success" : "error"));
			
			/*
			 * Three cases:
			 * 1 - Session is okay, set it - done
			 * 2 - There was an error that we can recover from, and we haven't tried before, then do it - done
			 * 3 - We are out of luck, run the remaining API requests, and probably they will die
			 *     but the user will at least get some feedback
			 */
			
			if (response != null) {
				
				setSession(response);
				
			} else if (error != null && 
					(error.getCode() == 1101 || error.getCode() == 1104 || error.getCode() == 1108) 
					&& mTryToRecover ) {
				
				mTryToRecover = false;
				postSession();
				
			} else {
				
				mEta.getRequestQueue().sessionUpdateComplete();
				
			}
			
		}
	};
	
	public synchronized boolean refresh() {
		
		boolean recovering = (mReqInFlight != null);
		if (recovering) {
			putSession();
		}
		return recovering;
		
	}
	
	private synchronized void runQueue(Request<JSONObject> r) {
		
		mSessionQueue.add(r);
		if (mReqInFlight == null) {
			
			mReqInFlight = mSessionQueue.get(0);
			mSessionQueue.remove(0);
			
			if (r.getListener() != sessionListener) {
				
			}
			
		} else {
			EtaLog.d(TAG, "Session Request in Flight. Waiting...");
		}
		
	}
	
	private void performNextRequest() {
		
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
			
			// Reset session retry boolean
			mTryToRecover = true;
			
			// Send out notifications
			notifySubscribers();
			for (PageflipWebview p : PageflipWebview.pageflips) {
				p.updateSession();
			}
			
		}
		
	}
	
	/**
	 * Method for ensuring that there is a valid session on every resume event.
	 */
	public void onResume() {
		
		// Make sure, that the session is up to date
		if (mSession.getToken() == null) {
			postSession();
		} else {
			putSession();
		}
		
	}
	
	private void postSession() {
		
		Bundle args = new Bundle();
		args.putInt(Request.Param.TOKEN_TTL, TTL);
		
	    CookieSyncManager.createInstance(mEta.getContext());
	    CookieManager cm = CookieManager.getInstance();
	    String cookieString = cm.getCookie(ETA_COOKIE_DOMAIN);
	    
	    EtaLog.d(TAG, cookieString == null ? "cookie null" : cookieString);
	    
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
	    
	    JsonObjectRequest req = new JsonObjectRequest(Method.POST, ENDPOINT, null, sessionListener);
	    runQueue(req);
	}
	
	private void putSession(){
		JsonObjectRequest req = new JsonObjectRequest(Method.PUT, ENDPOINT, null, sessionListener);
		runQueue(req);
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
		JsonObjectRequest req = new JsonObjectRequest(Method.PUT, ENDPOINT, null, l);
		runQueue(req);
		
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
        JsonObjectRequest req = new JsonObjectRequest(Method.PUT, Request.Endpoint.SESSIONS, new JSONObject(args), new Listener<JSONObject>() {

			public void onComplete(boolean isCache, JSONObject response, EtaError error) {

				if (response != null) {
	                mEta.getListManager().clear(u.getId());
					setSession(response);
				} else {
					performNextRequest();
				}
				if (l != null) { l.onComplete(isCache, response, error); }
				
			}
		});
        
        runQueue(req);
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
		runQueue(req);
		
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
		runQueue(req);
		
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