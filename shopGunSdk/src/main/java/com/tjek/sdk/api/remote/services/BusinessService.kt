package com.tjek.sdk.api.remote.services

import com.tjek.sdk.api.Id
import com.tjek.sdk.api.models.BusinessV2Decodable
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface BusinessService {

    @GET("v2/dealers/{dealerId}")
    suspend fun getDealer(@Path("dealerId") dealerId: Id): Response<BusinessV2Decodable>

}