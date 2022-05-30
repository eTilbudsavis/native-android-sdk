package com.tjek.sdk.api.remote.models.v2

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

@Keep
@JsonClass(generateAdapter = true)
data class ImageUrlsV2(
    val view: String?,
    val zoom: String?,
    val thumb: String?
)