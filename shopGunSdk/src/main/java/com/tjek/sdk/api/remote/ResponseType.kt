package com.tjek.sdk.api.remote

sealed class ResponseType<T>(
    val data: T? = null,
    val message: String? = null
) {

    class Success<T>(data: T) : ResponseType<T>(data)

    class Error<T>(message: String, data: T? = null) : ResponseType<T>(data, message)

}