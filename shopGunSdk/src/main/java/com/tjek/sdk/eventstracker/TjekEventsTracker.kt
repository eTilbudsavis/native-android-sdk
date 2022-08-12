package com.tjek.sdk.eventstracker

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import com.fonfon.geohash.GeoHash
import com.shopgun.android.sdk.BuildConfig
import com.tjek.sdk.*
import com.tjek.sdk.database.TjekRoomDb
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

data class EventLocation(
    val geoHash: String,
    val timestamp: Long,
)

internal object TjekEventsTracker {
    private const val GEO_HASH_PRECISION = 4

    private val shipInterval = TimeUnit.SECONDS.toMillis(60)
    private lateinit var eventShipper: EventShipper
    private lateinit var eventDao: EventDao

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val trackIdLock = Any()
    var trackId: String = ""
        set(value) = synchronized(trackIdLock) { field = value}
        get() = synchronized(trackIdLock) { field }

    private val locationLock = Any()
    var location: EventLocation? = null
    private set (value) = synchronized(locationLock) { field = value }
    get() = synchronized(locationLock) { field }

    fun setLocation(location: Location) {
        if (location.accuracy > 2000) return
        this.location = EventLocation(
            geoHash = GeoHash.fromLocation(location, GEO_HASH_PRECISION).toString(),
            timestamp = TimeUnit.MILLISECONDS.toSeconds(location.time))
    }

    fun clearLocation() {
        location = null
    }

    suspend fun track(event: Event) {
        event.addApplicationTrackId(trackId)
        location?.let { event.addLocation(it.geoHash, it.timestamp) }
        // transform the Event into a shippable one
        eventDao.insert(ShippableEvent(
            id = event.id,
            version = event.version,
            timestamp = event.timestamp,
            jsonEvent = event.toJson()
        ))
    }

    fun initAtStartup(context: Context) {
        setTrackId(context)
        eventDao = TjekRoomDb.getInstance(context).eventDao()
        migrateEventDatabase()
        startShipping()
    }

    private fun startShipping() {
        eventShipper = EventShipper(eventDao)
        coroutineScope.launch {
            while (isActive) {
                eventShipper.shipEvents()
                delay(shipInterval)
            }
        }
    }

    private fun migrateEventDatabase() {
        // todo migrateEventDatabase
    }

    private fun setTrackId(context: Context) {
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