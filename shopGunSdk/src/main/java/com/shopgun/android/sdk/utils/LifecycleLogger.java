package com.shopgun.android.sdk.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.shopgun.android.sdk.log.SgnLog;

public class LifecycleLogger implements Application.ActivityLifecycleCallbacks {

    private String mTag;

    public LifecycleLogger(String tag) {
        mTag = tag;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        SgnLog.d(mTag, "ActivityLifecycleCallbacks.onActivityCreated: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityStarted(Activity activity) {
        SgnLog.d(mTag, "ActivityLifecycleCallbacks.onActivityStarted: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        SgnLog.d(mTag, "ActivityLifecycleCallbacks.onActivityResumed: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        SgnLog.d(mTag, "ActivityLifecycleCallbacks.onActivityPaused: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        SgnLog.d(mTag, "ActivityLifecycleCallbacks.onActivityStopped: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        SgnLog.d(mTag, "ActivityLifecycleCallbacks.onActivitySaveInstanceState: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        SgnLog.d(mTag, "ActivityLifecycleCallbacks.onActivityDestroyed: " + activity.getClass().getSimpleName());
    }
}
