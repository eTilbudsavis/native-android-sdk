package com.tjek.sdk.api.remote.services

import com.tjek.sdk.api.Id
import com.tjek.sdk.api.models.OfferV2Decodable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface OfferService {

    @GET("v2/offers")
    suspend fun getOffers(@QueryMap queryParams: Map<String, String>): Response<List<OfferV2Decodable>>

    @GET("v2/offers/{offerId}")
    suspend fun getOffer(@Path("offerId") offerId: Id): Response<OfferV2Decodable>

    @GET("v2/offers/search")
    suspend fun getOffersSearch(@QueryMap queryParams: Map<String, String>): Response<List<OfferV2Decodable>>
}