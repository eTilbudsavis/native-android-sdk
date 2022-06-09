package com.tjek.sdk.api.remote.models.v2

import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import java.lang.NumberFormatException

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
    val colorHex: String?
) : Parcelable