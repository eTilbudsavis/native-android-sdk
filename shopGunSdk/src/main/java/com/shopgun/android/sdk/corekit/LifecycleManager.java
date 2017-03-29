package com.shopgun.android.sdk.corekit;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.LifecycleLogger;

import java.util.Collection;
import java.util.HashSet;

public class LifecycleManager {

    public static final String TAG = Constants.getTag(LifecycleManager.class);

    private static final int DEF_DESTROY_DELAY = 3000;

    private final Collection<Callback> mCallbacks = new HashSet<>();
    private Application mApplication;
    private Activity mCurrentActivity;
    private Runnable mDestroyRunnable = new Runnable() {
        @Override
        public void run() {
            // Remove current Activity, to indicate that we are no longer active
            Activity tmp = mCurrentActivity;
            mCurrentActivity = null;
            dispatchDestroy(tmp);
            // TODO to remove callbacks or not to remove callbacks, that is the question.
//            unregisterAllCallbacks();
        }
    };

    public LifecycleManager(Application application) {
        mApplication = application;
//        mApplication.registerActivityLifecycleCallbacks(new LifecycleLogger(TAG));
        mApplication.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                ShopGun.getInstance().getHandler().removeCallbacks(mDestroyRunnable);
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
                    ShopGun.getInstance().getHandler().postDelayed(mDestroyRunnable, DEF_DESTROY_DELAY);
                }
            }

            private void throwIfNoActivity() {
                if (mCurrentActivity == null) {
                    throw new IllegalStateException("No activity set in " + TAG +
                            ". Make sure to instantiate ShopGun in Application.onCreate()");
                }
            }

        });
        mApplication.registerComponentCallbacks(new ComponentCallbacks2() {
            @Override
            public void onTrimMemory(int level) {
                dispatchTrimMemory(level);
            }

            @Override
            public void onConfigurationChanged(Configuration newConfig) {
                dispatchConfigurationChanged(newConfig);
            }

            @Override
            public void onLowMemory() {
                onTrimMemory(TRIM_MEMORY_COMPLETE);
            }
        });
    }

    public boolean isActive() {
        return mCurrentActivity != null;
    }

    public Activity getActivity() {
        return mCurrentActivity;
    }

    private Application getApplication() {
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
                callback.onCreate(activity);
            }
        }
    }

    private void dispatchStart(Activity activity) {
        Callback[] callbacks = collectCallbacks();
        if (callbacks != null) {
            for (Callback callback : callbacks) {
                callback.onStart(activity);
            }
        }
    }

    private void dispatchStop(Activity activity) {
        Callback[] callbacks = collectCallbacks();
        if (callbacks != null) {
            for (Callback callback : callbacks) {
                callback.onStop(activity);
            }
        }
    }

    private void dispatchDestroy(Activity activity) {
        Callback[] callbacks = collectCallbacks();
        if (callbacks != null) {
            for (Callback callback : callbacks) {
                callback.onDestroy(activity);
            }
        }
    }

    private void dispatchTrimMemory(int level) {
        Callback[] callbacks = collectCallbacks();
        if (callbacks != null) {
            for (Callback callback : callbacks) {
                callback.onTrimMemory(level);
            }
        }
    }

    private void dispatchConfigurationChanged(Configuration newConfig) {
        Callback[] callbacks = collectCallbacks();
        if (callbacks != null) {
            for (Callback callback : callbacks) {
                callback.onConfigurationChanged(newConfig);
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
        void onCreate(Activity activity);

        /**
        * Called when the first activity starts
        */
        void onStart(Activity activity);

        /**
         * Called when the last activity stops
         */
        void onStop(Activity activity);

        /**
         * Called when the last activity is destroyed.
         * After this, {@link ShopGun} will be inactive and throw exceptions
         */
        void onDestroy(Activity activity);

        /**
         * @see android.content.ComponentCallbacks2#onTrimMemory(int)
         */
        void onTrimMemory(int level);

        /**
         * @see android.content.ComponentCallbacks2#onConfigurationChanged(Configuration)
         */
        void onConfigurationChanged(Configuration newConfig);

    }

    public static class SimpleCallback implements Callback {
        @Override public void onCreate(Activity activity) {}
        @Override public void onStart(Activity activity) {}
        @Override public void onStop(Activity activity) {}
        @Override public void onDestroy(Activity activity) {}
        @Override public void onTrimMemory(int level) {}
        @Override public void onConfigurationChanged(Configuration newConfig) {}
    }

}
