package com.tjek.sdk.api.models

internal const val estimatedThumbWidth = 177
internal const val estimatedViewWidth = 768
internal const val estimatedZoomWidth = 1536

data class ImageUrls(
    val images: List<ImageData> = emptyList()
) {
    private val sortedImages = images.sortedWith(compareBy { it.width })

    val thumb = sortedImages.findLast { it.width <= estimatedThumbWidth }?.url ?: sortedImages.last().url
    val view = sortedImages.findLast { it.width <= estimatedViewWidth }?.url ?: sortedImages.last().url
    val zoom = sortedImages.findLast { it.width <= estimatedZoomWidth }?.url ?: sortedImages.last().url
}

data class ImageData(
    val width: Int = 1,
    val height: Int? = null,
    val url: String = ""
)