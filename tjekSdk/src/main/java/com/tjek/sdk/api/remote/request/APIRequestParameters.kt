package com.tjek.sdk.api.remote.request
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
import androidx.annotation.Keep
import com.tjek.sdk.api.models.Coordinate

/**
 * When using location as a parameter in an APIRequest, this defines the position and optional search radius.
 * maxRadius is in meters.
 */
data class LocationQuery(
    val coordinate: Coordinate,
    val maxRadius: Int? = null
) {
    fun v2RequestParams(): Map<String, String> {
        val params = HashMap<String, String>()
        params["r_lat"] = coordinate.latitude.toString()
        params["r_lng"] = coordinate.longitude.toString()
        maxRadius?.let { params["r_radius"] = it.toString() }
        return params
    }
}

@Keep
enum class StoresRequestSortOrder(val key: String) {
    Nearest("distance"),
    BusinessNameAZ("dealer")
}

data class PaginatedRequestV2(
    val startCursor: Int,
    val itemCount: Int
) {
    companion object {
        fun firstPage(count: Int = 24): PaginatedRequestV2 {
            return PaginatedRequestV2(startCursor = 0, itemCount = count)
        }
    }

    fun v2RequestParams(): Map<String, String> {
        val params = HashMap<String, String>()
        params["offset"] = startCursor.toString()
        params["limit"] = itemCount.toString()
        return params
    }

    fun nextPage(lastCursor: String): PaginatedRequestV2 {
        return PaginatedRequestV2(lastCursor.toIntOrNull() ?: startCursor, itemCount)
    }

}