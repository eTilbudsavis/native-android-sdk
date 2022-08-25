package com.tjek.sdk.eventstracker.api
/*
 * Copyright (C) 2022 Tjek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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