/*!
 * eTilbudsavis ApS
 * (c) 2012, eTilbudsavis ApS
 * http://etilbudsavis.dk
 */
/**
 * @fileoverview	Main class.
 * @author			Morten Bo <morten@etilbudsavis.dk>
 * 					Danny Hvam <danny@etilbudsavid.dk>
 * @version			0.3.0
 */
package com.eTilbudsavis.etasdk;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;

import com.eTilbudsavis.etasdk.EtaObjects.User;
import com.eTilbudsavis.etasdk.NetworkHelpers.HttpNetwork;
import com.eTilbudsavis.etasdk.NetworkInterface.Cache;
import com.eTilbudsavis.etasdk.NetworkInterface.Network;
import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Param;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Sort;
import com.eTilbudsavis.etasdk.NetworkInterface.RequestQueue;
import com.eTilbudsavis.etasdk.Utils.Utils;

// Main object for interacting with the SDK.
public class Eta implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public static final String TAG = "ETA";
	public static boolean DEBUG = false;
	private List<Pageflip> mPageflips = new ArrayList<Pageflip>();
	
	private static Eta mEta;
	
	private Context mContext;
	private String mApiKey;
	private String mApiSecret;
	private Settings mSettings;
	private SessionManager mSessionManager;
	private EtaLocation mLocation;
	private ListManager mListManager;
	private static Handler mHandler = new Handler();
	private boolean mResumed = false;
	private RequestQueue mRequestQueue;
	private ConnectivityManager mConnectivityManager;
	
	private Eta() {
		Cache c = new Cache();
		Network n = new HttpNetwork();
		mRequestQueue = new RequestQueue(this, c, n);
		mRequestQueue.start();
	}

	/**
	 * TODO: Write a long story about usage, this will basically be the documentation
	 * @param apiKey The API key found at http://etilbudsavis.dk/api/
	 * @param apiSecret The API secret found at http://etilbudsavis.dk/api/
	 * @param context The context of the activity instantiating this class.
	 */
	public static Eta getInstance() {
		if (mEta == null) {
			synchronized (Eta.class) {
				if (mEta == null) {
					mEta = new Eta();
				}
			}
		}
		return mEta;
	}
	
	public void set(String apiKey, String apiSecret, Context context) {

		mContext = context;
		mApiKey = apiKey;
		mApiSecret = apiSecret;
		
		if (!isSet()) {
			mSettings = new Settings(mContext);
			mLocation = new EtaLocation();
			mListManager = new ListManager(Eta.this);
			mSessionManager = new SessionManager(Eta.this);
		} else {
			Utils.logd(TAG, "Eta already set. apiKey, apiSecret and context has been switched");
		}
		
	}
	
	public boolean isSet() {
		return mApiKey != null && mApiSecret == null;
	}

	public boolean isOnline() {
		NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnected();
	}
	
	/**
	 * The context, the given Eta has been set in.<br>
	 * This context, does not necessarily have real estate on screen
	 * to instantiate any views.
	 * @return A context
	 */
	public Context getContext() {
		return mContext;
	}

	/**
	 * Returns the API key found at http://etilbudsavis.dk/api/.
	 * @return API key as String
	 */
	public String getApiKey() {
		return mApiKey;
	}
	
	/**
	 * Returns the API secret found at http://etilbudsavis.dk/api/.
	 * @return API secret as String
	 */
	public String getApiSecret() {
		return mApiSecret;
	}
	
	public <T> Request<T> add(Request<T> r) {
		mRequestQueue.add(r);
		return r;
	}
	
	/**
	 * Get the currently active session.
	 * @return a session
	 */
	public SessionManager getSessionManager() {
		return mSessionManager;
	}

	/**
	 * Get the current user
	 * @return a user
	 */
	public User getUser() {
		return getSessionManager().getSession().getUser();
	}
	
	/**
	 * Get the settings, that ETA SDK is using.
	 */
	public Settings getSettings() {
		return mSettings;
	}

	/**
	 * A location object used by ETA, when making API requests.
	 * This object should be edited when ever you want to change location.
	 * @return <li> A location object
	 */
	public EtaLocation getLocation() {
		return mLocation;
	}
	
	public ListManager getListManager() {
		return mListManager;
	}
	
	/**
	 * Get a static handler, to avoid memory leaks.
	 * @return a handler
	 */
	public Handler getHandler() {
		return mHandler;
	}
	
	/**
	 * Clears ALL preferences that the SDK has created.<br><br>
	 * 
	 * This includes the session and user.
	 * @return Returns true if the new values were successfully written to persistent storage.
	 */
	public boolean clearPreferences() {
		mSessionManager.invalidate();
		return mSettings.clear();
	}
	
	public boolean isResumed() {
		return mResumed;
	}
	
	public Eta debug(boolean useDebug) {
		DEBUG = useDebug;
		return this;
	}

	@SuppressLint("NewApi")
	public void onPause() {
		mResumed = false;
		mListManager.onPause();
		mLocation.saveState();
		for (Pageflip p : mPageflips)
			p.onPause();
	}
	
	@SuppressLint("NewApi")
	public void onResume() {
		mResumed = true;
		mListManager.onResume();
		mLocation.restoreState();
		for (Pageflip p : mPageflips)
			p.onResume();
	}
	
	private Bundle getApiParams(int offset, int limit, String orderBy) {
		Bundle apiParams = new Bundle();
		apiParams.putInt(Param.OFFSET, offset);
		apiParams.putInt(Param.LIMIT, limit);
		if (orderBy != null) 
			apiParams.putString(Sort.ORDER_BY, orderBy);
		return apiParams;
	}
	
	private Bundle getSearchApiParams(int offset, int limit, String orderBy, String query) {
		Bundle apiParams = getApiParams(offset, limit, orderBy);
		apiParams.putString(Param.QUERY, query);
		return apiParams;
	}

}