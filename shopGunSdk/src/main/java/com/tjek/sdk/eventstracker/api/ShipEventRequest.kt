package com.tjek.sdk.eventstracker.api

import com.tjek.sdk.api.remote.ResponseType
import com.tjek.sdk.api.remote.request.APIRequestBase
import com.tjek.sdk.eventstracker.Event

internal object ShipEventRequest : APIRequestBase() {

    private val eventService: EventService by lazy { EventClient.getClient().create(EventService::class.java) }

    suspend fun shipEvents(
        events: List<Event>
    ): ResponseType<EventResponse> {
        return safeApiCall(
            decoder = { it }) {
            eventService.syncEvents(ShippableEvents(events))
        }
    }
}