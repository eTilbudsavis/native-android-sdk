package com.tjek.sdk.publicationviewer.paged.verso

import android.graphics.Rect
import java.util.*

data class VersoZoomPanInfo(
    val fragment: VersoPageViewFragment,
    val scale: Float,
    val viewRect: Rect
) {

    val position: Int = fragment.mPosition
    val pages: IntArray = Arrays.copyOf(fragment.mPages, fragment.mPages.size)

    override fun toString(): String {
        return java.lang.String.format(
            Locale.ENGLISH,
            strFormat,
            position,
            pages.joinToString(),
            scale,
            viewRect.toString()
        )
    }

    companion object {
        private const val strFormat =
            "VersoZoomPanInfo[ position:%s, pages:%s, scale:%.2f, viewRect:%s ]"
    }

}