package com.tjek.sdk.publicationviewer.paged.layouts
/*
 * Copyright (C) 2022 Tjek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.content.Context
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import com.tjek.sdk.R
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