package com.tjek.sdk.api.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Keep
@JsonClass(generateAdapter = true)
@Parcelize
data class ImageV4(
    val width: Int,
    val height: Int?,
    val url: String
): Parcelable