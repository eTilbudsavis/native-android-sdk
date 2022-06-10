package com.tjek.sdk.api.remote.models.v2

import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.tjek.sdk.api.Id

@Keep
@JsonClass(generateAdapter = true)
data class BusinessV2Decodable(
    val id:Id,
    val name: String,
    val website: String?,
    val description: String?,
    @Json(name = "description_markdown")
    val descriptionMarkdown: String?,
    @Json(name = "logo")
    val logoOnWhite: String,
    val color: String?,
    @Json(name = "pageflip")
    val pageFlip: PageFlipV2,
    val country: CountryV2
)

@Keep
@JsonClass(generateAdapter = true)
data class PageFlipV2(
    val logo: String,
    val color: String?,
)