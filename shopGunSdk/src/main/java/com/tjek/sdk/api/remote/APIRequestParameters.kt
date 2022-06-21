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

data class PaginatedRequest<T>(
    val startCursor: T,
    val itemCount: Int
) {
    companion object {
        fun v2FirstPage(count: Int = 24): PaginatedRequest<Int> {
            return PaginatedRequest(startCursor = 0, itemCount = count)
        }

        fun v4FirstPage(count: Int = 24): PaginatedRequest<String?> {
            return PaginatedRequest(startCursor = null, itemCount = count)
        }
    }

    fun v2RequestParams(): Map<String, String> {
        val params = HashMap<String, String>()
        params["offset"] = startCursor.toString()
        params["limit"] = itemCount.toString()
        return params
    }

    fun nextPage(lastCursor: T): PaginatedRequest<T> {
        return PaginatedRequest(lastCursor, itemCount)
    }

}