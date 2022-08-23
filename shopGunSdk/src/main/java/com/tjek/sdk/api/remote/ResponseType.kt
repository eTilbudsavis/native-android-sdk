package com.tjek.sdk.api.remote

import com.tjek.sdk.api.remote.request.PaginatedRequestV2

sealed class ResponseType<out T : Any> {

    class Success<out T : Any>(val data: T) : ResponseType<T>()

    class Error(val code: Int? = null, val message: String?) : ResponseType<Nothing>() {
        override fun toString(): String {
            return "Error with code = $code. $message"
        }
    }
}

data class PaginatedResponse<T>(
    val results: T,
    val pageInfo: PageInfo
) {
    companion object {
        fun <T> v2PaginatedResponse(request: PaginatedRequestV2, response: List<T>): PaginatedResponse<List<T>> {
            return if (response.isEmpty()) {
                PaginatedResponse(response, PageInfo(request.startCursor.toString(), false))
            } else {
                PaginatedResponse(response, PageInfo(
                    lastCursor = (request.startCursor + response.size).toString(),
                    hasNextPage = response.size == request.itemCount))
            }
        }
    }
}

data class PageInfo(
    val lastCursor: String,
    val hasNextPage: Boolean
)