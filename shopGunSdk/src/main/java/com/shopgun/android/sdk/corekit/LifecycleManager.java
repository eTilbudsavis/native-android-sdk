package com.shopgun.android.sdk.corekit;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.utils.Constants;

import java.util.Collection;
import java.util.HashSet;

public class LifecycleManager {

    public static final String TAG = Constants.getTag(LifecycleManager.class);

    private final Collection<Callback> mCallbacks = new HashSet<>();
    private Application mApplication;
    private Activity mCurrentActivity;

    public LifecycleManager(Application application) {
        mApplication = application;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//            mApplication.registerActivityLifecycleCallbacks(new LifecycleLogger(TAG));
            mApplication.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    if (mCurrentActivity == null) {
                        mCurrentActivity = activity;
                        dispatchCreate(mCurrentActivity);
                    }
                }

                @Override
                public void onActivityStarted(Activity activity) {
                    mCurrentActivity = activity;
                    dispatchStart(activity);
                }

                @Override
                public void onActivityResumed(Activity activity) {
                    mCurrentActivity = activity;
                }

                @Override
                public void onActivityPaused(Activity activity) {
                    throwIfNoActivity();
                }

                @Override
                public void onActivityStopped(Activity activity) {
                    dispatchStop(activity);
                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }

                @Override
                public void onActivityDestroyed(Activity activity) {
                    if (activity == mCurrentActivity) {
                        dispatchDestroy(activity);
                        mCurrentActivity = null;
                        unregisterAllCallbacks();
                    }
                }

                private void throwIfNoActivity() {
                    if (mCurrentActivity == null) {
                        throw new IllegalStateException("No activity set in " + TAG +
                                ". Make sure to instantiate ShopGun in Application.onCreate()");
                    }
                }

            });
        }
    }

    public boolean isActive() {
        return mCurrentActivity != null;
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }

    public Application getApplication() {
        return mApplication;
    }

    public boolean registerCallback(Callback callback) {
        synchronized (mCallbacks) {
            return mCallbacks.add(callback);
        }
    }

    public void unregisterCallback(Callback callback) {
        synchronized (mCallbacks) {
            mCallbacks.remove(callback);
        }
    }

    public void unregisterAllCallbacks() {
        synchronized (mCallbacks) {
            mCallbacks.clear();
        }
    }

    private void dispatchCreate(Activity activity) {
        Callback[] callbacks = collectCallbacks();
        if (callbacks != null) {
            for (Callback callback : callbacks) {
                callback.onCreate();
            }
        }
    }

    private void dispatchStart(Activity activity) {
        Callback[] callbacks = collectCallbacks();
        if (callbacks != null) {
            for (Callback callback : callbacks) {
                callback.onStart();
            }
        }
    }

    private void dispatchStop(Activity activity) {
        Callback[] callbacks = collectCallbacks();
        if (callbacks != null) {
            for (Callback callback : callbacks) {
                callback.onStop();
            }
        }
    }

    private void dispatchDestroy(Activity activity) {
        Callback[] callbacks = collectCallbacks();
        if (callbacks != null) {
            for (Callback callback : callbacks) {
                callback.onDestroy();
            }
        }
    }

    private Callback[] collectCallbacks() {
        Callback[] callbacks = null;
        synchronized (mCallbacks) {
            if (mCallbacks.size() > 0) {
                callbacks = mCallbacks.toArray(new Callback[mCallbacks.size()]);
            }
        }
        return callbacks;
    }

    /**
     * Lifecycle for making ShopGun SDK lifecycle aware.
     */
    public interface Callback {

        /**
         * Called when the first activity is created.
         */
        void onCreate();

        /**
        * Called when the first activity starts
        */
        void onStart();

        /**
         * Called when the last activity stops
         */
        void onStop();

        /**
         * Called when the last activity is destroyed.
         * After this, {@link ShopGun} will be inactive and throw exceptions
         */
        void onDestroy();
    }

    public static class CallbackLogger implements Callback {

        final String mTag;

        public CallbackLogger(String tag) {
            mTag = tag;
        }

        @Override
        public void onCreate() {
            log("onCreate");
        }

        @Override
        public void onStart() {
            log("onStart");
        }

        @Override
        public void onStop() {
            log("onStop");
        }

        @Override
        public void onDestroy() {
            log("onDestroy");
        }

        private void log(String event) {
            SgnLog.d(mTag, event + ": " + ShopGun.getInstance().getLifecycleManager().getCurrentActivity().getClass().getSimpleName());
        }

    }
}
