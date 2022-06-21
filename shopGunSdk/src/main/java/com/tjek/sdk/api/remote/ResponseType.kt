package com.tjek.sdk.api.remote

sealed class ResponseType<T>(
    val data: T? = null
) {

    class Success<T>(data: T) : ResponseType<T>(data)

    class Error<T>(val errorType: ErrorType) : ResponseType<T>()

}

sealed class ErrorType {
    data class Api(val error: APIError) : ErrorType()
    data class Parsing(val message: String?) : ErrorType()
    data class Network(val code: Int? = null, val message: String?) : ErrorType()
    data class Unknown(val code: Int? = null, val message: String? = null) : ErrorType()
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