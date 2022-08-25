package com.tjek.sdk.publicationviewer.paged.views
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
import android.view.animation.AnimationUtils
import com.tjek.sdk.R
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