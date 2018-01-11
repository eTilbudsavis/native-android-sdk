package com.shopgun.android.sdk.corekit.utils;

import android.app.Activity;
import android.content.res.Configuration;

import com.shopgun.android.sdk.corekit.LifecycleManager;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.utils.ToStringUtils;

public class LifecycleManagerCallbackLogger implements LifecycleManager.Callback {

    private final String mTag;

    public LifecycleManagerCallbackLogger(String tag) {
        mTag = tag;
    }

    @Override
    public void onCreate(Activity activity) {
        SgnLog.d(mTag, "onCreate: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onStart(Activity activity) {
        SgnLog.d(mTag, "onStart: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onStop(Activity activity) {
        SgnLog.d(mTag, "onStop: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onDestroy(Activity activity) {
        SgnLog.d(mTag, "onDestroy: " + activity.getClass().getSimpleName());
    }

    @Override
    public void onTrimMemory(int level) {
        SgnLog.d(mTag, "onTrimMemory: " + ToStringUtils.onTrimMemoryToString(level));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        SgnLog.d(mTag, "onConfigurationChanged");
    }
}
