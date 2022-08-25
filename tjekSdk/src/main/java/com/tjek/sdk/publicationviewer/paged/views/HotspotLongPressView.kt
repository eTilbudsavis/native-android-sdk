package com.tjek.sdk.publicationviewer.paged.views

import android.content.Context
import android.view.animation.AnimationUtils
import com.shopgun.android.sdk.R
import com.tjek.sdk.api.models.PublicationHotspotV2

class HotspotLongPressView(
    context: Context?,
    hotspot: PublicationHotspotV2?,
    pages: IntArray?
) : HotspotView(context, hotspot, pages, true) {

    init {
        setBackgroundResource(R.drawable.tjek_sdk_pagedpub_hotspot_long_press_bg)
        animation = AnimationUtils.loadAnimation(getContext(), R.anim.tjek_sdk_pagedpub_hotspot_in_long_press_highlight_view)
    }

    override fun onAnimationEnd() {
        super.onAnimationEnd()
        visibility = GONE
    }

}