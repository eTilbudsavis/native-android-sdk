package com.tjek.sdk.api.remote.services

import com.tjek.sdk.api.Id
import com.tjek.sdk.api.remote.models.v2.BusinessV2Decodable
import com.tjek.sdk.api.remote.models.v2.StoreV2Decodable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface BusinessService {

    @GET("v2/dealers/{dealerId}")
    suspend fun getDealer(@Path("dealerId") dealerId: Id): Response<BusinessV2Decodable>

}