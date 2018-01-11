package com.shopgun.android.sdk.network.impl;

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.RequestDebugger;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.utils.SgnUtils;
import com.shopgun.android.utils.log.Logger;

public class DeliveryDebugger implements RequestDebugger {

    public static final String TAG = DeliveryDebugger.class.getSimpleName();

    private final String mTag;
    private final Logger mLogger;

    public DeliveryDebugger(String tag) {
        this(SgnLog.getLogger(), tag);
    }

    public DeliveryDebugger(Logger logger, String tag) {
        mLogger = logger;
        mTag = tag;
    }

    @Override
    public void onFinish(Request<?> r) {
        // ignore
    }

    @Override
    public void onDelivery(Request<?> r, Object response, ShopGunError error) {
        mLogger.d(mTag, r.getMethod() + " " + SgnUtils.requestToUrlAndQueryString(r));
        String status = error == null ? "Success" : "Error";
        mLogger.d(mTag, "- " + status + ": " + String.valueOf(response));
    }

}
