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
import com.shopgun.android.sdk.palette.MaterialColor;

public class ColorUtils {

    public static final String TAG = Constants.getTag(ColorUtils.class);

    private ColorUtils() {
        // empty
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

    public static String toARGBString(MaterialColor color) {
        return color == null ? null : toARGBString(color.getValue());
    }

    public static String toARGBString(int color) {
        return String.format("#%08X", color);
    }

    public static String toRGBString(int color) {
        return String.format("#%06X", 0xFFFFFF & color);
    }

    public static String toHsvString(MaterialColor color) {
        return toHsvString(toHSV(color.getValue()));
    }

    public static String toHsvString(int color) {
        return toHsvString(toHSV(color));
    }

    public static String toHsvString(float[] hsv) {
        String format = "hsv[%.2f, %.2f, %.2f]";
        return String.format(format, hsv[0], hsv[1], hsv[2]);
    }

    public static float[] toHSV(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return hsv;
    }

    /**
     * Set the alpha component of {@code color} to be {@code alpha}.
     * <p>Wrapper for {@link android.support.v4.graphics.ColorUtils#setAlphaComponent(int, int)}</p>
     */
    public static int setAlphaComponent(int color, int alpha) {
        return android.support.v4.graphics.ColorUtils.setAlphaComponent(color, alpha);
    }

    /**
     * Returns the luminance of a color.
     *
     * Formula defined here: http://www.w3.org/TR/2008/REC-WCAG20-20081211/#relativeluminancedef
     * <p>Wrapper for {@link android.support.v4.graphics.ColorUtils#calculateLuminance(int)}</p>
     */
    public static double calculateLuminance(int color) {
        return android.support.v4.graphics.ColorUtils.calculateLuminance(color);
    }

    /**
     * Returns the contrast ratio between {@code foreground} and {@code background}.
     * {@code background} must be opaque.
     * <p>
     * Formula defined
     * <a href="http://www.w3.org/TR/2008/REC-WCAG20-20081211/#contrast-ratiodef">here</a>.
     * <p>Wrapper for {@link android.support.v4.graphics.ColorUtils#calculateContrast(int, int)}</p>
     */
    public static double calculateContrast(int foreground, int background) {
        return android.support.v4.graphics.ColorUtils.calculateContrast(foreground, background);
    }


    /**
     * Calculates the minimum alpha value which can be applied to {@code foreground} so that would
     * have a contrast value of at least {@code minContrastRatio} when compared to
     * {@code background}.
     *
     * @param foreground       the foreground color.
     * @param background       the background color. Should be opaque.
     * @param minContrastRatio the minimum contrast ratio.
     * @return the alpha value in the range 0-255, or -1 if no value could be calculated.
     * <p>Wrapper for {@link android.support.v4.graphics.ColorUtils#calculateMinimumAlpha(int, int, float)}</p>
     */
    public static int calculateMinimumAlpha(int foreground, int background, float minContrastRatio) {
        return android.support.v4.graphics.ColorUtils.calculateMinimumAlpha(foreground, background, minContrastRatio);
    }

}
