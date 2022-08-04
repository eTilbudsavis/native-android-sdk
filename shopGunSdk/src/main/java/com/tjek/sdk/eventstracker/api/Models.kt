package com.tjek.sdk.eventstracker.api

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import com.tjek.sdk.eventstracker.Event

@Keep
@JsonClass(generateAdapter = true)
data class ShippableEvents (
    val events: List<Event>
)

@Keep
@JsonClass(generateAdapter = true)
data class ShippedEvents(
    val status: EventStatus,
    val id: String,
    val errors: List<ShipError>
)

@Keep
@JsonClass(generateAdapter = true)
data class ShipError(
    val type: String,
    val path: List<String>?
)

@Keep
@JsonClass(generateAdapter = true)
data class EventResponse(
    val events: List<ShippedEvents>
)

@Suppress("EnumEntryName")
enum class EventStatus { ack, nack, validationError, unknown }