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
    val start: T,
    val count: Int
) {
    companion object {
        fun firstPage(count: Int = 24): PaginatedRequest<Int> {
            return PaginatedRequest(start = 0, count = count)
        }
    }

    fun v2RequestParams(): Map<String, String> {
        val params = HashMap<String, String>()
        params["offset"] = start.toString()
        params["limit"] = count.toString()
        return params
    }

}