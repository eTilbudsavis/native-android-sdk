/*******************************************************************************
 * Copyright 2015 ShopGun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.shopgun.android.sdk;

import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.shopgun.android.sdk.api.Parameters;
import com.shopgun.android.sdk.bus.SessionEvent;
import com.shopgun.android.sdk.bus.SgnBus;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Session;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.Request.Method;
import com.shopgun.android.sdk.network.Request.Priority;
import com.shopgun.android.sdk.network.Response.Listener;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.network.impl.JsonObjectRequest;
import com.shopgun.android.sdk.utils.Api.Endpoint;
import com.shopgun.android.sdk.utils.SgnJson;
import com.shopgun.android.sdk.utils.Utils;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SessionManager {

    public static final String TAG = Constants.getTag(SessionManager.class);

    public static final String ETA_COOKIE_DOMAIN = "etilbudsavis.dk";
    public static final String COOKIE_AUTH_ID = "auth[id]";
    public static final String COOKIE_AUTH_TIME = "auth[time]";
    public static final String COOKIE_AUTH_HASH = "auth[hash]";

    /**
     * Token time to live in seconds. Default for Android SDK is 45 days.
     * Must be in seconds
     */
    public static long TTL = 3888000;
    /** The lock object to use when requiring synchronization locks in SessionManager*/
    private final Object LOCK = new Object();
    /** weather or not, the SessionManager should recover from a bad session request */
    boolean mTryToRecover = true;
    /** Reference to ShopGun instance */
    private ShopGun mShopGun;
    /** The current user session */
    private Session mSession;
    /**  */
    private LinkedList<Request<?>> mSessionQueue = new LinkedList<Request<?>>();

    private Request<?> mReqInFlight;

    public SessionManager(ShopGun shopGun) {

        mShopGun = shopGun;
        JSONObject session = mShopGun.getSettings().getSessionJson();
        mSession = Session.fromJSON(session);
        if (mSession == null) {
            mSession = new Session();
        }
        ExternalClientIdStore.updateCid(mSession, mShopGun.getContext());
    }

    /**
     * Method for determining is a given error is an error that the SessionManager.
     * Should, and can recover from.
     * @param e - error to check
     * @return true if SessionManager can recover from this error, else false
     */
    public static boolean recoverableError(ShopGunError e) {
        if (e != null) {
            if (e.getCode() == ShopGunError.Code.TOKEN_EXPIRED ||
                    e.getCode() == ShopGunError.Code.INVALID_SIGNATURE ||
                    e.getCode() == ShopGunError.Code.MISSING_TOKEN ||
                    e.getCode() == ShopGunError.Code.INVALID_TOKEN) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method for determining if an error is a session error.
     * This is determined from the error code given by the API.
     * Note that SessionManager isn't nescessarily able to recover from all
     * session errors, so please check recoverableError() before retrying.
     * @param e - error to check
     * @return true if it's a session error
     */
    public static boolean isSessionError(ShopGunError e) {
        return (e != null && (1100 <= e.getCode() && e.getCode() < 1200));
    }

    private class SessionListenerWrapper implements Listener<JSONObject> {

        Listener<JSONObject> mListener;

        public SessionListenerWrapper(Listener<JSONObject> l) {
            this.mListener = l;
        }

        @Override
        public void onComplete(JSONObject response, ShopGunError error) {

            synchronized (LOCK) {

//                    SgnLog.d(TAG, "session: " + (response == null ? error.toString() : response.toString()));

                mReqInFlight = null;

                if (response != null) {

                    setSession(response);
                    runQueue();

                } else if (error.isSdk()) {

                    // Only API responses should result in a session update.
                    // We'll prevent the session from being destroyed, and 'ignore' the problem
                    runQueue();

                } else if (mTryToRecover && recoverableError(error)) {

                    mTryToRecover = false;
                    postSession(null);

                } else {

                    runQueue();

                }
            }

            if (mListener != null) {
                mListener.onComplete(response, error);
            }
        }
    }

    private void addRequest(JsonObjectRequest r) {

        synchronized (LOCK) {
            r.setPriority(Priority.HIGH);
            if (mSession.getClientId() != null) {
                r.getParameters().put(SgnJson.CLIENT_ID, mSession.getClientId());
            }
//			r.setDebugger(new NetworkDebugger());
            mSessionQueue.add(r);
            runQueue();
        }

    }

    private void runQueue() {

        if (isRequestInFlight()) {
            SgnLog.d(TAG, "Session in flight, waiting for session call to finish");
            return;
        }

        if (mSessionQueue.isEmpty()) {

            // SessionManager is done
            mShopGun.getRequestQueue().runParkedQueue();

        } else {

            synchronized (LOCK) {
                mReqInFlight = mSessionQueue.removeFirst();
                mShopGun.add(mReqInFlight);
            }

        }

    }

    /**
     * Ask the SessionManager to refresh the session.
     * @param e A {@link ShopGunError} to recover from.
     * @return true if SessionManager is trying, or will try to refresh the session.
     * False if no more tries will be attempted.
     */
    public boolean recover(ShopGunError e) {

        synchronized (LOCK) {

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

    }

    /**
     * Update current session with a JSONObject retrieved from ShopGun API v2.
     * @param session to update from
     * @return true if session was updated
     */
    private boolean setSession(JSONObject session) {

        synchronized (LOCK) {

            Session s = Session.fromJSON(session);
            if (mSession == null) {
                mSession = new Session();
            }

            // Check that the JSON is actually session JSON
            if (s.getToken() == null) {
                return false;
            }

            int oldId = mSession.getUser().getUserId();
            int newId = s.getUser().getUserId();

            mSession = s;
            ExternalClientIdStore.updateCid(mSession, mShopGun.getContext());
            mShopGun.getSettings().setSessionJson(session);

            // Reset session retry boolean
            mTryToRecover = true;

            // Send out notifications
            SgnBus.getInstance().post(new SessionEvent(oldId, newId));

            return true;

        }

    }

    /**
     * Get the current session request in flight, for a {@link Session} update.
     * If the {@link SessionManager} isn't performing a {@link Session} update then
     * {@code null} will be returned.
     * @return A {@link Request} if one is in flight, else {@code null}
     */
    public Request<?> getRequestInFlight() {
        synchronized (LOCK) {
            return mReqInFlight;
        }
    }

    /**
     * Check if {@link SessionManager} is currently performing a {@link Session} update.
     * @return {@code true} if {@link Session} is being updated, else {@code false}
     */
    public boolean isRequestInFlight() {
        synchronized (LOCK) {
            return mReqInFlight != null;
        }
    }

    /**
     * Method for ensuring that there is a valid session on every resume event.
     * <p>If no session exists, it will post for a new session. If the session
     * does exist, and it's been more than 2 hours since last usage it will put
     * for an session update</p>
     */
    public void onStart() {

        if (mSession.getToken() == null) {
            // If no session exists post for new
            postSession(null);
        } else {
            /*
			 * If it's been more than 2 hours since last usage, put for
			 * a session refresh, else ignore session refresh
			 */
            Date now = new Date();
            long delta = now.getTime() - mShopGun.getSettings().getLastUsedTime();
            boolean shouldPut = delta > TimeUnit.HOURS.toMillis(2);
//            boolean shouldPut = delta > (20 * Utils.SECOND_IN_MILLIS);
            if (shouldPut) {
                putSession(null);
            }
        }

        ExternalClientIdStore.updateCid(mSession, mShopGun.getContext());
    }

    public void onStop() {
        // empty, might be needed in the future
    }

    private void postSession(final Listener<JSONObject> l) {

        Map<String, Object> args = new HashMap<String, Object>();

        args.put(Parameters.TOKEN_TTL, TTL);
        args.put(Parameters.API_KEY, mShopGun.getApiKey());

        CookieSyncManager.createInstance(mShopGun.getContext());
        CookieManager cm = CookieManager.getInstance();
        String cookieString = cm.getCookie(ETA_COOKIE_DOMAIN);

        if (cookieString != null) {

            // No session yet, check cookies for old token
            String authId = null;
            String authTime = null;
            String authHash = null;

            String[] cookies = cookieString.split(";");
            for (String cookie : cookies) {

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
                args.put(Parameters.V1_AUTH_ID, authId);
                args.put(Parameters.V1_AUTH_HASH, authHash);
                args.put(Parameters.V1_AUTH_TIME, authTime);
            }

            // Clear all cookie data, just to make sure
            cm.removeAllCookie();

        }

        JsonObjectRequest req = new JsonObjectRequest(Method.POST, Endpoint.SESSIONS, new JSONObject(args), new SessionListenerWrapper(l));
        addRequest(req);

    }

    private void putSession(final Listener<JSONObject> l) {
        JsonObjectRequest req = new JsonObjectRequest(Method.PUT, Endpoint.SESSIONS, null, new SessionListenerWrapper(l));
        addRequest(req);
    }

    /**
     * Perform a standard login, using an existing ShopGun user.
     * @param email - ShopGun user name (e-mail)
     * @param password for user
     * @param l for callback on complete
     * @return The {@link Request} generated to perform the action.
     *          The request have already been queued for processing in the {@link com.shopgun.android.sdk.network.RequestQueue}.
     */
    public JsonObjectRequest login(String email, String password, Listener<JSONObject> l) {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put(Parameters.EMAIL, email);
        args.put(Parameters.PASSWORD, password);
        mShopGun.getSettings().setSessionUser(email);
        JsonObjectRequest req = new JsonObjectRequest(Method.PUT, Endpoint.SESSIONS, new JSONObject(args), new SessionListenerWrapper(l));
        addRequest(req);
        return req;
    }

    /**
     * Login to ShopGun, using a Facebook token.<br>
     * This requires you to implement the Facebook SDK, and relay the Facebook token.
     * @param facebookAccessToken A facebook access token
     * @param l lister for callbacks
     * @return The {@link Request} generated to perform the action.
     *          The request have already been queued for processing in the {@link com.shopgun.android.sdk.network.RequestQueue}.
     */
    public JsonObjectRequest loginFacebook(String facebookAccessToken, Listener<JSONObject> l) {
        Map<String, String> args = new HashMap<String, String>();
        args.put(Parameters.FACEBOOK_TOKEN, facebookAccessToken);
        mShopGun.getSettings().setSessionFacebook(facebookAccessToken);
        JsonObjectRequest req = new JsonObjectRequest(Method.PUT, Endpoint.SESSIONS, new JSONObject(args), new SessionListenerWrapper(l));
        addRequest(req);
        return req;
    }

    /**
     * Tell if current session is from facebook.
     * @return true if current session is a facebook session, else false
     */
    public boolean isFacebookSession() {
        return mShopGun.getSettings().getSessionFacebook() != null;
    }

    /**
     * Signs a user out, and cleans all references to the user.<br><br>
     * A new {@link #login(String, String, com.shopgun.android.sdk.network.Response.Listener)} login} is needed to get access to user stuff again.
     *
     * @param l A request listener
     * @return The {@link Request} generated to perform the action.
     *          The request have already been queued for processing in the {@link com.shopgun.android.sdk.network.RequestQueue}.
     */
    public JsonObjectRequest signout(final Listener<JSONObject> l) {


//		if (ShopGun.getInstance().isOnline()) {
        mShopGun.getSettings().setSessionFacebook(null);
        mShopGun.getListManager().clear(mSession.getUser().getUserId());
        Map<String, String> args = new HashMap<String, String>();
        args.put(Parameters.EMAIL, "");
        JsonObjectRequest req = new JsonObjectRequest(Method.PUT, Endpoint.SESSIONS, new JSONObject(args), new SessionListenerWrapper(l));
        addRequest(req);
        return req;
//		} else {
//			invalidate();
//			ShopGun.getInstance().getHandler().post(new Runnable() {
//				
//				public void run() {
//					l.onRequestComplete(mSession.toJSON(), null);
//				}
//			});
//		}

    }

    /**
     * Create a new ShopGun user.
     *
     * @param email An email
     * @param password A password
     * @param name A name
     * @param birthYear A birthyear
     * @param gender A gender
     * @param locale A {@link java.util.Locale} string
     * @param successRedirect An URL to include in the validation email, given registration succeeded
     * @param errorRedirect An URL to include in the validation email, given registration failed
     * @param l A request listener
     * @return The {@link Request} generated to perform the action.
     *          The request have already been queued for processing in the {@link com.shopgun.android.sdk.network.RequestQueue}.
     */
    public JsonObjectRequest createUser(String email, String password, String name, int birthYear, String gender, String locale, String successRedirect, String errorRedirect, Listener<JSONObject> l) {

        Map<String, String> args = new HashMap<String, String>();
        args.put(Parameters.EMAIL, email.trim());
        args.put(Parameters.PASSWORD, password);
        args.put(Parameters.NAME, name.trim());
        args.put(Parameters.BIRTH_YEAR, String.valueOf(birthYear));
        args.put(Parameters.GENDER, gender.trim());
        args.put(Parameters.SUCCESS_REDIRECT, successRedirect);
        args.put(Parameters.ERROR_REDIRECT, errorRedirect);
        args.put(Parameters.LOCALE, locale);
        JsonObjectRequest req = new JsonObjectRequest(Method.POST, Endpoint.USER, new JSONObject(args), l);
        mShopGun.add(req);
        return req;

    }

    /**
     * Method for requesting a password reset.
     * @param email of the user
     * @param successRedirect An URL to include in the validation email, given registration succeeded
     * @param errorRedirect An URL to include in the validation email, given registration failed
     * @param l lister for callbacks
     * @return The {@link Request} generated to perform the action.
     *          The request have already been queued for processing in the {@link com.shopgun.android.sdk.network.RequestQueue}.
     */
    public JsonObjectRequest forgotPassword(String email, String successRedirect, String errorRedirect, Listener<JSONObject> l) {

        Map<String, String> args = new HashMap<String, String>();
        args.put(Parameters.EMAIL, email);
        args.put(Parameters.SUCCESS_REDIRECT, successRedirect);
        args.put(Parameters.ERROR_REDIRECT, errorRedirect);
        JsonObjectRequest req = new JsonObjectRequest(Method.POST, Endpoint.USER_RESET, new JSONObject(args), l);
        mShopGun.add(req);
        return req;

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
     * @param headerToken X-Token received in headers from from eta api
     * @param headerExpires The expires string received in headers from from eta api
     */
    public void updateTokens(String headerToken, String headerExpires) {

        synchronized (LOCK) {

            if (mSession.getToken() == null || !mSession.getToken().equals(headerToken)) {
                mSession.setToken(headerToken);
                Date exp = Utils.stringToDate(headerExpires);
                mSession.setExpires(exp);
                mShopGun.getSettings().setSessionJson(mSession.toJSON());
            }
        }

    }

    /**
     * Destroys this session.<br>
     * A new session will be generated, on first request to server.
     */
    public void invalidate() {
        synchronized (LOCK) {
            int oldUserId = mSession.getUser().getUserId();
            mSession = new Session();
            ExternalClientIdStore.updateCid(mSession, mShopGun.getContext());
            mShopGun.getSettings().setSessionJson(mSession.toJSON());
            mShopGun.getSettings().setSessionFacebook(null);
            clearUser();
            SgnBus.getInstance().post(new SessionEvent(oldUserId, mSession.getUser().getUserId()));
        }
    }

    /**
     * Clear all eta-user details
     */
    private void clearUser() {
        mShopGun.getSettings().setSessionUser(null);
        mShopGun.getSettings().setSessionFacebook(null);
    }

}
