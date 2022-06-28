package com.shopgun.android.sdk.demo

import android.app.Application
import com.shopgun.android.sdk.demo.DemoApplication
import com.tjek.sdk.TjekSDK
import com.tjek.sdk.api.remote.EndpointEnvironment

class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        TjekSDK.configure(
            enableLogCatMessages = true,
            endpointEnvironment = EndpointEnvironment.STAGING
        )
    }
}