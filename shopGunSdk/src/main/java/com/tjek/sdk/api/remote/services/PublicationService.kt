package com.tjek.sdk.api.remote.services

import com.tjek.sdk.api.Id
import com.tjek.sdk.api.remote.models.v2.PublicationV2
import com.tjek.sdk.api.remote.models.v2.PublicationV2Decodable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface PublicationService {

    @GET("v2/catalogs/{catalogId}")
    suspend fun getCatalog(@Path("catalogId") catalogId: Id): Response<PublicationV2Decodable>

    @GET("v2/catalogs")
    suspend fun getCatalogs(): Response<List<PublicationV2Decodable>>
}