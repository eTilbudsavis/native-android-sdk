package com.tjek.sdk
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
import android.graphics.Color
import com.tjek.sdk.api.HexColorStr

// An int that can be used for setting colors
typealias ColorInt = Int

// Convert the hexadecimal string into an integer
fun HexColorStr?.getColorInt(): ColorInt {
    return if (this == null || length < 2 ) 0
    else try {
        Color.parseColor(if (this[0] != '#') "#$this" else this)
    } catch (e: Exception) {
        0
    }
}

// Get a nice primary-text-color for text on top of the given color
fun ColorInt.getPrimaryText(): ColorInt {
    return getPrimaryTextColor(isLight(this))
}

// Get a nice secondary-text-color for text on top of the given color
fun ColorInt.getSecondaryText(): ColorInt {
    return getSecondaryTextColor(isLight(this))
}

private fun getSecondaryTextColor(brightBackground: Boolean): ColorInt {
    val color = if (brightBackground) Color.BLACK else Color.WHITE
    val alpha = if (brightBackground) TEXT_ALPHA_SECONDARY_DARK else TEXT_ALPHA_SECONDARY_LIGHT
    return androidx.core.graphics.ColorUtils.setAlphaComponent(color, alpha)
}

private fun getPrimaryTextColor(brightBackground: Boolean): ColorInt {
    val color = if (brightBackground) Color.BLACK else Color.WHITE
    val alpha = if (brightBackground) TEXT_ALPHA_PRIMARY_DARK else TEXT_ALPHA_PRIMARY_LIGHT
    return androidx.core.graphics.ColorUtils.setAlphaComponent(color, alpha)
}

private fun isLight(color: ColorInt): Boolean =
    androidx.core.graphics.ColorUtils.calculateLuminance(color) > THRESHOLD_LIGHT

/** Google definition: alpha = 0.70f  */
const val TEXT_ALPHA_SECONDARY_LIGHT = (255 * 0.70f).toInt()
/** Google definition: alpha = 0.54f  */
const val TEXT_ALPHA_SECONDARY_DARK = (255 * 0.54f).toInt()
/** Google definition: alpha = 1.0f  */
const val TEXT_ALPHA_PRIMARY_LIGHT = (255 * 1.0f).toInt()
/** Google definition: alpha = 0.87f  */
const val TEXT_ALPHA_PRIMARY_DARK = (255 * 0.87f).toInt()
const val THRESHOLD_LIGHT = 0.54
