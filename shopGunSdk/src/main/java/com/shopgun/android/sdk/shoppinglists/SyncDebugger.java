package com.shopgun.android.sdk.shoppinglists;

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.RequestDebugger;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.network.impl.DeliveryDebugger;
import com.shopgun.android.utils.log.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @deprecated No longer maintained
 */
@Deprecated
public class SyncDebugger implements RequestDebugger {

    public static final String TAG = DeliveryDebugger.class.getSimpleName();

    private final String mTag;
    private final Logger mLogger;
    private boolean mSkipSuccess;
    private Set<Request.Method> mSkipMethods = new HashSet<>();

    public SyncDebugger(String tag) {
        this(tag, SgnLog.getLogger());
    }

    public SyncDebugger(String tag, Logger logger) {
        mLogger = logger;
        mTag = tag;
    }

    public SyncDebugger setSkipSuccess(boolean skipSuccess) {
        mSkipSuccess = skipSuccess;
        return this;
    }

    public SyncDebugger setSkipMethods(Request.Method method) {
        mSkipMethods.add(method);
        return this;
    }

    public SyncDebugger setSkipMethods(Request.Method method, Request.Method... methods) {
        mSkipMethods.add(method);
        Collections.addAll(mSkipMethods, method);
        return this;
    }

    @Override
    public void onFinish(Request<?> r) {
        // ignore
    }

    @Override
    public void onDelivery(Request<?> r, Object response, ShopGunError error) {
        if (mSkipSuccess && error == null) {
            return;
        }
        if (mSkipMethods.contains(r.getMethod())) {
            return;
        }
        mLogger.d(mTag, r.getMethod() + " " + r.getUrl());
        if (error == null) {
            mLogger.d(mTag, "- Success: " + String.valueOf(response));
        } else {
            mLogger.d(mTag, "- Error in [" + r.getClass().getSimpleName() + "] " + String.valueOf(error));
        }
    }

}
