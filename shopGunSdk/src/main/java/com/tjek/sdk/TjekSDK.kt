package com.tjek.sdk

import android.content.Context
import android.location.Location
import androidx.startup.Initializer
import com.shopgun.android.sdk.BuildConfig
import com.tjek.sdk.api.remote.EndpointEnvironment
import com.tjek.sdk.api.remote.NetworkLogLevel
import com.tjek.sdk.api.remote.APIClient
import com.tjek.sdk.eventstracker.Event
import com.tjek.sdk.eventstracker.TjekEventsTracker
import com.tjek.sdk.eventstracker.api.EventClient
import com.tjek.sdk.eventstracker.api.EventEnvironment

//todo: change namespace keys
const val META_API_KEY = "com.shopgun.android.sdk.api_key"
const val META_DEVELOP_API_KEY = "com.shopgun.android.sdk.develop.api_key"
const val META_APPLICATION_TRACK_ID = "com.shopgun.android.sdk.eventskit.application_track_id"
const val META_APPLICATION_TRACK_ID_DEBUG = "com.shopgun.android.sdk.develop.eventskit.application_track_id"

object TjekSDK {

    // Initializes the SDK with default settings (auto-initialization)
    fun initialize(context: Context): TjekSDK {
        with(APIClient) {
            setApiKey(context)
            setClientVersion(context)
        }
        TjekPreferences.initAtStartup(context)
        TjekEventsTracker.initAtStartup(context)
        return this
    }

    /**
     * The sdk is auto initialised by the App Startup library.
     * This method allows you to change some settings that will be used byt the sdk.
     * *The default settings should be used for production releases*.
     *
     * Parameters:
     * - enableLogCatMessages (default=false): allow the sdk to print messages in the LogCat. **Only available for debug builds**, so logging if off for release builds
     * - networkLogLevel (default=None): change the log level of the underlying okHttp client. **Only available for debug builds**.
     * - endpointEnvironment (default=Production): environment hit by the TjekAPIs. **Staging is only for development and it can be outdated/unstable**.
     * - eventEnvironment (default=Production): environment used by the event tracker. **Staging is only for development**.
     */
    fun configure(
        enableLogCatMessages: Boolean = false,
        networkLogLevel: NetworkLogLevel = NetworkLogLevel.None,
        endpointEnvironment: EndpointEnvironment = EndpointEnvironment.PRODUCTION,
        eventEnvironment: EventEnvironment = EventEnvironment.PRODUCTION
    ) {
        if (BuildConfig.DEBUG && enableLogCatMessages)
            TjekLogCat.enableLogging()
        with(APIClient) {
            if (BuildConfig.DEBUG)
                logLevel = networkLogLevel
            environment = endpointEnvironment
        }
        with(EventClient) {
            if (BuildConfig.DEBUG)
                logLevel = networkLogLevel
            environment = eventEnvironment
        }
    }

    /**
     * The api key should be set in the manifest, but if you need to set it at runtime, you can use this method.
     * **This has to be called before any request to the TjekAPI is performed**.
     */
    fun setApiKey(key: String) {
        if (key.isNotEmpty()) {
            APIClient.setApiKey(key)
        }
    }

    /**
     * The application track id is added to the events sent to Tjek backend.
     * It should be set in the manifest, but if you need to set it at runtime, you can use this method.
     * It can be called at any time: the new events will have the new id (in case there are old events stored, those will have the old id)
     */
    fun setApplicationTrackId(id: String) {
        if (id.isNotEmpty()) {
            TjekEventsTracker.trackId = id
        }
    }

    /**
    Updates the `location` property of the events, using a lat/lng/timestamp to generate the geohash (to an accuracy of ±20km).
    This geohash will be included in all **future** tracked events, until `clearLocation()` is called.

    Note: It is up to the user of the SDK to decide how this location information is collected.
    We recommend, however, that only GPS-sourced location data is used.

    - location: the location given by the system.
    The accuracy has to be less than 2km or it won't be accepted
     */
    fun setEventsLocation(location: Location) {
        TjekEventsTracker.setLocation(location)
    }

    /**
     * Clear the location and it won't be added to future events.
     */
    fun clearEventsLocation() {
        TjekEventsTracker.clearLocation()
    }

    /**
     * Track events to be sent to Tjek backend for Insight.
     */
    fun trackEvent(event: Event) {
        TjekEventsTracker.track(event)
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