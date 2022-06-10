package com.tjek.sdk.api.remote

import retrofit2.Response

internal abstract class APIRequestBase {

    suspend fun <T,V> safeApiCall(decoder: suspend (V) -> T, apiCall: suspend () -> Response<V>): ResponseType<T> {
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                body?.let {
                    return ResponseType.Success(decoder(body))
                }
            }
            return error("${response.code()} ${response.message()}")
        } catch (e: Exception) {
            return error(e.message ?: e.toString())
        }
    }

    private fun <T> error(errorMessage: String): ResponseType<T> =
        ResponseType.Error("Network call failed : $errorMessage")


}