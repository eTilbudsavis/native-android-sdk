package com.tjek.sdk.api.remote.services

import com.tjek.sdk.api.Id
import com.tjek.sdk.api.remote.models.v2.ImageUrlsV2
import com.tjek.sdk.api.remote.models.v2.PublicationV2Decodable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface PublicationService {

    @GET("v2/catalogs/{catalogId}")
    suspend fun getCatalog(@Path("catalogId") catalogId: Id): Response<PublicationV2Decodable>

    @GET("v2/catalogs")
    suspend fun getCatalogs(@QueryMap queryParams: Map<String, String>): Response<List<PublicationV2Decodable>>

    @GET("v2/catalogs/{catalogId}/pages")
    suspend fun getCatalogPages(@Path("catalogId") catalogId: Id): Response<List<ImageUrlsV2>>
}