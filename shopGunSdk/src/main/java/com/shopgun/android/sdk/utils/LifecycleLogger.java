package com.shopgun.android.sdk.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

public class LifecycleLogger implements Application.ActivityLifecycleCallbacks {

    private String mTag;

    public LifecycleLogger(String tag) {
        mTag = tag;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.d(mTag, "ActivityLifecycleCallbacks.onActivityCreated: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.d(mTag, "ActivityLifecycleCallbacks.onActivityStarted: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.d(mTag, "ActivityLifecycleCallbacks.onActivityResumed: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.d(mTag, "ActivityLifecycleCallbacks.onActivityPaused: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.d(mTag, "ActivityLifecycleCallbacks.onActivityStopped: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Log.d(mTag, "ActivityLifecycleCallbacks.onActivitySaveInstanceState: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.d(mTag, "ActivityLifecycleCallbacks.onActivityDestroyed: " + activity.getClass().getSimpleName());
    }
}
