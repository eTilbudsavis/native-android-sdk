package com.tjek.sdk

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
