package com.tjek.sdk.api.remote.services

import com.tjek.sdk.api.Id
import com.tjek.sdk.api.models.OfferV2Decodable
import com.tjek.sdk.api.models.OfferV4Decodable
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