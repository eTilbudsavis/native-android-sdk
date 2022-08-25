package com.tjek.sdk.eventstracker.api

import com.tjek.sdk.api.remote.ResponseType
import com.tjek.sdk.api.remote.request.APIRequestBase
import com.tjek.sdk.eventstracker.ShippableEvent
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

internal object ShipEventRequest : APIRequestBase() {

    private val eventService: EventService by lazy { EventClient.getClient().create(EventService::class.java) }

    suspend fun shipEvents(
        events: List<ShippableEvent>,
    ): ResponseType<EventResponse> {
        return safeApiCall(
            decoder = { it }) {
            // create a JSONArray from the event list
            val eventsArray = JSONArray()
            events.forEach {
                // the events need to be reconverted to json for the serialization to work properly
                eventsArray.put(JSONObject(it.jsonEvent))
            }
            // create the body request the way the server is expecting it
            val bodyRequest = JSONObject().put("events", eventsArray).toString()
                .toRequestBody(contentType = "application/json".toMediaTypeOrNull())
            eventService.syncEvents(bodyRequest)
        }
    }
}