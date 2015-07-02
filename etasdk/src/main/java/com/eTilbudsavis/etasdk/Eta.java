/*******************************************************************************
 * Copyright 2015 eTilbudsavis
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
package com.eTilbudsavis.etasdk;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.eTilbudsavis.etasdk.database.DatabaseWrapper;
import com.eTilbudsavis.etasdk.imageloader.ImageLoader;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.Shoppinglist;
import com.eTilbudsavis.etasdk.model.ShoppinglistItem;
import com.eTilbudsavis.etasdk.model.User;
import com.eTilbudsavis.etasdk.network.HttpStack;
import com.eTilbudsavis.etasdk.network.Request;
import com.eTilbudsavis.etasdk.network.RequestQueue;
import com.eTilbudsavis.etasdk.network.impl.DefaultHttpNetwork;
import com.eTilbudsavis.etasdk.network.impl.HttpURLNetwork;
import com.eTilbudsavis.etasdk.network.impl.MemoryCache;
import com.eTilbudsavis.etasdk.network.impl.NetworkImpl;
import com.eTilbudsavis.etasdk.utils.ActivityCounter;
import com.eTilbudsavis.etasdk.utils.Utils;
import com.eTilbudsavis.etasdk.utils.Validator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * Eta is the main class for interacting with the eTilbudsavis SDK / API.
 *
 * Eta is a singleton, that will have to be created with a context, from there on out
 * you can invoke with the static method getInstance.
 *
 * <h3>Requirements</h3>
 * There is only a few requirements to get going. You will need to get an
 * API key, and API secret. You can request a set at 
 * <a href="https://etilbudsavis.dk/developers/"> etilbudsavis.dk </a>
 * (look for "Apply for Developer Program" in the top right corner).
 *
 *
 * You will have to add the API key and API secret as meta data in your AndroidManifest, in the following way:
 *
 * <pre>
 * &lt;meta-data android:name="com.eTilbudsavis.etasdk.api_key" android:value="YOUR_API_KEY" /&gt;
 * &lt;meta-data android:name="com.eTilbudsavis.etasdk.api_secret" android:value="YOUR_API_SECRET" /&gt;
 * </pre>
 *
 * <pre>
 * &lt;meta-data android:name="com.eTilbudsavis.etasdk.develop.api_key" android:value="YOUR_DEVELOP_API_KEY" /&gt;
 * &lt;meta-data android:name="com.eTilbudsavis.etasdk.develop.api_secret" android:value="YOUR_DEVELOP_API_SECRET" /&gt;
 * </pre>
 *
 *
 *
 * <ul>
 * 	<li> First invoke Eta.create(Context context)
 * 	<li> Then call Eta.getInstance() to get the current instance of Eta
 * </ul>
 *
 *
 * <h3>Usage</h3>
 *
 * <ol>
 * 	<li> First invoke Eta.create(Context context)
 * 	<li> Then call Eta.getInstance() to get the current instance of Eta
 * </ol>
 *
 *
 * @author Danny Hvam - danny@etilbudsavis.dk
 * @version 2.2.1
 *
 */
public class Eta {

    public static final String TAG = Constants.getTag(Eta.class);

    private static final int DEFAULT_THREAD_COUNT = 3;

    /** The Eta singleton */
    private static Eta mEta;
    /** A static handler for usage in the SDK, this will help prevent leaks */
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    /** Counting the number of active activities, to determine when to stop any long running activities */
//	private final AtomicInteger mActivityCounter = new AtomicInteger();
    private final ActivityCounter mActivityCounter = new ActivityCounter();
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
    private EtaLocation mLocation;
    /** Manager for handling all {@link Shoppinglist}, and {@link ShoppinglistItem} */
    private ListManager mListManager;
    /** Manager for doing asynchronous sync */
    private SyncManager mSyncManager;
    /** A {@link RequestQueue} implementation to handle all API requests */
    private RequestQueue mRequestQueue;
    /** My go to executor service */
    private ExecutorService mExecutor;
    /** The ImageLoader for use by clients, and SDK */
    private ImageLoader mImageLoader;

    /** The development flag, indicating the app is in development */
    private boolean mDevelop = false;

    /**
     * Default constructor, this is private to allow us to create a singleton instance
     * @param context A context
     */
    private Eta(Context context) {
        // Get application context, to avoid memory leaks (e.g. holding a reference to an Activity)
        mContext = context.getApplicationContext();
        init();
    }

    /**
     * Singleton access to a {@link Eta} object.
     *
     * <p>Be sure to {@link Eta#create(Context)} create an
     * instance} before invoking this method, or bad things will happen.</p>
     *
     * @throws IllegalStateException If {@link Eta} no instance is available
     */
    public static Eta getInstance() {
        synchronized (Eta.class) {
            if (mEta == null) {
                throw new IllegalStateException("Eta.create() needs to be invoked prior to Eta.getInstance()");
            }
            return mEta;
        }
    }

    /**
     * Creates a new instance of {@link Eta}.
     *
     * <p>This method will instantiate a new instance of {@link Eta}, than can be
     * used throughout your app.</p>
     *
     * @param ctx A context
     */
    public static Eta create(Context ctx) {

        synchronized (Eta.class) {
            if (isCreated()) {
                EtaLog.v(TAG, "Eta instance already created - ignoring");
            } else {
                mEta = new Eta(ctx);
            }
            return mEta;
        }

    }

    /**
     * Check if the instance have been instantiated.
     * <p>To instantiate an instance use {@link #create(Context)}</p>
     * @return {@code true} if Eta is instantiated, else {@code false}
     */
    public static boolean isCreated() {
        synchronized (Eta.class) {
            return mEta != null;
        }
    }

    private void init() {

        setupKeys(mContext);
        setAppVersion(Utils.getAppVersion(mContext));
        mExecutor = Executors.newFixedThreadPool(DEFAULT_THREAD_COUNT, new EtaThreadFactory());
        mImageLoader = new ImageLoader(mContext, mExecutor);
        mSettings = new Settings(mContext);

        HttpStack stack = null;
        if (Build.VERSION.SDK_INT > 8) {
            stack = new HttpURLNetwork();
        } else {
            stack = new DefaultHttpNetwork();
        }

        mRequestQueue = new RequestQueue(Eta.this, new MemoryCache(), new NetworkImpl(stack));
        mRequestQueue.start();

        mLocation = mSettings.getLocation();

        // Session manager implicitly requires Settings
        mSessionManager = new SessionManager(Eta.this);

        DatabaseWrapper db = DatabaseWrapper.getInstance(Eta.this);
        mListManager = new ListManager(Eta.this, db);
        mSyncManager = new SyncManager(Eta.this, db);

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
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
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
        if (!isKeySecretOk()) {
            setupKeys(mContext);
        }
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
    public String getAppVersion() {
        return mAppVersion;
    }

    /**
     * Set the version of your app, for better statistics collection on http://insight.etilbudsavis.dk/.<br><br>
     * App version should follow http://semver.org/ specifications (MAJOR.MINOR.PATCH), e.g.:<br>
     * <li> 1.0.0
     * <li> 1.0.0-beta
     * <li> 1.0.0-rc.1
     *
     */
    public void setAppVersion(String appVersion) {
        if (Validator.isAppVersionValid(appVersion)) {
            mAppVersion = appVersion;
        }
        EtaLog.v(TAG, "AppVersion: " + String.valueOf(mAppVersion));
    }

    /**
     * Returns the API secret found at http://etilbudsavis.dk/api/.
     * @return API secret as String
     */
    public String getApiSecret() {
        if (!isKeySecretOk()) {
            setupKeys(mContext);
        }
        return mApiSecret;
    }

    public ImageLoader getImageloader() {
        return mImageLoader;
    }

    /**
     * Shortcut method for adding requests to the RequestQueue.
     * @param request to be performed
     * @return the request
     */
    public Request<?> add(Request<?> request) {
        return mRequestQueue.add(request);
    }

    /**
     * Get the current instance of request queue. This is the queue
     * responsible for any API request passed into the SDK.
     * @return The current RequestQueue
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
     * Get the current state of the Eta instance
     * @return {@code true} if in resumed state, else {@code false}
     */
    public boolean isStarted() {
        return mActivityCounter.isStarted();
    }

    /**
     * Indicates whether the SDK is in develop state, and is using development keys.
     * @return true is in develop state, else false
     */
    public boolean isDevelop() {
        return mDevelop;
    }

    /**
     * Set the ETA SDK to use development options, such as development API key/secret.
     * This must be called prior to calling start.
     * @param develop <code>true</code> to set development state.
     */
    public void setDevelop(boolean develop) {
        mDevelop = develop;
        if (isStarted()) {
            EtaLog.i(TAG, "Re-registering apiKey and apiSecret");
            setupKeys(mContext);
        }
    }

    private boolean isKeySecretOk() {
        if (mApiKey == null || mApiSecret == null) {
            // Reset both to keep sane state
            mApiKey = null;
            mApiSecret = null;
        }
        return mApiKey != null && mApiSecret != null;
    }

    private void setupKeys(Context c) {

        Bundle b = Utils.getMetaData(c);
        if (b == null) {
            throw new IllegalStateException("Package meta data not available.");
        }

        // If the develop flag has been set, then try to get the matching keys
        if (isDevelop()) {

            mApiKey = b.getString(Constants.META_DEVELOP_API_KEY);
            mApiSecret = b.getString(Constants.META_DEVELOP_API_SECRET);

            if (isKeySecretOk()) {
                EtaLog.i(TAG, "Using development key/secret");
            } else {
                EtaLog.w(TAG, "Debug flag set, but no develop keys found in AndroidManifest.");
            }

        }

        // If no develop keys were found, then get the production keys
        if (!isKeySecretOk()) {
            mApiKey = b.getString(Constants.META_API_KEY);
            mApiSecret = b.getString(Constants.META_API_SECRET);
        }

        // If no keys were found at all, then die
        if (!isKeySecretOk()) {
            throw new IllegalStateException("API key/secret missing from AndroidManifest.xml");
        }

    }

    /**
     * First clears all preferences with {@link #clear()}, and then {@code null's}
     * this instance of Eta
     *
     * <p>Further use of {@link Eta} after this, you must invoke
     * {@link #create(Context)} it again.</p>
     */
    public void destroy() {
        synchronized (Eta.class) {
            internalStop();
            clear();
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
        if (!isStarted()) {
            mSessionManager.invalidate();
            mSettings.clear();
            mLocation.clear();
            mRequestQueue.clear();
            mListManager.clear();
            EtaLog.getLogger().getLog().clear();
        }
    }

//	Runnable termination = new Runnable() {
//
//		public void run() {
//			if (isStarted()) {
//				EtaLog.i(TAG, "Eta has been resumed, bail out");
//				return;
//			}
//			EtaLog.i(TAG, "Finalizing long running tasks...");
//			int retries = 0;
//			mRequestQueue.stop();
//			mExecutor.shutdown();
//			while (retries < 5) {
//				retries ++;
//				try {
//					if (mExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
//						break;
//					}
//				} catch (InterruptedException e) {
//					// Ignore
//				}
//			}
//			finalCleanup();
//			EtaLog.i(TAG, "SDK cleanup complete");
//		}
//	};

//	private void finalCleanup() {
//		// TODO don't need to null everything
//		synchronized (Eta.class) {
//			mEta = null;
//			mApiKey = null;
//			mApiSecret = null;
//			mAppVersion = null;
//			mContext = null;
//			mExecutor = null;
//			mListManager = null;
//			mRequestQueue = null;
//			mSessionManager = null;
//			mSettings = null;
//			mSyncManager = null;
//		}
//	}

    public void onStart() {
        setupKeys(mContext);
        if (mActivityCounter.increment()) {
            internalStart();
        }
    }

    private void internalStart() {
//		mHandler.removeCallbacks(termination);
        mSessionManager.onStart();
        mListManager.onStart();
        mSyncManager.onStart();
        EtaLog.v(TAG, "SDK has been started");
    }

    public void onStop() {
        if (mActivityCounter.decrement()) {
            internalStop();
        }
    }

    private void internalStop() {
        mSettings.saveLocation(mLocation);
        mListManager.onStop();
        mSyncManager.onStop();
        mSessionManager.onStop();
        mSettings.setLastUsageNow();
        mActivityCounter.reset();
//		mHandler.postDelayed(termination, Utils.SECOND_IN_MILLIS * 5);
        EtaLog.v(TAG, "SDK has been stopped");
    }

}
