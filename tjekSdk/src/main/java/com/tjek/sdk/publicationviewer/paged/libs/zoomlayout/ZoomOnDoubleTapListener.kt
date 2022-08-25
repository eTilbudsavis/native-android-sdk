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
class ZoomOnDoubleTapListener(threeStep: Boolean) : EventListener {

    private var mThreeStep = threeStep

    override fun onEvent(event: Event): Boolean {
        return when (event) {
            is Event.DoubleTap -> onDoubleTap(event.view, event.info)
            else -> false
        }
    }

    private fun onDoubleTap(view: ZoomLayout, info: TapInfo): Boolean {
        try {
            if (mThreeStep) {
                threeStep(view, info.absoluteX, info.absoluteY)
            } else {
                twoStep(view, info.absoluteX, info.absoluteY)
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            // Can sometimes happen when absoluteX and absoluteY are called
        }
        return true
    }

    private fun twoStep(view: ZoomLayout, x: Float, y: Float) {
        if (view.scale > view.minScale) {
            view.setScale(view.minScale, true)
        } else {
            view.setScale(view.maxScale, x, y, true)
        }
    }

    private fun threeStep(view: ZoomLayout, x: Float, y: Float) {
        val scale = view.scale
        val medium = view.minScale + (view.maxScale - view.minScale) * 0.3f
        if (scale < medium) {
            view.setScale(medium, x, y, true)
        } else if (scale >= medium && scale < view.maxScale) {
            view.setScale(view.maxScale, x, y, true)
        } else {
            view.setScale(view.minScale, true)
        }
    }
}