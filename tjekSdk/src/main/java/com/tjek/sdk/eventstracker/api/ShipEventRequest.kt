package com.tjek.sdk.eventstracker.api
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