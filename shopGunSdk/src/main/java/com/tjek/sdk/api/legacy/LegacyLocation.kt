package com.tjek.sdk.api.legacy

import com.tjek.sdk.TjekLogCat
import com.tjek.sdk.eventstracker.timestamp
import org.json.JSONObject

// Data class to read the location info from the old SharedPreferences file.
// Location information won't be stored by the sdk anymore,
// so this gives a way for the host app to retrieve this info in case they used the sdk to store it.
data class LegacyLocation(
    val isLocationEnabled: Boolean,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Int,
    val sensor: Boolean,
    val time: Long,
    val accuracy: Double
) {
    companion object {
        fun fromLegacySettings(oldStoredLocation: String, isLocationEnabled: Boolean): LegacyLocation? {
            try {
                val json = JSONObject(oldStoredLocation)
                return LegacyLocation(
                    isLocationEnabled = isLocationEnabled,
                    address = json.optString("address", ""),
                    latitude = json.optDouble("r_lat", 0.0),
                    longitude = json.optDouble("r_lng", 0.0),
                    radius = json.optInt("r_radius", 700000),
                    sensor = json.optBoolean("r_sensor", false),
                    time = json.optLong("time", timestamp()),
                    accuracy = json.optDouble("accuracy", 0.0)
                )
            } catch (e: Exception) {
                TjekLogCat.printStackTrace(e)
            }
            return null
        }
    }
}
