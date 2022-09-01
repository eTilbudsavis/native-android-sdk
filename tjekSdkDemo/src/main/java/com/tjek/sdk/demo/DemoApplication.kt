package com.tjek.sdk.demo

import android.app.Application
import com.tjek.sdk.TjekSDK
import com.tjek.sdk.api.remote.EndpointEnvironment
import com.tjek.sdk.api.remote.NetworkLogLevel
import com.tjek.sdk.eventstracker.api.EventEnvironment

class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        TjekSDK.configure(
            enableLogCatMessages = true,
            networkLogLevel = NetworkLogLevel.Basic,
            endpointEnvironment = EndpointEnvironment.STAGING,
            eventEnvironment = EventEnvironment.STAGING
        )
//        TjekSDK.setApplicationTrackId("this_is_a_test")
    }
}