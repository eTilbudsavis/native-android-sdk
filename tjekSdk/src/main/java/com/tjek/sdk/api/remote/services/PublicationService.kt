package com.tjek.sdk.api.remote.services

import com.tjek.sdk.api.Id
import com.tjek.sdk.api.IncitoData
import com.tjek.sdk.api.models.ImageUrlsV2
import com.tjek.sdk.api.models.PublicationHotspotV2Decodable
import com.tjek.sdk.api.models.PublicationV2Decodable
import com.tjek.sdk.api.remote.request.IncitoAPIQuery
import retrofit2.Response
import retrofit2.http.*

interface PublicationService {

    @GET("v2/catalogs/{catalogId}")
    suspend fun getCatalog(@Path("catalogId") catalogId: Id): Response<PublicationV2Decodable>

    @GET("v2/catalogs")
    suspend fun getCatalogs(@QueryMap queryParams: Map<String, String>): Response<List<PublicationV2Decodable>>

    @GET("v2/catalogs/{catalogId}/pages")
    suspend fun getCatalogPages(@Path("catalogId") catalogId: Id): Response<List<ImageUrlsV2>>

    @GET("v2/catalogs/{catalogId}/hotspots")
    suspend fun getCatalogHotspots(@Path("catalogId") catalogId: Id): Response<List<PublicationHotspotV2Decodable>>

    @POST("v4/rpc/generate_incito_from_publication")
    suspend fun getIncito(@Body incitoAPIQuery: IncitoAPIQuery): Response<IncitoData>
}