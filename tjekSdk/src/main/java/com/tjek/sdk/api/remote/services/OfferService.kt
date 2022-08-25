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
import com.tjek.sdk.api.models.OfferV2Decodable
import com.tjek.sdk.api.models.OfferV4DecodableContainer
import com.tjek.sdk.api.remote.request.IncitoOfferAPIQuery
import retrofit2.Response
import retrofit2.http.*

interface OfferService {

    @GET("v2/offers")
    suspend fun getOffers(@QueryMap queryParams: Map<String, String>): Response<List<OfferV2Decodable>>

    @GET("v2/offers/{offerId}")
    suspend fun getOffer(@Path("offerId") offerId: Id): Response<OfferV2Decodable>

    @GET("v2/offers/search")
    suspend fun getOffersSearch(@QueryMap queryParams: Map<String, String>): Response<List<OfferV2Decodable>>

    @POST("v4/rpc/get_offer_from_incito_publication_view")
    suspend fun getOfferFromIncito(@Body incitoOfferAPIQuery: IncitoOfferAPIQuery): Response<OfferV4DecodableContainer>
}