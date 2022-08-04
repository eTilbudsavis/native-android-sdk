package com.tjek.sdk.eventstracker

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import com.fonfon.geohash.GeoHash
import com.shopgun.android.sdk.BuildConfig
import com.tjek.sdk.*
import java.util.concurrent.TimeUnit

data class EventLocation(
    val geoHash: String,
    val timestamp: Long,
)

object TjekEventsTracker {
    private const val GEO_HASH_PRECISION = 4

    private val trackIdLock = Any()
    var trackId: String = ""
        set(value) = synchronized(trackIdLock) { field = value}
        get() = synchronized(trackIdLock) { field }

    private val locationLock = Any()
    var location: EventLocation? = null
    private set (value) = synchronized(locationLock) { field = value }
    get() = synchronized(locationLock) { field }

    /**
        Updates the `location` property, using a lat/lng/timestamp to generate the geohash (to an accuracy of ±20km).
        This geohash will be included in all **future** tracked events, until `clearLocation()` is called.

        Note: It is up to the user of the SDK to decide how this location information is collected.
        We recommend, however, that only GPS-sourced location data is used.

        - location: the location given by the system.
        The accuracy has to be less than 2km or it won't be accepted
     */
    fun setLocation(location: Location) {
        if (location.accuracy > 2000) return
        this.location = EventLocation(
            geoHash = GeoHash.fromLocation(location, GEO_HASH_PRECISION).toString(),
            timestamp = TimeUnit.MILLISECONDS.toSeconds(location.time))
    }

    /**
     * Clear the location and it won't be added to future events.
     */
    fun clearLocation() {
        location = null
    }

    fun setTrackId(context: Context) {
        // get trackId from manifest
        val packageName = context.packageName
        val metaData = context.packageManager.getApplicationInfo(
            packageName,
            PackageManager.GET_META_DATA
        ).metaData
        val id = when {
            metaData == null -> null
            BuildConfig.DEBUG && metaData.containsKey(META_APPLICATION_TRACK_ID_DEBUG) -> metaData.getString(META_APPLICATION_TRACK_ID_DEBUG)
            else -> metaData.getString(META_APPLICATION_TRACK_ID)
        }
        id?.let { trackId = it }
    }
}