package com.tjek.sdk.api.remote.models.v2

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.tjek.sdk.api.Id
import org.json.JSONArray

@Keep
@JsonClass(generateAdapter = true)
data class StoreV2(
    val id: Id?,
    val street: String?,
    val city: String?,
    @Json(name = "zip_code")
    val zipCode: String?,
    val country: CountryV2?,
    val latitude: Double?,
    val longitude: Double?,
    @Json(name = "dealer_id")
    val dealerId: Id?,
    val branding: BrandingV2?,
    val contact: String?,
    @Json(name = "opening_hours")
    val openingHours: JSONArray?
)
//todo: move the opening hours logic