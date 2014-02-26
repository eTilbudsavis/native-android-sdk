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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;

import com.eTilbudsavis.etasdk.EtaObjects.User;
import com.eTilbudsavis.etasdk.NetworkHelpers.HttpNetwork;
import com.eTilbudsavis.etasdk.NetworkInterface.Cache;
import com.eTilbudsavis.etasdk.NetworkInterface.Network;
import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.NetworkInterface.RequestQueue;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

// Main object for interacting with the SDK.
public class Eta {
	
	public static final String TAG = "ETA";
	
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
	private Cache mCache;
	private ConnectivityManager mConnectivityManager;
	
	private Eta() {
		mCache = new Cache();
		Network n = new HttpNetwork();
		mRequestQueue = new RequestQueue(this, mCache, n);
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
	
	/**
	 * This method must be called before performing any requests.<br><br>
	 * It's given that the SDK cannot perform any requests without an apiKey and apiSecret.
	 * @param apiKey for eTilbudsavis API v2
	 * @param apiSecret for eTilbudsavis API v2
	 * @param context of your application
	 */
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
			mLocation = new EtaLocation(this);
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
	
	/**
	 * Shortcut method for adding requests to the RequestQueue.
	 * @param request to be performed
	 * @return the request
	 */
	public <T> Request<T> add(Request<T> request) {
		return mRequestQueue.add(request);
	}
	
	/**
	 * Get the current instance of request queue. This is the queue 
	 * responsible for any API request passed into the SDK.
	 * @return
	 */
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

    /**
     * Get the ETA SDK cache for various items and objects.
     * @return the current cache object
     */
    public Cache getCache() {
            return mCache;
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
	 * First clears all preferences with {@link #clear() clear()}, and then null's this instance of Eta.<br>
	 * Therefore you must get a new instance and then {@link #set(String, String, Context) set()} it again.
	 * if you want to continue using the SDK. 
	 */
	public void reset() {
		clear();
		mCache.clear();
		mListManager.clear();
		mEta = null;
	}
	
	/**
	 * Clears all preferences that the SDK has created.<br><br>
	 * 
	 * This includes invalidating the current session and user, clearing all rows in DB, clearing preferences,
	 * resetting location info, clearing API cache, e.t.c.
	 */
	public void clear() {
		mSessionManager.invalidate();
		mSettings.clear();
		mLocation.clear();
		mRequestQueue.getLog().clear();
		EtaLog.getExceptionLog().clear();
	}
	
	/**
	 * Get the current state of the Eta instance
	 * @return if it's resumed.
	 */
	public boolean isResumed() {
		return mResumed;
	}

	/**
	 * Method must be called when the current activity is put to background.<br>
	 * This amongst other stops any sync services.
	 */
	@SuppressLint("NewApi")
	public void onPause() {
		if (mResumed) {
			mResumed = false;
			mListManager.onPause();
			mSessionManager.onPause();
			for (PageflipWebview p : PageflipWebview.pageflips)
				p.onPause();
		}
	}
	
	/**
	 * Method must be called when the activity is resuming from background.<br>
	 * This will amongst other start any sync services.
	 */
	@SuppressLint("NewApi")
	public void onResume() {
		if (!mResumed) {
			mResumed = true;
			mSessionManager.onResume();
			mListManager.onResume();
			for (PageflipWebview p : PageflipWebview.pageflips)
				p.onResume();
		}
	}

}