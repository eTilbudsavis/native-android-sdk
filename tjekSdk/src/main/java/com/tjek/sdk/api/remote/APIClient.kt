package com.tjek.sdk.api.remote
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
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.os.LocaleListCompat
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.EnumJsonAdapter
import com.tjek.sdk.*
import com.tjek.sdk.TjekLogCat
import com.tjek.sdk.api.models.PublicationType
import com.tjek.sdk.api.models.QuantityUnit
import com.tjek.sdk.api.models.QuantityUnitAdapter
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okio.ByteString
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.*

private const val CLIENT_HEADER = "X-Client-Version"
private const val API_KEY_HEADER = "X-Api-Key"
private const val LANGUAGE_HEADER = "accept-language"
private const val CONTENT_TYPE_HEADER = "content-type"
private const val USER_AGENT_HEADER = "user-agent"

internal object APIClient {

    var environment: EndpointEnvironment = EndpointEnvironment.PRODUCTION
    var logLevel: NetworkLogLevel = NetworkLogLevel.None

    private var apiKey: String = ""
    private var userAgent: String = ""
    private var clientVersion: String = ""

    private val languageTags = LocaleListCompat.getAdjustedDefault().toLanguageTags()

    fun setApiKey(context: Context) {
        // get app key from manifest
        val packageName = context.packageName
        val metaData = context.packageManager.getApplicationInfo(
            packageName,
            PackageManager.GET_META_DATA
        ).metaData
        val key = when {
            metaData == null -> null
            TjekSDK.isDevBuild && metaData.containsKey(META_DEVELOP_API_KEY) -> metaData.getString(META_DEVELOP_API_KEY)
            else -> metaData.getString(META_API_KEY)
        }
        if (key == null) {
            TjekLogCat.w("api key not found in the manifest.")
        } else {
            apiKey = key
        }
    }

    fun setApiKey(key: String) {
        apiKey = key
    }

    fun setClientVersion(context: Context) {
        val sdkVersion = BuildConfig.TJEK_SDK_VERSION
        val appPackageName = context.packageName
        val appVersion = context.packageManager.getPackageInfo(appPackageName, 0).versionName ?: "unknown"
        userAgent = String.format(Locale.ENGLISH, USER_AGENT, sdkVersion, appPackageName, appVersion)
        clientVersion = appVersion
    }

    private fun getLoggingInterceptor(logLevel: NetworkLogLevel): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().setLevel(when(logLevel) {
            NetworkLogLevel.None -> HttpLoggingInterceptor.Level.NONE
            NetworkLogLevel.Basic -> HttpLoggingInterceptor.Level.BASIC
            NetworkLogLevel.Full -> HttpLoggingInterceptor.Level.BODY
        })
    }

    // missing "accept-encoding": "gzip"
    private fun getHeaderInterceptor(apiKey: String): Interceptor {
        return Interceptor { chain ->
            val request =
                chain.request().newBuilder()
                    .header(CONTENT_TYPE_HEADER, "\"application/json; charset=utf-8\"")
                    .header(LANGUAGE_HEADER, languageTags)
                    .header(API_KEY_HEADER, apiKey)
                    .header(USER_AGENT_HEADER, userAgent)
                    .header(CLIENT_HEADER, clientVersion)
                    .build()
            chain.proceed(request)
        }
    }

    fun getClient(): Retrofit {

        val mOkHttpClient = OkHttpClient
            .Builder()
            .addInterceptor(getHeaderInterceptor(apiKey))
            .addInterceptor(getLoggingInterceptor(logLevel))
            .build()

        val moshi = Moshi.Builder()
            .add(PublicationType::class.java, EnumJsonAdapter.create(PublicationType::class.java).withUnknownFallback(PublicationType.paged))
            .add(ByteString::class.java, RawJson::class.java, RawJsonAdapter())
            .add(QuantityUnit::class.java, QuantityUnitAdapter())
            .build()

        return Retrofit.Builder()
            .baseUrl("https://${environment.host}/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(mOkHttpClient)
            .build()
    }

}