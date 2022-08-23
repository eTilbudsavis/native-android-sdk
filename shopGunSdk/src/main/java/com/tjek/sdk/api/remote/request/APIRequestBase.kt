package com.tjek.sdk.api.remote.request

import com.squareup.moshi.Moshi
import com.tjek.sdk.api.remote.APIError
import com.tjek.sdk.api.remote.ResponseType
import retrofit2.Response

internal abstract class APIRequestBase {

    suspend fun <T : Any ,V> safeApiCall(decoder: suspend (V) -> T, apiCall: suspend () -> Response<V>): ResponseType<T> {
        try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                body?.let {
                    return ResponseType.Success(decoder(body))
                }
            }
            return error(response)
         } catch (e: Exception) {
            return ResponseType.Error(message = e.message ?: e.toString())
        }
    }

    private fun <T : Any,V> error(response: Response<V>): ResponseType<T> {
        when (response.code()) {
            408,    // Request Timeout
            429,    // Too Many Requests
            502,    // Bad Gateway
            503,    // Service Unavailable
            504     // Gateway Timeout
                -> return ResponseType.Error(code = response.code(), message = response.message())
        }

        // If it's none of the above, let's see if it's a known error from the server
        response.errorBody()?.string()?.let {
            try {
                val serverResponse = Moshi.Builder().build().adapter(APIError::class.java).fromJson(it)
                if (serverResponse != null) {
                    return ResponseType.Error(code = serverResponse.code, message = serverResponse.toString())
                }
            } catch (e: Exception) {
                return ResponseType.Error(code = response.code(), message = it)
            }
        }
        return ResponseType.Error(message = "Unknown error occurred")
    }


}