package com.tjek.sdk.publicationviewer.paged

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.tjek.sdk.api.models.PublicationHotspotV2
import com.tjek.sdk.publicationviewer.paged.libs.HotspotView

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

    fun showHotspots(list: List<PublicationHotspotV2>) {
        if (list.isEmpty()) return
        val hsViews = mutableListOf<View>()
        for (h in list) {
            addView(HotspotView(context, h, pages))
        }
        hsViews.forEach { it.animation.startNow() }
    }
}