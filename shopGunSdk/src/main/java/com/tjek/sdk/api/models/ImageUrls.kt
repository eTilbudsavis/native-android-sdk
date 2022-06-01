package com.tjek.sdk.api.models

internal const val avgThumbWidth = 177
internal const val avgViewWidth = 768
internal const val avgZoomWidth = 1536

data class ImageUrls(
    val images: List<ImageData> = emptyList()
) {
    private val sortedImages = images.sortedWith(compareBy { it.width })

    val thumb = sortedImages.findLast { it.width <= avgThumbWidth }?.url ?: sortedImages.last().url
    val view = sortedImages.findLast { it.width <= avgViewWidth }?.url ?: sortedImages.last().url
    val zoom = sortedImages.findLast { it.width <= avgZoomWidth }?.url ?: sortedImages.last().url
}

data class ImageData(
    val width: Int = 1,
    val height: Int? = null,
    val url: String = ""
)