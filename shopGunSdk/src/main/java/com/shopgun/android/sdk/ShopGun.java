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

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.shopgun.android.sdk.api.Environment;
import com.shopgun.android.sdk.api.ThemeEnvironment;
import com.shopgun.android.sdk.corekit.LifecycleManager;
import com.shopgun.android.sdk.corekit.UserAgentInterceptor;
import com.shopgun.android.sdk.corekit.realm.SgnRealmModule;
import com.shopgun.android.sdk.database.DatabaseWrapper;
import com.shopgun.android.sdk.eventskit.EzEvent;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.ShoppinglistItem;
import com.shopgun.android.sdk.network.Cache;
import com.shopgun.android.sdk.network.Network;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.RequestQueue;
import com.shopgun.android.sdk.network.impl.DefaultRedirectProtocol;
import com.shopgun.android.sdk.network.impl.HttpURLNetwork;
import com.shopgun.android.sdk.network.impl.MemoryCache;
import com.shopgun.android.sdk.network.impl.NetworkImpl;
import com.shopgun.android.sdk.shoppinglists.ListManager;
import com.shopgun.android.sdk.shoppinglists.SyncManager;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.SgnThreadFactory;
import com.shopgun.android.sdk.utils.SgnUserAgent;
import com.shopgun.android.sdk.utils.SgnUtils;
import com.shopgun.android.sdk.utils.Version;
import com.shopgun.android.utils.PackageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 *
 * ShopGun is the main class for interacting with the ShopGun SDK / API.
 *
 * ShopGun is a singleton, that will have to be created with a context, from there on out
 * you can invoke with the static method getInstance.
 *
 * <h3>Requirements</h3>
 * There is only a few requirements to get going. You will need to get an
 * API key, and API secret. You can request a set at
 * <a href="https://etilbudsavis.dk/developers/">eTilbudsavis.dk</a>, under "Manage Apps".
 *
 *
 * You will have to add the API key and API secret as meta data in your AndroidManifest, in the following way:
 *
 * {@code <meta-data android:name="com.shopgun.android.sdk.api_key" android:value="YOUR_API_KEY" />}
 * {@code <meta-data android:name="com.shopgun.android.sdk.api_secret" android:value="YOUR_API_SECRET" />}
 *
 *
 * {@code <meta-data android:name="com.shopgun.android.sdk.develop.api_key" android:value="YOUR_DEVELOP_API_KEY" />}
 * {@code <meta-data android:name="com.shopgun.android.sdk.develop.api_secret" android:value="YOUR_DEVELOP_API_SECRET" />}
 *
 * <h3>Usage</h3>
 *
 * First instantiate the instance with {@link Builder(Context) new ShopGun.Builder(Application).build()}.
 * Once the SDk is instantiated, ShopGun have been setup and you can now
 * refer to the singleton by calling {@link ShopGun#getInstance()}.
 *
 * <h3>Demo</h3>
 * For further instructions on the usage of ShopGun, please refer to the ShopGun SDK Demo included in the SDK.
 * The ShopGun SDK Demo, demonstrates some of the setup methods, and features included in the SDK.
 *
 */
public class ShopGun {

    public static final String TAG = Constants.getTag(ShopGun.class);

    public static final Version VERSION = new Version(4,0,0,"dev");

    /** The ShopGun singleton */
    private static ShopGun mSingleton;

    /** Application context for usage in the SDK */
    private Context mContext;
    private LifecycleManager mLifecycleManager;
    /** A static handler for usage in the SDK, this will help prevent leaks */
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    /** My go to executor service */
    private final ExecutorService mExecutor;
    /** The development flag, indicating the app is in development */
    private boolean mDevelop = false;
    /** The current API environment in use */
    private Environment mEnvironment;
    /** The current API environment in use for themes (used for e.g. shoppinglists */
    private ThemeEnvironment mThemeEnvironment;
    /** The http client of choice for SDK traffic */
    private OkHttpClient mClient;
    /** The session id for this specific session */
    private String mSessionId;
    /** The device id, this will if possible be persisted across installations */
    private String mDeviceId;

    // Things we'd like to get rid of

    /** The SDK settings */
    private final Settings mSettings;
    /** A session manager, for handling all session requests, user information e.t.c. */
    private final SessionManager mSessionManager;
    /** The current location that the SDK is aware of */
    private final SgnLocation mLocation;
    /** Manager for handling all {@link Shoppinglist}, and {@link ShoppinglistItem} */
    private final ListManager mListManager;
    /** Manager for doing asynchronous sync */
    private final SyncManager mSyncManager;
    /** A {@link RequestQueue} implementation to handle all API requests */
    private final RequestQueue mRequestQueue;
    /** A RealmConfiguration specifically for the SDK */
    private final RealmConfiguration mRealmConfiguration;

    private LifecycleManager.Callback mLifecycleCallback = new LifecycleManager.SimpleCallback() {
        @Override
        public void onCreate(Activity activity) {
            mSessionId = SgnUtils.createUUID();
            mSessionManager.onStart();
            mListManager.onStart();
            mSyncManager.onStart();
            mSettings.incrementUsageCount();
            SgnLog.v(TAG, "onCreate");
        }

        @Override
        public void onStart(Activity activity) {
            if (mSettings.getUsageCount() == 0) {
                EzEvent.firstClientSessionOpened().track();
            }
            EzEvent.clientSessionOpened().track();
        }

        @Override
        public void onStop(Activity activity) {
            EzEvent.clientSessionClosed().track();
        }

        @Override
        public void onDestroy(Activity activity) {
            mSettings.saveLocation(mLocation);
            mListManager.onStop();
            mSyncManager.onStop();
            mSessionManager.onStop();
            mSettings.setLastUsedTimeNow();
            mSettings.setSessionId(mSessionId);
            SgnLog.v(TAG, "onDestroy");
        }
    };

    private ShopGun(Builder builder) {
        // Get application context, to avoid memory leaks (e.g. holding a reference to an Activity)
        mContext = builder.application.getApplicationContext();
        mDevelop = builder.develop;
        mEnvironment = builder.environment;
        mThemeEnvironment = builder.themeEnvironment;
        mExecutor = builder.executorService;
        mClient = builder.okHttpClient;
        mRealmConfiguration = builder.realmConfiguration;

        // TODO how do we pass around the RealmConfig without exposing it publicly and without having any knowledge of the kits

        mLifecycleManager = new LifecycleManager(builder.application);
        mLifecycleManager.registerCallback(mLifecycleCallback);
//        mLifecycleManager.registerCallback(new LifecycleManager.CallbackLogger(TAG));
//        mLifecycleManager.registerCallback(new LifecycleEventLogger());

        mSettings = new Settings(mContext);

        mDeviceId = mSettings.getClientId();
        mSessionId = mSettings.getSessionId();

        mRequestQueue = new RequestQueue(ShopGun.this, builder.cache, builder.network);
        mRequestQueue.start();

        mLocation = mSettings.getLocation();

        // Session manager implicitly requires Settings
        mSessionManager = new SessionManager(ShopGun.this);

        DatabaseWrapper db = DatabaseWrapper.getInstance(ShopGun.this);
        mListManager = new ListManager(ShopGun.this, db);
        mSyncManager = new SyncManager(ShopGun.this, db);

    }

    /**
     * Singleton access to a {@link ShopGun} object.
     * <p>To build and customize an instance of {@link ShopGun}, please refer to {@link ShopGun.Builder}.</p>
     * @return The {@link ShopGun} instance
     */
    public static ShopGun getInstance() {
        if(mSingleton == null) {
            throw new IllegalStateException("No ShopGun instance found, see ShopGun.Builder.");
        } else {
            return mSingleton;
        }
    }

    /**
     * Check if the instance have been instantiated.
     * <p>To build and customize an instance of {@link ShopGun}, please refer to {@link ShopGun.Builder}.</p>
     * @return {@code true} if ShopGun is instantiated, else {@code false}
     */
    public static boolean isInstantiated() {
        return mSingleton != null;
    }

    public OkHttpClient getClient() {
        return mClient;
    }

    public LifecycleManager getLifecycleManager() {
        return mLifecycleManager;
    }

    /**
     * This returns a pool of threads for executing various SDK tasks on a thread.
     * @return An {@link ExecutorService}
     */
    public ExecutorService getExecutor() {
        return mExecutor;
    }

    /**
     * The {@link Context}, {@link ShopGun} has been instantiated with
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
    public synchronized String getApiKey() {
        Bundle b = PackageUtils.getMetaData(mContext);
        if (isDevelop() && b.containsKey(Constants.META_DEVELOP_API_KEY)) {
            return b.getString(Constants.META_DEVELOP_API_KEY);
        } else {
            return b.getString(Constants.META_API_KEY);
        }
    }

    /**
     * Returns the API secret found at http://etilbudsavis.dk/api/.
     * @return API secret as String
     */
    public synchronized String getApiSecret() {
        Bundle b = PackageUtils.getMetaData(mContext);
        if (isDevelop() && b.containsKey(Constants.META_DEVELOP_API_SECRET)) {
            return b.getString(Constants.META_DEVELOP_API_SECRET);
        } else {
            return b.getString(Constants.META_API_SECRET);
        }
    }

    /**     * Returns the current {@link Environment} in use.
     *
     * @return The current {@link Environment}
     */
    public Environment getEnvironment() {
        return mEnvironment;
    }

    /**
     * Returns the current {@link ThemeEnvironment} in use.
     *
     * @return The current {@link Environment}
     */
    public ThemeEnvironment getThemeEnvironment() {
        return mThemeEnvironment;
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
     * Get the settings, that ETA SDK is using.
     * @return current settings
     */
    public Settings getSettings() {
        return mSettings;
    }

    /**
     * A location object used by ETA, when making API requests.
     * This object should be edited when ever you want to change location.
     * @return A location object
     */
    public SgnLocation getLocation() {
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
     * Indicates whether the SDK is in develop state, and is using development keys.
     * @return true is in develop state, else false
     */
    public boolean isDevelop() {
        return mDevelop;
    }

    /**
     * Clears all preferences that the SDK has created.
     *
     * <p>This includes invalidating the current session and user, clearing all
     * rows in DB, clearing preferences, resetting location info, clearing API
     * cache, e.t.c.</p>
     */
    public void clear() {
        if (!mLifecycleManager.isActive()) {
            mSessionManager.invalidate();
            mSettings.clear();
            mLocation.clear();
            mRequestQueue.clear();
            mListManager.clear();
        }
    }

    public Realm getRealmInstance() {
        return Realm.getInstance(mRealmConfiguration);
    }

    /**
     * API for creating ShopGun instance.
     */
    @SuppressWarnings("unused")
    public static class Builder {

        final Application application;

        ExecutorService executorService;
        Cache cache;
        Network network;
        Boolean develop;
        Environment environment;
        ThemeEnvironment themeEnvironment;
        RealmConfiguration realmConfiguration;
        OkHttpClient okHttpClient;
        List<Interceptor> interceptors = new ArrayList<>();
        List<Interceptor> networkInterceptors = new ArrayList<>();

        /**
         * Start building your {@link ShopGun} instance.
         * @param application The application
         */
        public Builder(Application application) {
            if (application == null) {
                throw new IllegalArgumentException("Context must not be null.");
            }
            this.application = application;
        }

        public Builder addInterceptor(Interceptor interceptor) {
            interceptors.add(interceptor);
            return this;
        }

        public Builder addNetworkInterceptor(Interceptor interceptor) {
            networkInterceptors.add(interceptor);
            return this;
        }

        /**
         * Specify the memory cache for the shopgun-requests.
         * @param cache A {@link Cache}
         * @return This object
         */
        public Builder setCache(Cache cache) {
            if (cache == null) {
                throw new IllegalArgumentException("Cache must not be null.");
            }
            if (this.cache != null) {
                throw new IllegalStateException("Cache already set.");
            }
            this.cache = cache;
            return this;
        }

        /**
         * Specify the {@link Network} to use for requests performed.
         * @param network A network
         * @return This object
         */
        public Builder setNetwork(Network network) {
            if (network == null) {
                throw new IllegalArgumentException("Network must not be null.");
            }
            if (this.network != null) {
                throw new IllegalStateException("Network already set.");
            }
            this.network = network;
            return this;
        }

        /**
         * Specify the {@link ExecutorService} for background tasks
         * @param executorService A ExecutorService
         * @return This object
         */
        public Builder setExecutorService(ExecutorService executorService) {
            if (executorService == null) {
                throw new IllegalArgumentException("ExecutorService must not be null.");
            }
            if (this.executorService != null) {
                throw new IllegalStateException("ExecutorService already set.");
            }
            this.executorService = executorService;
            return this;
        }

        /**
         * Enable development keys, for the ShopGun API, among other features.
         * @param develop {@code true} for development mode, else {@code false}
         * @return This object
         */
        public Builder setDevelop(boolean develop) {
            if (this.develop != null) {
                throw new IllegalStateException("Develop already set.");
            }
            this.develop = develop;
            return this;
        }

        /**
         * Specify the {@link Environment} to use for requesting content from ShopGun API.
         * <p><b>Please be careful</b> when specifying environments, you don't want to end up with a
         * production app querying the wrong environment, that might break everything for you!</p>
         * @param environment An environment
         * @return This object
         */
        public Builder setEnvironment(Environment environment) {
            if (environment == null) {
                throw new IllegalArgumentException("Environment must not be null.");
            }
            if (this.environment != null) {
                throw new IllegalStateException("Environment already set.");
            }
            if (BuildConfig.DEBUG && environment != Environment.PRODUCTION) {
                Log.w(TAG, "A production build not using Environment.PRODUCTION might cause trouble!");
            }
            this.environment = environment;
            return this;
        }

        /**
         * Specify the {@link ThemeEnvironment} to use for requesting themes from ShopGun API.
         * <p><b>Please be careful</b> when specifying environments, you don't want to end up with a
         * production app querying the wrong environment, that might break everything for you!</p>
         * @param themeEnvironment An environment
         * @return This object
         */
        public Builder setThemeEnvironment(ThemeEnvironment themeEnvironment) {
            if (themeEnvironment == null) {
                throw new IllegalArgumentException("ThemeEnvironment must not be null.");
            }
            if (this.themeEnvironment != null) {
                throw new IllegalStateException("ThemeEnvironment already set.");
            }
            if (BuildConfig.DEBUG && themeEnvironment != ThemeEnvironment.PRODUCTION) {
                Log.w(TAG, "A production build not using ThemeEnvironment.PRODUCTION might cause trouble!");
            }
            this.themeEnvironment = themeEnvironment;
            return this;
        }

        /**
         * Builds and set the ShopGun instance, and sets it to be the global singleton.
         * @return The ShopGun instance
         */
        public synchronized ShopGun setInstance() {

            if (ShopGun.mSingleton != null) {
                SgnLog.d(TAG, "ShopGun instance already build and set.");
                return ShopGun.mSingleton;
            }

            if (executorService == null) {
                executorService = Executors.newFixedThreadPool(3, new SgnThreadFactory());
            }

            if (cache == null) {
                cache = new MemoryCache();
            }

            if (network == null) {
                network = new NetworkImpl(new HttpURLNetwork(new DefaultRedirectProtocol()));
            }

            if (develop == null) {
                develop = false;
            }

            if (environment == null) {
                environment = Environment.PRODUCTION;
            }

            if (themeEnvironment == null) {
                themeEnvironment = ThemeEnvironment.PRODUCTION;
            }

            // Setup the default OkHttpClient
            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
            for (Interceptor i : interceptors) {
                okHttpClientBuilder.addInterceptor(i);
            }
            for (Interceptor i : networkInterceptors) {
                okHttpClientBuilder.addNetworkInterceptor(i);
            }
            // Add sdk interceptors last to override user options if necessary
            okHttpClientBuilder.addInterceptor(new UserAgentInterceptor(SgnUserAgent.getUserAgent(application)));
            okHttpClient = okHttpClientBuilder.build();

            // Set the default RealmConfiguration.
            Realm.init(application);
            realmConfiguration = new RealmConfiguration.Builder()
                    .name(Constants.PACKAGE + ".realm")
                    .modules(new SgnRealmModule())
                    .schemaVersion(1)
                    .build();

            ShopGun.mSingleton = new ShopGun(Builder.this);
            return ShopGun.getInstance();

        }

    }

    private static List<OnInstanceCreationListener> mOnInstanceCreationListeners = new ArrayList<>();

    public interface OnInstanceCreationListener {
        void onInstanceCreated();
    }

    public static void registerOnInstanceCreationListener(OnInstanceCreationListener listener) {
        if (ShopGun.isInstantiated()) {
            throw new IllegalStateException("ShopGun instance already created");
        }
        mOnInstanceCreationListeners.add(listener);
    }

    private static void dispatchInstanceCreated() {
        OnInstanceCreationListener[] listeners = null;
        int size = mOnInstanceCreationListeners.size();
        if (size > 0) {
            listeners = mOnInstanceCreationListeners.toArray(new OnInstanceCreationListener[size]);
        }
        if (listeners != null) {
            for (OnInstanceCreationListener listener : listeners) {
                listener.onInstanceCreated();
            }
        }
    }

}
