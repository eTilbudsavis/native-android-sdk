package com.tjek.sdk.publicationviewer.paged.libs.zoomlayout
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
import android.view.MotionEvent
import android.view.View
import java.util.*

data class TapInfo(
    var view: View,
    var absoluteX: Float = 0f,
    var absoluteY: Float = 0f,
    var relativeX: Float = 0f,
    var relativeY: Float = 0f,
    var percentX: Float = 0f,
    var percentY: Float = 0f,
    var contentClicked: Boolean = false
) {
    private val strFormat =
        "TapInfo[ absX:%.0f, absY:%.0f, relX:%.0f, relY:%.0f, percentX:%.2f, percentY:%.2f, contentClicked:%s ]"

    constructor(zoomLayout: ZoomLayout, e: MotionEvent): this(zoomLayout) {
        absoluteX = e.x
        absoluteY = e.y
        zoomLayout.array[0] = absoluteX
        zoomLayout.array[1] = absoluteY
        zoomLayout.screenPointsToScaledPoints(zoomLayout.array)
        val v = zoomLayout.getChildAt(0)
        relativeX = zoomLayout.array[0] - v.left
        relativeY = zoomLayout.array[1] - v.top
        percentX = relativeX / v.width.toFloat()
        percentY = relativeY / v.height.toFloat()
        contentClicked = zoomLayout.drawRect.contains(absoluteX, absoluteY)

    }

    override fun toString(): String {
        return String.format(Locale.ENGLISH, strFormat,
            absoluteX, absoluteY, relativeX, relativeY, percentX, percentY, contentClicked
        )
    }
}
