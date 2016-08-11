package com.shopgun.android.sdk.demo;

import android.app.Application;
import android.util.Log;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.utils.log.L;
import com.shopgun.android.utils.log.LogCatLogger;

import okhttp3.logging.HttpLoggingInterceptor;

public class DemoApplication extends Application {

    public static final String TAG = DemoApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        L.setLogger(new LogCatLogger(Log.VERBOSE));
        SgnLog.setLogger(new LogCatLogger(Log.VERBOSE));

        new ShopGun.Builder(this)
                .addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .setInstance();

    }

    public static ShopGun getShopGun() {
        return ShopGun.getInstance();
    }

}
