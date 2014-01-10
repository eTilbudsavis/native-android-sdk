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
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.eTilbudsavis.etasdk.EtaObjects.User;
import com.eTilbudsavis.etasdk.NetworkHelpers.HttpNetwork;
import com.eTilbudsavis.etasdk.NetworkInterface.Cache;
import com.eTilbudsavis.etasdk.NetworkInterface.Network;
import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Param;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Sort;
import com.eTilbudsavis.etasdk.NetworkInterface.RequestQueue;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

// Main object for interacting with the SDK.
public class Eta implements Serializable {

	private static final long serialVersionUID = 1L;
	public static final String TAG = "ETA";

	public static boolean DEBUG_ENDPOINT = false;
	public static boolean DEBUG_LOGD = false;
	public static boolean DEBUG_PAGEFLIP = false;
	
	private static Eta mEta;
	
	private Context mContext;
	private String mApiKey;
	private String mApiSecret;
	private String mAppVersion;
	private Settings mSettings;
	private SessionManager mSessionManager;
	private EtaLocation mLocation;
	private ListManager mListManager;
	private static Handler mHandler;
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
		
		mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		mContext = context;
		mApiKey = apiKey;
		mApiSecret = apiSecret;

		try {
			String name = context.getPackageName();
			String version = context.getPackageManager().getPackageInfo(name, 0 ).versionName;
			setAppVersion(version);
		} catch (NameNotFoundException e) {
			EtaLog.d(TAG, e);
		}

		if (!isSet()) {
			mSettings = new Settings(mContext);
			mLocation = new EtaLocation();
			mListManager = new ListManager(Eta.this);
			mSessionManager = new SessionManager(Eta.this);
		} else {
			EtaLog.d(TAG, "Eta already set. apiKey, apiSecret and context has been switched");
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
	 * Set the version of your app, for better statistics collection on http://insight.etilbudsavis.dk/.<br><br>
	 * App version should follow http://semver.org/ specifications (MAJOR.MINOR.PATCH), e.g.:<br>
	 * <li> 1.0.0
	 * <li> 1.0.0-beta
	 * <li> 1.0.0-rc.1
	 * 
	 * @return API key as String
	 */
	public void setAppVersion(String appVersion) {
		if (Utils.validVersion(appVersion)) {
			mAppVersion = appVersion;
		}
		EtaLog.d(TAG, "AppVersion: " + (mAppVersion == null ? "version not valid" : mAppVersion));
	}

	/**
	 * Set the version of your app, for better statistics collection on http://insight.etilbudsavis.dk/.<br><br>
	 * App version should follow http://semver.org/ specifications (MAJOR.MINOR.PATCH), e.g.:<br>
	 * <li> 1.0.0
	 * <li> 1.0.0-beta
	 * <li> 1.0.0-rc.1
	 * 
	 * @return API key as String
	 */
	public String getAppVersion() {
		return mAppVersion;
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

	public RequestQueue getRequestQueue() {
		return mRequestQueue;
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
	 * Get a static handler, created on the main looper. <br>
	 * Use this to avoid memory leaks.
	 * @return a handler
	 */
	public Handler getHandler() {
		if (mHandler == null) {
			mHandler = new Handler(Looper.getMainLooper());
		}
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

	@SuppressLint("NewApi")
	public void onPause() {
		if (mResumed) {
			mResumed = false;
			mLocation.saveState();
			mListManager.onPause();
			for (PageflipWebview p : PageflipWebview.pageflips)
				p.onPause();
		}
	}

	@SuppressLint("NewApi")
	public void onResume() {
		if (!mResumed) {
			mResumed = true;
			mLocation.restoreState();
			mListManager.onResume();
			mSessionManager.onResume();
			for (PageflipWebview p : PageflipWebview.pageflips)
				p.onResume();
		}
	}

}