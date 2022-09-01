package com.tjek.sdk.api.remote.services
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