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

package com.shopgun.android.sdk.palette;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.graphics.ColorUtils;

import com.shopgun.android.sdk.log.SgnLog;

public class SgnPalette {

    public static final String TAG = SgnPalette.class.getSimpleName();

    /** Google definition: alpha = 0.30f */
    public static final int TEXT_ALPHA_DISABLED_LIGHT = (int)(255*0.3f);
    /** Google definition: alpha = 0.38f */
    public static final int TEXT_ALPHA_DISABLED_DARK = (int)(255*0.38f);
    /** Google definition: alpha = 0.70f */
    public static final int TEXT_ALPHA_SECONDARY_LIGHT = (int)(255*0.70f);
    /** Google definition: alpha = 0.54f */
    public static final int TEXT_ALPHA_SECONDARY_DARK = (int)(255*0.54f);
    /** Google definition: alpha = 1.0f */
    public static final int TEXT_ALPHA_PRIMARY_LIGHT = (int)(255*1.0f);
    /** Google definition: alpha = 0.87f */
    public static final int TEXT_ALPHA_PRIMARY_DARK = (int)(255*0.87f);

    public static final double THRESHOLD_VERY_BRIGHT = 0.95d;
    public static final double THRESHOLD_BRIGHT = 0.87d;
    public static final double THRESHOLD_LIGHT = 0.50d;
    public static final double THRESHOLD_DARK = 0.13d;
    public static final double THRESHOLD_VERY_DARK = 0.025d;

    // We have modifier-percentages that map to shades.
    // Use this list to find the matching percentage, or lerp if the shade is not on a x100 bounds
    private static float[] mValuePercentConversion = new float[]{
            1.06f,//50
            0.70f,//100
            0.50f,//200
            0.30f,//300
            0.15f,//400
            0.00f,//500
            -0.10f,//600
            -0.25f,//700
            -0.42f,//800
            -0.59f,//900
    };

    public static float getModifiedHue(float hue, Shade shade) {
        float s = (float)shade.getValue();
        if (s > 500) {
            // Laurie's calculations are based on hue being in the range [0.0-1.0]
            // java has the range [0.0-360.0] so we'll do a little conversion
            hue = hue/360.0f;
            float hueAt900 = (1.003f*hue) - 0.016f;
            hue = ((hueAt900 - hue)/((float)900-(float)500))*(s-(float)500) + hue;
            return hue*360.0f;
        } else {
            return hue;
        }
    }

    public static float getModifiedSaturation(float saturation, Shade shade) {
        if (shade.getValue() == 500) {
            return saturation;
        }

        if (shade.getValue() < 500) {
            // get the saturation target @ 50:
            // clamp to 0.0
            float f = (0.136f * saturation) - 0.025f;
            float satAt50 = Math.max(f, 0.0f);
            // lerp shade 500->900
            return ((saturation - satAt50)/(500-50))*(shade.getValue()-50) + satAt50;
        } else {
            // get the saturation target @ 900:
            // quick inaccurate version:
            // 110% of the base saturation (clamped to 1.0)
            //            CGFloat satAt900 = MIN(baseSaturation * 1.10, 1.0);
            // expensive(?) accurate version
            float satAt900 = Math.min((-1.019f * saturation * saturation) + (2.283f * saturation) - 0.281f, 1.0f);
            // lerp shade 500->900
            return ((satAt900 - saturation)/(900-500))*(shade.getValue()-500) + saturation;
        }
    }


    public static float getModifiedValue(float value, Shade shade) {

        if (shade.getValue() == 500) {
            return value;
        }

        float indexFloat = ((float)shade.getValue())/100.0f;

        int indexFloor = (int)Math.floor(indexFloat);
        int indexCeil = (int)Math.ceil(indexFloat);

        int max = mValuePercentConversion.length-1;
        int lowerIndex = Math.min( Math.max(indexFloor , 0 ), max );
        int upperIndex = Math.min( Math.max(indexCeil, 0), max );

        float lowerPercent = mValuePercentConversion[lowerIndex];
        float valuePercent = 0.0f;
        if (lowerIndex != upperIndex) {
            float upperPercent = mValuePercentConversion[upperIndex];
            float deltaPercent = upperPercent-lowerPercent;
            float deltaIndex = upperIndex-lowerIndex;
            valuePercent = lowerPercent + (deltaPercent / deltaIndex) * (indexFloat-(float)lowerIndex);
        } else {
            valuePercent = lowerPercent;
        }

        if (shade.getValue() < 500) {
            return value + ((1.0f-value)*valuePercent);
        } else {
            return value + (value*valuePercent);
        }
    }

    public static SgnColor getModifiedColor(int color, Shade shade) {
        int modified = color;
        if (shade != Shade.Shade500) {
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            hsv[0] = getModifiedHue(hsv[0], shade);
            hsv[1] = getModifiedSaturation(hsv[1], shade);
            hsv[2] = getModifiedValue(hsv[2], shade);
            modified = Color.HSVToColor(Color.alpha(color), hsv);
        }
//        print(shade, color, modified);
        return new SgnColor(modified);
    }

    private static String hsvToString(float[] hsv) {
        return String.format("%.2f, %.2f, %.2f", hsv[0], hsv[1], hsv[2]);
    }

    private static void print(Shade shade, int orig, int modified) {
        float[] origHsv = new float[3];
        Color.colorToHSV(orig, origHsv);
        float[] modHsv = new float[3];
        Color.colorToHSV(modified, modHsv);
        String format = "shade: %3s, orig.hsv[%s], mod.hsv[%s]";
        SgnLog.d(TAG, String.format(format, shade.getValue(), hsvToString(origHsv), hsvToString(modHsv)));
    }

    /**
     * @param color A background color, to place the returned foreground color on top of.
     * @return A foreground text color to fit the background
     */
    public static int getPrimaryTextColor(int color) {
        return getPrimaryTextColor(isLight(color));
    }

    /**
     * @param brightBackground <code>true</code> if the background is bright, else <code>false</code>
     * @return A foreground text color to fit the background
     */
    public static int getPrimaryTextColor(boolean brightBackground) {
        int color = brightBackground ? Color.BLACK : Color.WHITE;
        int alpha = brightBackground ? TEXT_ALPHA_PRIMARY_DARK : TEXT_ALPHA_PRIMARY_LIGHT;
        return ColorUtils.setAlphaComponent(color, alpha);
    }

    /**
     * @param color A background color, to place the returned foreground color on top of.
     * @return A foreground text color to fit the background
     */
    public static int getSecondaryTextColor(int color) {
        return getSecondaryTextColor(isLight(color));
    }

    /**
     * @param brightBackground <code>true</code> if the background is bright, else <code>false</code>
     * @return A foreground text color to fit the background
     */
    public static int getSecondaryTextColor(boolean brightBackground) {
        int color = brightBackground ? Color.BLACK : Color.WHITE;
        int alpha = brightBackground ? TEXT_ALPHA_SECONDARY_DARK : TEXT_ALPHA_SECONDARY_LIGHT;
        return ColorUtils.setAlphaComponent(color, alpha);
    }

    /**
     * @param color A background color, to place the returned foreground color on top of.
     * @return A foreground text color to fit the background
     */
    public static int getDisabledTextColor(int color) {
        return getDisabledTextColor(isLight(color));
    }

    /**
     * @param brightBackground <code>true</code> if the background is bright, else <code>false</code>
     * @return A foreground text color to fit the background
     */
    public static int getDisabledTextColor(boolean brightBackground) {
        int color = brightBackground ? Color.BLACK : Color.WHITE;
        int alpha = brightBackground ? TEXT_ALPHA_DISABLED_DARK : TEXT_ALPHA_DISABLED_LIGHT;
        return ColorUtils.setAlphaComponent(color, alpha);
    }

    /** luminance value below {@link #THRESHOLD_VERY_BRIGHT} */
    public static boolean isVeryBright(int color) {
        return ColorUtils.calculateLuminance(color) > THRESHOLD_VERY_BRIGHT;
    }

    /** luminance value below {@link #THRESHOLD_BRIGHT} */
    public static boolean isBright(int color) {
        return ColorUtils.calculateLuminance(color) > THRESHOLD_BRIGHT;
    }

    /** luminance value below {@link #THRESHOLD_LIGHT} */
    public static boolean isLight(int color) {
        return ColorUtils.calculateLuminance(color) > THRESHOLD_LIGHT;
    }

    /** luminance value below {@link #THRESHOLD_VERY_DARK} */
    public static boolean isVeryDark(int color) {
        return ColorUtils.calculateLuminance(color) < THRESHOLD_VERY_DARK;
    }

    /** luminance value below {@link #THRESHOLD_DARK} */
    public static boolean isDark(int color) {
        return ColorUtils.calculateLuminance(color) < THRESHOLD_DARK;
    }

}
