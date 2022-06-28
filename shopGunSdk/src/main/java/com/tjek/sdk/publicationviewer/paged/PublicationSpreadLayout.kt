package com.tjek.sdk.publicationviewer.paged

import android.content.Context
import android.widget.FrameLayout

class PublicationSpreadLayout(
    context: Context,
    val pages: IntArray
) : FrameLayout(context) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST)
        val childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)
        measureChildren(childWidthMeasureSpec, childHeightMeasureSpec)
        setMeasuredDimension(width, height)
    }
}