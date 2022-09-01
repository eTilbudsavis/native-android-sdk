package com.tjek.sdk.legacy
/*
 * Copyright (C) 2022 Tjek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
