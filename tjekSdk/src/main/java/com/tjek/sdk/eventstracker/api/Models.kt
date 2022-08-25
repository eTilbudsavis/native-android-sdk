package com.tjek.sdk.eventstracker.api

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class ShippedEvent(
    val status: EventStatus,
    val id: String,
    val errors: List<ShipError>
)

@Keep
@JsonClass(generateAdapter = true)
data class ShipError(
    val type: String,
    val path: List<String>?,
    val message: String?
)

@Keep
@JsonClass(generateAdapter = true)
data class EventResponse(
    val events: List<ShippedEvent>
)

@Suppress("EnumEntryName", "unused")
enum class EventStatus { ack, nack, validation_error, unknown }