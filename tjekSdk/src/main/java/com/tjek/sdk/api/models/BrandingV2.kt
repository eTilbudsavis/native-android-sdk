package com.tjek.sdk.api.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.tjek.sdk.api.HexColorStr
import kotlinx.parcelize.Parcelize

@Keep
@JsonClass(generateAdapter = true)
@Parcelize
data class BrandingV2(
    val name: String?,
    val website: String?,
    val description: String?,
    @Json(name = "logo")
    val logoURL: String?,
    @Json(name = "color")
    val colorHex: HexColorStr?
) : Parcelable