package com.tjek.sdk.api.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Keep
@JsonClass(generateAdapter = true)
@Parcelize
data class ImageUrlsV2(
    val view: String?,
    val zoom: String?,
    val thumb: String?
): Parcelable
