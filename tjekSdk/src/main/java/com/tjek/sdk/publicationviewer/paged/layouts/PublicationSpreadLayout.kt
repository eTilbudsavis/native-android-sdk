package com.tjek.sdk.publicationviewer.paged.layouts

import android.content.Context
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import com.shopgun.android.sdk.R
import com.tjek.sdk.api.models.PublicationHotspotV2
import com.tjek.sdk.publicationviewer.paged.views.*

// This overlay will add the hotspot views and a dimmed overlay that will mask the rest of the page,
// creating the highlight effect
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

    fun showHotspots(list: List<PublicationHotspotV2>, longPress: Boolean) {
        if (list.isEmpty()) return
        val hsViews = mutableListOf<View>()
        for (h in list) {
            val view = HotspotView(context, h, pages, longPress)
            addView(view)
            hsViews.add(view)
        }
        val overlay = HotspotOverlay(context, longPress)
        addView(overlay)
        viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                if (overlay.visibility == View.VISIBLE) {
                    val corner: Float = resources.getDimensionPixelSize(R.dimen.tjek_pagedpub_hotspot_corner_radius).toFloat()
                    overlay.drawHolesForViews(*hsViews.map { HighlightedView(it, HoleType.RoundRectangle(corner)) }.toTypedArray())
                    viewTreeObserver.removeOnPreDrawListener(this)
                }
                return true
            }
        })

        if (longPress) {
            for (h in list) {
                val view = HotspotLongPressView(context, h, pages)
                addView(view)
                hsViews.add(view)
            }
        }

        hsViews.forEach { it.animation.startNow() }
        overlay.animation.startNow()
    }

    fun removeHotspots() {
        removeAllViews()
    }
}