package com.tjek.sdk.eventstracker
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
import com.tjek.sdk.createUUID
import org.json.JSONObject

enum class EventType(val code: Int) {
    Dummy                          (0),
    PagedPublicationOpened         (1),
    PagedPublicationPageOpened     (2),
    OfferInteraction               (3),
    Searched                       (5),
    FirstOfferOpenedAfterSearch    (6),
    OfferOpenedAfterSearch         (7),
    SearchResultsViewed            (9),
    IncitoPublicationOpenedV2      (11),
    BasicAnalytics                 (12)
}

typealias PayloadType = Map<String, Any>

//  An Event defines a package of data that can be sent to the EventsTracker.
//  There are some core properties that are required, and any additional metadata is added to the `payload`.
data class Event(

    // The unique identifier of the event. A UUID string.
    val id: String = createUUID(),

    // The version of the event. If the format of the event ever changes, this may increase.
    // It is used to choose where to send the event, and by the server to decide how to process the event.
    val version: Int = 2,

    // The timestamp the event was triggered.
    val timestamp: Long = timestamp(),

    // The type identifier of the event. There are a set of pre-defined constants that can be used here.
    // For the server to be able to parse the event, the type & payload must be consistent.
    val type: Int,

    // metadata of the event
    var payloadType: PayloadType = emptyMap()
) {

    // Merges a new payload into the current one.
    // New values for existing keys will override the old ones (so be careful!).
    fun mergePayload(newPayloadType: PayloadType) {
       val merge = payloadType + newPayloadType
        payloadType = merge
    }

    fun addViewToken(vt: String) {
        if (vt.isEmpty()) return
        mergePayload(mapOf(Pair("vt", vt)))
    }

    // countryCode is an ISO 3166-1 alpha-2 encoded string, like "DK"
    fun addCountryCode(countryCode: String) {
        if (countryCode.isEmpty()) return
        mergePayload(mapOf(Pair("l.c", countryCode)))
    }

    fun addLocation(geohash: String, timestamp: Long) {
        if (geohash.isEmpty() || timestamp <= 0) return
        mergePayload(mapOf(
            Pair("l.h", geohash),
            Pair("l.ht", timestamp)
        ))
    }

    fun addApplicationTrackId(id: String) {
        if (id.isEmpty()) return
        mergePayload(mapOf(Pair("_a", id)))
    }

    fun toJson(): String {
        return JSONObject().apply {
            put("_i", id)
            put("_v", version)
            put("_t", timestamp)
            put("_e", type)
            payloadType.forEach { put(it.key, it.value) }
        }.toString()
    }

    fun asShippableEvent(): ShippableEvent {
        return ShippableEvent(
            id = id,
            version = version,
            timestamp = timestamp,
            jsonEvent = toJson()
        )
    }
}