package com.tjek.sdk.publicationviewer.paged.verso

data class VersoSpreadProperty (
    val pages: IntArray?,
    val width: Float,
    val maxZoomScale: Float,
    val minZoomScale: Float
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VersoSpreadProperty) return false

        if (pages != null) {
            if (other.pages == null) return false
            if (!pages.contentEquals(other.pages)) return false
        } else if (other.pages != null) return false
        if (width != other.width) return false
        if (maxZoomScale != other.maxZoomScale) return false
        if (minZoomScale != other.minZoomScale) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pages?.contentHashCode() ?: 0
        result = 31 * result + width.hashCode()
        result = 31 * result + maxZoomScale.hashCode()
        result = 31 * result + minZoomScale.hashCode()
        return result
    }
}