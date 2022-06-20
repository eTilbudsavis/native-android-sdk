package com.tjek.sdk.api.remote

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.tjek.sdk.TjekLogCat
import retrofit2.Response
import java.io.IOException

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
            return error(response)
         } catch (e: IOException) {
            return ResponseType.Error(ErrorType.Network(message = e.message ?: e.toString()))
         } catch(e: JsonDataException) {
            return ResponseType.Error(ErrorType.Parsing(message = e.message ?: e.toString()))
         } catch (e: Exception) {
            return ResponseType.Error(ErrorType.Unknown(message = e.message ?: e.toString()))
        }
    }

    private fun <T,V> error(response: Response<V>): ResponseType<T> {
        when (response.code()) {
            408,    // Request Timeout
            429,    // Too Many Requests
            502,    // Bad Gateway
            503,    // Service Unavailable
            504     // Gateway Timeout
                -> return ResponseType.Error(ErrorType.Network(code = response.code(), message = response.message()))
        }

        // If it's none of the above, let's see if it's a known error from the server
        response.errorBody()?.string()?.let {
            try {
                val serverResponse = Moshi.Builder().build().adapter(ServerResponse::class.java).fromJson(it)
                if (serverResponse != null) {
                    return ResponseType.Error(ErrorType.Api(APIError(serverResponse)))
                }
            } catch (e: Exception) {
                TjekLogCat.printStackTrace(e)
            }
        }
        return ResponseType.Error(ErrorType.Unknown())
    }


}