package com.tjek.sdk.api.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.EnumJsonAdapter
import com.tjek.sdk.api.remote.models.ColorAdapter
import com.tjek.sdk.api.remote.models.v2.PublicationTypesV2
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

private const val CLIENT_VERSION = "X-Client-Version"
private const val API_KEY = "X-Api-Key"
private const val API_SECRET = "X-Api-Secret"
private const val LANGUAGE = "Accept-Language"
private const val TOKEN = "X-Token"

internal object RetrofitClient {

    private fun getHeaderInterceptor():Interceptor{
        return Interceptor { chain ->
            val request =
                chain.request().newBuilder()
                    .header(API_KEY, "00ibm8b65trmxkmo6fy42nr5hyesj8o9")
                    .header(API_SECRET, "00ibm8b65t6vgrgdjrel73okq0yxdb2a")
                    .build()
            chain.proceed(request)
        }
    }

    fun getClient(): Retrofit {
        val mHttpLoggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)

        val mOkHttpClient = OkHttpClient
            .Builder()
            .addInterceptor(mHttpLoggingInterceptor)
            .addInterceptor(getHeaderInterceptor())
            .build()

        val moshi = Moshi.Builder()
            .add(ColorAdapter())
            .add(PublicationTypesV2::class.java, EnumJsonAdapter.create(PublicationTypesV2::class.java).withUnknownFallback(PublicationTypesV2.paged))
            .build()

        // todo: environments
        return Retrofit.Builder()
            .baseUrl("https://squid-api.tjek-staging.com")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(mOkHttpClient)
            .build()
    }

}