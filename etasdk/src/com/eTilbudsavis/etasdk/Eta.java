/*******************************************************************************
* Copyright 2014 eTilbudsavis
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
*******************************************************************************/
package com.eTilbudsavis.etasdk;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.eTilbudsavis.etasdk.EtaObjects.Shoppinglist;
import com.eTilbudsavis.etasdk.EtaObjects.ShoppinglistItem;
import com.eTilbudsavis.etasdk.EtaObjects.User;
import com.eTilbudsavis.etasdk.ImageLoader.ImageLoader;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Network.HttpStack;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.RequestQueue;
import com.eTilbudsavis.etasdk.Network.Impl.DefaultHttpNetwork;
import com.eTilbudsavis.etasdk.Network.Impl.HttpURLNetwork;
import com.eTilbudsavis.etasdk.Network.Impl.MemoryCache;
import com.eTilbudsavis.etasdk.Network.Impl.NetworkImpl;
import com.eTilbudsavis.etasdk.Utils.Utils;

/**
 * 
 * The main class for interacting with the eTilbudsavis SDK / API
 * 
 * @author Danny Hvam - danny@etilbudsavis.dk
 * @version 2.1.0
 *
 */
public class Eta {

	public static final String TAG_PREFIX = "EtaSdk-";
	public static final String TAG = TAG_PREFIX + Eta.class.getSimpleName();
	
	private static final int DEFAULT_THREAD_COUNT = 3;
	private static final String DEFAULT_THREAD_NAME = "etasdk-";
	
	/** The Eta singleton */
	private static Eta mEta;
	
	/** Application context for usage in the SDK */
	private Context mContext;
	
	/** The developers APIkey */
	private String mApiKey;
	
	/** The developers APIsecret */
	private String mApiSecret;

	/** The developers app version, this isn't strictly necessary */
	private String mAppVersion;
	
	/** The SDK settings */
	private Settings mSettings;
	
	/** A session manager, for handling all session requests, user information e.t.c. */
	private SessionManager mSessionManager;
	
	/** The current location that the SDK is aware of */
	private final EtaLocation mLocation;
	
	/** Manager for handling all {@link Shoppinglist}, and {@link ShoppinglistItem} */
	private ListManager mListManager;
	
	/** Manager for doing asynchronous sync */
	private SyncManager mSyncManager;
	
	/** A static handler for usage in the SDK, this will help prevent leaks */
	private static Handler mHandler;
	
	/** The current state of the SDK */
	private boolean mResumed = false;
	
	/** A {@link RequestQueue} implementation to handle all API requests */
	private RequestQueue mRequestQueue;
	
	/** System manager for getting the connectivity status */
	private ConnectivityManager mConnectivityManager;
	
	private ExecutorService mExecutor;
	
	/**
	 * Default constructor, this is private to allow us to create a singleton instance
	 * @param apiKey An API v2 apiKey
	 * @param apiSecret An API v2 apiSecret (matching the apiKey)
	 * @param ctx A context
	 */
	private Eta(String apiKey, String apiSecret, Context ctx) {
		
		// Get a context that isn't likely to disappear with an activity.
		mContext = ctx.getApplicationContext();
		mApiKey = apiKey;
		mApiSecret = apiSecret;
		
		mHandler = new Handler(Looper.getMainLooper());
		ThreadFactory tf = new DefaultThreadFactory(DEFAULT_THREAD_NAME);
		mExecutor = Executors.newFixedThreadPool(DEFAULT_THREAD_COUNT, tf);
		
		HttpStack stack = null;
        if (Build.VERSION.SDK_INT > 8) {
            stack = new HttpURLNetwork();
        } else {
            stack = new DefaultHttpNetwork();
        }
        
		mRequestQueue = new RequestQueue(this, new MemoryCache(), new NetworkImpl(stack));
		mRequestQueue.start();
		
		mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		try {
			String name = mContext.getPackageName();
			String version = mContext.getPackageManager().getPackageInfo(name, 0 ).versionName;
			setAppVersion(version);
		} catch (NameNotFoundException e) {
			EtaLog.e(TAG, null, e);
		}
		
		mSettings = new Settings(mContext);
		mLocation = mSettings.getLocation();
		mSessionManager = new SessionManager(Eta.this);
		mListManager = new ListManager(Eta.this);
		mSyncManager = new SyncManager(Eta.this);
        ImageLoader.init(Eta.this);
		
	}
	
	/**
	 * Singleton access to a {@link Eta} object.
	 * 
	 * <p>Be sure to {@link Eta#createInstance(String, String, Context) create an
	 * instance} before invoking this method, or bad things will happen.</p>
	 * 
	 * @throws IllegalStateException If {@link Eta} no instance is available
	 */
	public static Eta getInstance() {
		synchronized (Eta.class) {
			if (mEta == null) {
				throw new IllegalStateException("Eta.createInstance() needs to be"
						+ "called before Eta.getInstance()");
			}
			return mEta;
		}
	}
	
	/**
	 * Creates a new instance of {@link Eta}.
	 * 
	 * <p>This method will instantiate a new instance of {@link Eta}, than can be
	 * used throughout your app. But you can only create one instance pr. context
	 * (or app), this amongst others ensures some session and user safety.</p>
	 * 
	 * @param apiKey An API v2 apiKey
	 * @param apiSecret An API v2 apiSecret (matching the apiKey)
	 * @param ctx A context
	 */
	public static void createInstance(String apiKey, String apiSecret, Context ctx) {
		
		if (mEta == null) {
			synchronized (Eta.class) {
				if (mEta == null) {
					mEta = new Eta(apiKey, apiSecret, ctx);
				}
			}
		} else {
			EtaLog.d(TAG, "Eta instance already created");
		}
		
	}
	
	/**
	 * Check if the instance have been instantiated.
	 * <p>To instantiate an instance use {@link #createInstance(String, String, Context)}</p>
	 * @return {@code true} if Eta is instantiated, else {@code false}
	 */
	public static boolean isInstanciated() {
		synchronized (Eta.class) {
			return mEta != null;
		}
	}
	
	/**
	 * This returns a pool of threads for executing various SDK tasks on a thread.
	 * @return An {@link ExecutorService}
	 */
	public ExecutorService getExecutor() {
		return mExecutor;
	}
	
	/**
	 * Method for determining the current network state
	 * @return true if network connectivity exists, false otherwise.
	 */
	public boolean isOnline() {
		NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnected();
	}

	/**
	 * The {@link Context}, {@link Eta} has been instantiated with
	 * 
	 * <p>Note that this is the {@link Application#getApplicationContext()
	 * application context}, and therefore has some restrictions</p>
	 * 
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
		EtaLog.v(TAG, "AppVersion: " + (mAppVersion == null ? "version not valid" : mAppVersion));
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
	@SuppressWarnings("unchecked")
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
		return mSessionManager.getSession().getUser();
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
     * Get the current instance of {@link ListManager}.
     * @return A ListManager
     */
	public ListManager getListManager() {
		return mListManager;
	}
	
    /**
     * Get the current instance of {@link SyncManager}.
     * @return A SyncManager
     */
	public SyncManager getSyncManager() {
		return mSyncManager;
	}
	
	/**
	 * Get a static handler, created on the main looper. <br>
	 * Use this to avoid memory leaks.
	 * @return a handler
	 */
	public Handler getHandler() {
		return mHandler;
	}
	
	/**
	 * @deprecated
	 */
	public void destroyInstance() {
		destroy();
	}

	/**
	 * First clears all preferences with {@link #clear()}, and then {@code null's}
	 * this instance of Eta
	 * 
	 * <p>For further use of {@link Eta} after this, you must invoke
	 * {@link #createInstance(String, String, Context) set()} it again.</p>
	 */
	public void destroy() {
		clear();
		synchronized (Eta.class) {
			mEta = null;
		}
	}
	
	/**
	 * Clears all preferences that the SDK has created.
	 * 
	 * <p>This includes invalidating the current session and user, clearing all
	 * rows in DB, clearing preferences, resetting location info, clearing API
	 * cache, e.t.c.</p>
	 */
	public void clear() {
		mSessionManager.invalidate();
		mSettings.clear();
		mLocation.clear();
		mRequestQueue.clear();
		mListManager.clear();
		EtaLog.getLogger().getLog().clear();
	}
	
	/**
	 * Get the current state of the Eta instance
	 * @return {@code true} if in resumed state, else {@code false}
	 */
	public boolean isResumed() {
		return mResumed;
	}

	/**
	 * Method must be called when the current activity is put to background
	 */
	@SuppressLint("NewApi")
	public void onPause() {
		if (mResumed) {
			mResumed = false;
			mSettings.saveLocation(mLocation);
			mListManager.onPause();
			mSyncManager.onPause();
			mSessionManager.onPause();
			for (PageflipWebview p : PageflipWebview.pageflips) {
				p.pause();
			}
			mSettings.setLastUsageNow();
		}
	}
	
	/**
	 * Method must be called when the activity is resuming
	 */
	@SuppressLint("NewApi")
	public void onResume() {
		if (!mResumed) {
			mResumed = true;
			mSessionManager.onResume();
			mListManager.onResume();
			mSyncManager.onResume();
			for (PageflipWebview p : PageflipWebview.pageflips) {
				p.resume();
			}
		}
	}

}
