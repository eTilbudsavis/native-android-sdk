package com.tjek.sdk.api.models

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import com.tjek.sdk.api.Id

@Keep
@JsonClass(generateAdapter = true)
data class CountryV2Decodable(
    val id:Id
)
