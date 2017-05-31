package com.shopgun.android.sdk.network.impl;

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.RequestDebugger;
import com.shopgun.android.sdk.utils.SgnUtils;

public class DeliveryDebugger implements RequestDebugger {

    public static final String TAG = DeliveryDebugger.class.getSimpleName();

    private final String mTag;

    public DeliveryDebugger(String tag) {
        mTag = tag;
    }

    @Override
    public void onFinish(Request<?> r) {
        // ignore
    }

    @Override
    public void onDelivery(Request<?> r) {
        SgnLog.d(mTag, r.getMethod() + " " + SgnUtils.requestToUrlAndQueryString(r));
    }

}
