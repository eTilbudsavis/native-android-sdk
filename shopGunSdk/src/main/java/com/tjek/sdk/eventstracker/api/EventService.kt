package com.tjek.sdk.eventstracker.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface EventService {

    @POST("sync")
    suspend fun syncEvents(@Body events: ShippableEvents): Response<EventResponse>
}