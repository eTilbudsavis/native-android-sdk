package com.tjek.sdk.api.remote.services

import com.tjek.sdk.api.remote.models.v2.PublicationV2
import retrofit2.Response
import retrofit2.http.GET

interface PublicationService {

    @GET("/catalogs")
    suspend fun getCatalogs(): Response<List<PublicationV2>>
}