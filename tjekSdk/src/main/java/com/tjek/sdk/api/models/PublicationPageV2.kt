package com.tjek.sdk.api.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PublicationPageV2(
    val index: Int,
    val title: String?,
    val aspectRatio: Double,
    val images: ImageUrlsV2
): Parcelable
