package com.tjek.sdk.api.remote.models.v2

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import com.tjek.sdk.api.Id

@Keep
@JsonClass(generateAdapter = true)
data class CountryV2(
    val id:Id?
)
