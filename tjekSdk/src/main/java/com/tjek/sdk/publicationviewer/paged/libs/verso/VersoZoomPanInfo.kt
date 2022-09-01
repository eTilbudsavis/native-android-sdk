package com.tjek.sdk.publicationviewer.paged.libs.verso
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