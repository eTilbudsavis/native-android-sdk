package com.tjek.sdk.api.remote

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