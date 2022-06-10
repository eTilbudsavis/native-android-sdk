package com.tjek.sdk.api.remote.models.v2

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.tjek.sdk.api.DayOfWeekStr
import com.tjek.sdk.api.Id
import com.tjek.sdk.api.TimeOfDayStr
import com.tjek.sdk.api.ValidityDateStr

@Keep
@JsonClass(generateAdapter = true)
data class StoreV2Decodable(
    val id: Id,
    val street: String?,
    val city: String?,
    @Json(name = "zip_code")
    val zipCode: String?,
    val country: CountryV2,
    val latitude: Double,
    val longitude: Double,
    @Json(name = "dealer_id")
    val businessId: Id,
    val branding: BrandingV2,
    val contact: String?,
    @Json(name = "opening_hours")
    val openingHours: List<OpeningHoursDecodable>?
)

@Keep
@JsonClass(generateAdapter = true)
data class OpeningHoursDecodable (
    @Json(name = "day_of_week")
    val dayOfWeekStr: DayOfWeekStr?,
    @Json(name = "valid_from")
    val validFrom: ValidityDateStr?,
    @Json(name = "valid_until")
    val validUntil: ValidityDateStr?,
    val opens: TimeOfDayStr?,
    val closes: TimeOfDayStr?
)