package com.tjek.sdk.eventstracker.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.EnumJsonAdapter
import com.tjek.sdk.api.remote.NetworkLogLevel
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val CONTENT_TYPE_HEADER = "Content-Type"
private const val ACCEPT_HEADER = "Accept"

enum class EventEnvironment(val host: String) {
    PRODUCTION("wolf-api.tjek.com"),
    STAGING("wolf-api.tjek-staging.com")
}

internal object EventClient {

    var environment: EventEnvironment = EventEnvironment.PRODUCTION
    var logLevel: NetworkLogLevel = NetworkLogLevel.None

    private fun getLoggingInterceptor(logLevel: NetworkLogLevel): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().setLevel(when(logLevel) {
            NetworkLogLevel.None -> HttpLoggingInterceptor.Level.NONE
            NetworkLogLevel.Basic -> HttpLoggingInterceptor.Level.BASIC
            NetworkLogLevel.Full -> HttpLoggingInterceptor.Level.BODY
        })
    }

    private fun getHeaderInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request =
                chain.request().newBuilder()
                    .header(CONTENT_TYPE_HEADER, "\"application/json\"")
                    .header(ACCEPT_HEADER, "\"application/json\"")
                    .build()
            chain.proceed(request)
        }
    }

    fun getClient(): Retrofit {

        val mOkHttpClient = OkHttpClient
            .Builder()
            .addInterceptor(getHeaderInterceptor())
            .addInterceptor(getLoggingInterceptor(logLevel))
            .build()

        val moshi = Moshi.Builder()
            .add(EventStatus::class.java, EnumJsonAdapter.create(EventStatus::class.java).withUnknownFallback(EventStatus.unknown))
            .build()

        return Retrofit.Builder()
            .baseUrl("https://${environment.host}/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(mOkHttpClient)
            .build()
    }

}