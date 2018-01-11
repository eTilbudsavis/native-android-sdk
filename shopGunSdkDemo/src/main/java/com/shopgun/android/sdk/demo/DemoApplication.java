package com.shopgun.android.sdk.demo;

import android.app.Application;
import android.util.Log;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.corekit.ApiV2Interceptor;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.utils.log.L;
import com.shopgun.android.utils.log.LogCatLogger;
import com.shopgun.android.utils.log.Logger;

import okhttp3.logging.HttpLoggingInterceptor;

public class DemoApplication extends Application {

    public static final String TAG = DemoApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        Logger logger = new LogCatLogger(Log.VERBOSE);
        L.setLogger(logger);
        SgnLog.setLogger(logger);

        new ShopGun.Builder(this)
                .addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .addInterceptor(new ApiV2Interceptor())
                .setInstance();

    }

    public static ShopGun getShopGun() {
        return ShopGun.getInstance();
    }

}
