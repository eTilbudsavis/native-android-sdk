package com.tjek.sdk

import android.content.Context
import androidx.startup.Initializer
import com.shopgun.android.sdk.BuildConfig
import com.tjek.sdk.api.remote.EndpointEnvironment
import com.tjek.sdk.api.remote.NetworkLogLevel
import com.tjek.sdk.api.remote.APIClient

//todo: change namespace keys
const val META_API_KEY = "com.shopgun.android.sdk.api_key"
const val META_DEVELOP_API_KEY = "com.shopgun.android.sdk.develop.api_key"

object TjekSDK {

    // Initializes the SDK with default settings (auto-initialization)
    fun initialize(context: Context): TjekSDK {
        with(APIClient) {
            setApiKey(context)
            setClientVersion(context)
        }
        return this
    }

    fun configure(
        enableLogCatMessages: Boolean = false,
        networkLogLevel: NetworkLogLevel = NetworkLogLevel.None,
        endpointEnvironment: EndpointEnvironment = EndpointEnvironment.PRODUCTION
    ) {
        if (BuildConfig.DEBUG && enableLogCatMessages)
            TjekLogCat.enableLogging()
        with(APIClient) {
            if (BuildConfig.DEBUG)
                logLevel = networkLogLevel
            environment = endpointEnvironment
        }
    }
}

// Initializer for the App Startup library
class TjekSDKInitializer : Initializer<TjekSDK> {
    override fun create(context: Context): TjekSDK {
        return TjekSDK.initialize(context)
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        // No dependencies on other libraries.
        return emptyList()
    }

}