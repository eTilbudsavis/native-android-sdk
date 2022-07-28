package com.tjek.sdk.api.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

//internal const val avgThumbWidth = 177
//internal const val avgViewWidth = 768
//internal const val avgZoomWidth = 1536

@Keep
@JsonClass(generateAdapter = true)
@Parcelize
data class ImageUrlsV2(
    val view: String?,
    val zoom: String?,
    val thumb: String?
): Parcelable


// todo: is this needed?
//@Parcelize
//data class ImageUrls(
//    val images: List<ImageData> = emptyList()
//) : Parcelable {
//    @IgnoredOnParcel
//    private val sortedImages = images.sortedWith(compareBy { it.width })
//
//    @IgnoredOnParcel
//    val thumb = sortedImages.findLast { it.width <= avgThumbWidth }?.url ?: sortedImages.last().url
//    @IgnoredOnParcel
//    val view = sortedImages.findLast { it.width <= avgViewWidth }?.url ?: sortedImages.last().url
//    @IgnoredOnParcel
//    val zoom = sortedImages.findLast { it.width <= avgZoomWidth }?.url ?: sortedImages.last().url
//}
//
//@Parcelize
//data class ImageData(
//    val width: Int = 1,
//    val height: Int? = null,
//    val url: String = ""
//) : Parcelable