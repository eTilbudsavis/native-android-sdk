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

import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;

@Deprecated
public class ColorUtils {

    private ColorUtils() {
        // empty
    }

    /** @deprecated see {@link com.shopgun.android.utils.ColorUtils#getCompliment(int)}*/
    @Deprecated
    public static int getCompliment(int color) {
        return com.shopgun.android.utils.ColorUtils.getCompliment(color);
    }

    /** @deprecated see {@link com.shopgun.android.utils.ColorUtils#toARGBString(int)}*/
    @Deprecated
    public static String toARGBString(int color) {
        return com.shopgun.android.utils.ColorUtils.toARGBString(color);
    }

    /** @deprecated see {@link com.shopgun.android.utils.ColorUtils#toRGBString(int)}*/
    @Deprecated
    public static String toRGBString(int color) {
        return com.shopgun.android.utils.ColorUtils.toRGBString(color);
    }

    /** @deprecated see {@link com.shopgun.android.utils.ColorUtils#toHsvString(int)}*/
    @Deprecated
    public static String toHsvString(int color) {
        return com.shopgun.android.utils.ColorUtils.toHsvString(color);
    }

    /** @deprecated see {@link com.shopgun.android.utils.ColorUtils#toHsvString(float[])}*/
    @Deprecated
    public static String toHsvString(float[] hsv) {
        return com.shopgun.android.utils.ColorUtils.toHsvString(hsv);
    }

    /** @deprecated see {@link com.shopgun.android.utils.ColorUtils#toHSV(int)}*/
    @Deprecated
    public static float[] toHSV(int color) {
        return com.shopgun.android.utils.ColorUtils.toHSV(color);
    }

    /** @deprecated see {@link com.shopgun.android.utils.ColorUtils#setAlphaComponent(int, int)}*/
    @Deprecated
    @ColorInt
    public static int setAlphaComponent(@ColorInt int color, @IntRange(from = 0x0, to = 0xFF) int alpha) {
        return com.shopgun.android.utils.ColorUtils.setAlphaComponent(color, alpha);
    }

    /** @deprecated see {@link com.shopgun.android.utils.ColorUtils#calculateLuminance(int)}*/
    @Deprecated
    public static double calculateLuminance(@ColorInt int color) {
        return com.shopgun.android.utils.ColorUtils.calculateLuminance(color);
    }

    /** @deprecated see {@link com.shopgun.android.utils.ColorUtils#calculateContrast(int, int)}*/
    @Deprecated
    public static double calculateContrast(@ColorInt int foreground, @ColorInt int background) {
        return com.shopgun.android.utils.ColorUtils.calculateContrast(foreground, background);
    }

    /** @deprecated see {@link com.shopgun.android.utils.ColorUtils#calculateMinimumAlpha(int, int, float)}*/
    @Deprecated
    public static int calculateMinimumAlpha(@ColorInt int foreground, @ColorInt int background, float minContrastRatio) {
        return com.shopgun.android.utils.ColorUtils.calculateMinimumAlpha(foreground, background, minContrastRatio);
    }

}
