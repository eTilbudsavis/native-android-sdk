package com.tjek.sdk.api.remote.models.v2

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.tjek.sdk.api.remote.models.HexColor

@Keep
@JsonClass(generateAdapter = true)
data class BrandingV2(
    val name: String?,
    val website: String?,
    val description: String?,
    @Json(name = "logo")
    val logoURL: String?,
    @HexColor
    val color: Int?
)