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