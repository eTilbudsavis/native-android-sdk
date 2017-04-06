package com.shopgun.android.sdk.corekit.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.shopgun.android.sdk.log.SgnLog;

public class ActivityLifecycleCallbacksLogger implements Application.ActivityLifecycleCallbacks {

    private String mTag;

    public ActivityLifecycleCallbacksLogger(String tag) {
        mTag = tag;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        SgnLog.d(mTag, "onActivityCreated: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityStarted(Activity activity) {
        SgnLog.d(mTag, "onActivityStarted: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityResumed(Activity activity) {
        SgnLog.d(mTag, "onActivityResumed: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityPaused(Activity activity) {
        SgnLog.d(mTag, "onActivityPaused: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityStopped(Activity activity) {
        SgnLog.d(mTag, "onActivityStopped: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        SgnLog.d(mTag, "onActivitySaveInstanceState: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        SgnLog.d(mTag, "onActivityDestroyed: " + activity.getClass().getSimpleName());
    }
}
