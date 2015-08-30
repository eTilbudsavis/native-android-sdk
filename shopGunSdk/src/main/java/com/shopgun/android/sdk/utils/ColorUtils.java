/*******************************************************************************
 * Copyright 2015 ShopGun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.shopgun.android.sdk.utils;

import android.graphics.Color;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.log.SgnLog;

public class ColorUtils {

    public static final String TAG = Constants.getTag(ColorUtils.class);

    private ColorUtils() {
        // empty
    }

    /**
     * Remove any transparency from a color. The ShopGun v2 doesn't support alpha channel in colors.
     *
     * @param color A color
     * @return A new color, or <code>null</code> if input color is null
     */
    public static Integer stripAlpha(Integer color) {
        return stripAlpha(color, true);
    }

    /**
     * Remove any transparency from a color. The ShopGun v2 doesn't support alpha channel in colors.
     *
     * @param color       A color
     * @param showWarning <code>true</code> to send warnings to {@link SgnLog}
     * @return A new color, or <code>null</code> if input color is null
     */
    public static Integer stripAlpha(Integer color, boolean showWarning) {
        if (color != null) {
            if (showWarning && Color.alpha(color) < 255) {
                SgnLog.w(TAG, "eTilbudsavis API v2, doesn't support alpha colors - alpha will be stripped");
            }
            color |= 0x00000000ff000000;
        }
        return color;
    }

    /**
     * Method returns a ShopGun API friendly color string.<br>
     * Alpha isn't supported, and will be stripped. Warnings will be logged to console
     * <li>Color.WHITE = "FFFFFF"</li>
     * <li>Color.BLACK = "000000"</li>
     * <li>Color.BLUE = "0000FF"</li>
     *
     * @param color The color to parse
     * @return A string, or null
     */
    public static String toString(Integer color) {
        return toString(color, true);
    }

    /**
     * Method returns a ShopGun API friendly color string.<br>
     * Alpha isn't supported, and will be stripped. This will be logged as a warning in console
     * if ignoreWarnings is false.
     * <li>Color.WHITE = "FFFFFF"</li>
     * <li>Color.BLACK = "000000"</li>
     * <li>Color.BLUE = "0000FF"</li>
     *
     * @param color          The color to parse
     * @param ignoreWarnings - set to true to ignore alpha warnings
     * @return A string, or <code>null</code> if input color is null
     */
    public static String toString(Integer color, boolean showWarning) {
        return toString(color, showWarning, true);
    }

    /**
     * Convert a color into a String.
     * <br/>
     * <br/>
     * You can create two types of Strings:
     * <ol>
     * <li> A regular #ARGB string (e.g.: {@link Color#BLACK} = "#FF000000"), or
     * <li> An ShopGun API v2 compliant string, where alpha and '#' isn't allowed, e.g.: {@link Color#BLACK} = "000000")
     * </ol>
     * <p/>
     * <li>Color.WHITE = "FFFFFF"</li>
     * <li>Color.BLACK = "000000"</li>
     * <li>Color.BLUE = "0000FF"</li>
     *
     * @param color          The color to parse
     * @param ignoreWarnings - set to true to ignore alpha warnings
     * @param apiCompliant   <code>true</code> to create a ShopGun API v2 compliant string like "RGB".
     *                       <code>false</code> to get an "#ARGB"
     * @return A string, or <code>null</code> if input color is null
     */
    public static String toString(Integer color, boolean showWarning, boolean apiCompliant) {
        if (color == null) {
            return null;
        }
        color = stripAlpha(color, showWarning);
        if (apiCompliant) {
            return String.format("%06X", 0xFFFFFF & color);
        } else {
            // not eta api valid string
            return String.format("#%08X", color);
        }
    }

    /**
     * Converts a given string to a color
     * <li>"FFFFFF" = Color.WHITE</li>
     * <li>"000000" = Color.BLACK</li>
     * <li>"0000FF" = Color.BLUE</li>
     *
     * @return A string, or <code>null</code> if input color is null
     */
    public static Integer toColor(String color) {
        if (color == null) {
            return null;
        }
        if (!color.startsWith("#")) {
            color = "#" + color;
        }
        try {
            return Color.parseColor(color);
        } catch (NumberFormatException e) {
//			SgnLog.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    /**
     * Returns the complimentary color. (Alpha channel remains intact)
     *
     * @param color An ARGB color to return the compliment of
     * @return An ARGB of compliment color
     */
    public static int getCompliment(int color) {
        // get existing colors
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int blue = Color.blue(color);
        int green = Color.green(color);

        // find compliments
        red = (~red) & 0xff;
        blue = (~blue) & 0xff;
        green = (~green) & 0xff;

        return Color.argb(alpha, red, green, blue);
    }

    /**
     * Creates a new color with a decrease in value/brightness.
     *
     * @param color  The color to darken
     * @param shades The number of shades to darken the color
     * @param offset The offset in value/lumination
     * @return a new color
     */
    public static int decreaseBrightness(int color, int shades, float offset) {
        return decreaseHSV(2, color, shades, offset);
    }

    /**
     * Creates a new color with a decrease in saturation.
     *
     * @param color  The color to saturate
     * @param shades The number of shades to saturate the color
     * @param offset The offset in saturation
     * @return a new color
     */
    public static int decreaseSaturation(int color, int shades, float offset) {
        return decreaseHSV(1, color, shades, offset);
    }

    /**
     * Creates a new color with a decrease in hue.
     *
     * @param color  The color to offset the hue on
     * @param shades The number of steps to make in hue
     * @param offset The offset in hue
     * @return a new color
     */
    public static int decreaseHue(int color, int shades, float offset) {
        return decreaseHSV(0, color, shades, offset);
    }

    /**
     * Creates a new color with a increase in value/brightness.
     *
     * @param color  The color to darken
     * @param shades The number of shades to darken the color
     * @param offset The
     * @return a new color
     */
    public static int increaseBrightness(int color, int shades, float offset) {
        return increaseHSV(2, color, shades, offset);
    }

    public static int increaseSaturation(int color, int shades, float offset) {
        return increaseHSV(1, color, shades, offset);
    }

    public static int increaseHue(int color, int shades, float offset) {
        return increaseHSV(0, color, shades, offset);
    }

    /**
     * Creates a new color, with a decreased Hue, Saturation, or Value
     *
     * @param what   Either Hue (0), Saturation (1), or Value (2)
     * @param color A color
     * @param shades Number of shades to decrease
     * @param offset The offset for each shade
     * @return A new color
     */
    public static int decreaseHSV(int what, int color, int shades, float offset) {
        if (whatOutOfBounds(what)) { return color; }
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        decrease(what, hsv, shades, offset);
        return Color.HSVToColor(Color.alpha(color), hsv);
    }

    /**
     * Method will return a new color, with a increase Hue, Saturation, or Value
     *
     * @param what   Either Hue (0), Saturation (1), or Value (2)
     * @param color A color
     * @param shades Number of shades to increase
     * @param offset The offset for each shade
     * @return A new color
     */
    public static int increaseHSV(int what, int color, int shades, float offset) {
        if (whatOutOfBounds(what)) { return color; }
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        increase(what, hsv, shades, offset);
        return Color.HSVToColor(Color.alpha(color), hsv);
    }

    /**
     * Method will return a new color, with a increase Hue, Saturation, or Lightness
     *
     * @param what   Either Hue (0), Saturation (1), or Lightness (2)
     * @param color A color
     * @param shades Number of shades to increase
     * @param offset The offset for each shade
     * @return A new color
     */
    public static int increaseHSL(int what, int color, int shades, float offset) {
        if (whatOutOfBounds(what)) { return color; }
        float[] hsl = new float[3];
        android.support.v4.graphics.ColorUtils.colorToHSL(color, hsl);
        increase(what, hsl, shades, offset);
        int tmp = android.support.v4.graphics.ColorUtils.HSLToColor(hsl);
        return android.support.v4.graphics.ColorUtils.setAlphaComponent(tmp, Color.alpha(color));
    }

    /**
     * Creates a new color, with a decreased Hue, Saturation, or Lightness
     *
     * @param what   Either Hue (0), Saturation (1), or Lightness (2)
     * @param color A color
     * @param shades Number of shades to decrease
     * @param offset The offset for each shade
     * @return A new color
     */
    public static int decreaseHSL(int what, int color, int shades, float offset) {
        if (whatOutOfBounds(what)) { return color; }
        float[] hsl = new float[3];
        android.support.v4.graphics.ColorUtils.colorToHSL(color, hsl);
        decrease(what, hsl, shades, offset);
        int tmp = android.support.v4.graphics.ColorUtils.HSLToColor(hsl);
        return android.support.v4.graphics.ColorUtils.setAlphaComponent(tmp, Color.alpha(color));
    }

    private static boolean whatOutOfBounds(int what) {
        return what < 0 || 2 < what;
    }

    private static void decrease(int what, float[] hsl, int shades, float offset) {
        while (shades > 0) {
            shades--;
            hsl[what] = hsl[what] * (1.0f - offset);
        }
    }

    private static void increase(int what, float[] hsl, int shades, float offset) {
        while (shades > 0) {
            shades--;
            hsl[what] += ((1.0f - hsl[what]) * offset);
        }
    }

    /**
     * Get brightness of a specific color
     *
     * @param color A color
     * @return The brightness, in the range 0 - 255
     */
    public static int getBrightness(int color) {
        return (int) Math.sqrt(
                Color.red(color) * Color.red(color) * .241 +
                        Color.green(color) * Color.green(color) * .691 +
                        Color.blue(color) * Color.blue(color) * .068);
    }

}
