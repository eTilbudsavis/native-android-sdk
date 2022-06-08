package com.tjek.sdk.api.remote

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.os.LocaleListCompat
import com.shopgun.android.sdk.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.EnumJsonAdapter
import com.tjek.sdk.META_API_KEY
import com.tjek.sdk.META_DEVELOP_API_KEY
import com.tjek.sdk.TjekLogCat
import com.tjek.sdk.api.remote.models.ColorAdapter
import com.tjek.sdk.api.remote.models.v2.PublicationTypesV2
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*

private const val CLIENT_HEADER = "X-Client-Version"
private const val API_KEY_HEADER = "X-Api-Key"
private const val LANGUAGE_HEADER = "accept-language"
private const val CONTENT_TYPE_HEADER = "content-type"
private const val USER_AGENT_HEADER = "user-agent"
private const val API_DEPRECATION_HEADER = "X-Api-Deprecation-Info"
private const val API_DEPRECATION_DATE_HEADER = "X-Api-Deprecation-Date"

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
        val key = if (BuildConfig.DEBUG && metaData.containsKey(META_DEVELOP_API_KEY)) {
            metaData.getString(META_DEVELOP_API_KEY)
        } else {
            metaData.getString(META_API_KEY)
        }
        if (key == null) {
            TjekLogCat.forceE("api key not found in the manifest. Tjek sdk won't work properly without it.")
        } else {
            apiKey = key
        }
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
    private fun getV2HeaderInterceptor(apiKey: String): Interceptor {
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

    private fun getV2BaseUrl(environment: EndpointEnvironment): String {
        val builder = Uri.Builder()
            .scheme("https")
            .authority(environment.host)
            .appendPath("v2")
            .appendPath("") // it adds a final "/" needed by Retrofit baseUrl
        return builder.build().toString()
    }

    fun getV2Client(): Retrofit {

        val mOkHttpClient = OkHttpClient
            .Builder()
            .addInterceptor(getV2HeaderInterceptor(apiKey))
            .addInterceptor(getLoggingInterceptor(logLevel))
            .build()

        val moshi = Moshi.Builder()
            .add(ColorAdapter())
            .add(PublicationTypesV2::class.java, EnumJsonAdapter.create(PublicationTypesV2::class.java).withUnknownFallback(PublicationTypesV2.paged))
            .build()

        return Retrofit.Builder()
            .baseUrl("https://${environment.host}/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(mOkHttpClient)
            .build()
    }

}