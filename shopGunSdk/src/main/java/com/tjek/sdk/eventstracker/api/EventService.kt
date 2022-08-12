package com.tjek.sdk.eventstracker.api

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface EventService {

    @POST("sync")
    suspend fun syncEvents(@Body events: RequestBody): Response<EventResponse>
}