package com.shopgun.android.sdk.utils;

import android.content.Context;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.utils.PackageUtils;

import java.util.Locale;

public class SgnUserAgent {

    private static final String USER_AGENT = "com.shopgun.android.sdk/%s (%s/%s)";
    private static String mUserAgent;

    public static String getUserAgent(Context ctx) {
        if (mUserAgent == null) {
            synchronized (SgnUserAgent.class) {
                if (mUserAgent == null) {
                    mUserAgent = String.format(Locale.US, USER_AGENT,
                            ShopGun.VERSION.getName(), ctx.getPackageName(), PackageUtils.getVersionName(ctx));
                }
            }
        }
        return mUserAgent;
    }

}
