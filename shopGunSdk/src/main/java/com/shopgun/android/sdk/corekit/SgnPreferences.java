package com.shopgun.android.sdk.corekit;

import android.content.Context;
import android.content.SharedPreferences;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.utils.SgnUtils;

public class SgnPreferences {

    private static final String PREFS_NAME = Constants.PACKAGE + "_preferences";

    private static final String INSTALLATION_ID = "installation_id";

    private static SgnPreferences mInstance;

    private SharedPreferences mSharedPreferences;

    private SgnPreferences(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static SgnPreferences getInstance() {
        if (mInstance == null) {
            synchronized (SgnPreferences.class) {
                if (mInstance == null) {
                    Context context = ShopGun.getInstance().getContext();
                    mInstance = new SgnPreferences(context);
                }
            }
        }
        return mInstance;
    }

    public String getInstallationId() {
        if (!mSharedPreferences.contains(INSTALLATION_ID)) {
            mSharedPreferences.edit().putString(INSTALLATION_ID, SgnUtils.createUUID()).apply();
        }
        return mSharedPreferences.getString(INSTALLATION_ID, null);
    }

}
