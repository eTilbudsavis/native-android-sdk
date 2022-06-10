package com.tjek.sdk.api.remote.services

import com.tjek.sdk.api.Id
import com.tjek.sdk.api.remote.models.v2.StoreV2Decodable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface StoreService {

    @GET("v2/stores/{storeId}")
    suspend fun getStore(@Path("storeId") storeId: Id): Response<StoreV2Decodable>
}