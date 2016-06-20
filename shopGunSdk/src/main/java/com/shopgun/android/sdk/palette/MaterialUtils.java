package com.shopgun.android.sdk.palette;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.utils.ColorUtils;

public class MaterialUtils {

    public static final String TAG = Constants.getTag(MaterialUtils.class);

    private MaterialUtils() {
        // private
    }

    public static String toARGBString(MaterialColor color) {
        return color == null ? null : ColorUtils.toARGBString(color.getValue());
    }

    public static String toHsvString(MaterialColor color) {
        return ColorUtils.toHsvString(ColorUtils.toHSV(color.getValue()));
    }

    /**
     * Returns the luminance of a color.
     *
     * @param color A color to test
     * @see {@link ColorUtils#calculateLuminance(int)}
     */
    public static double calculateLuminance(MaterialColor color) {
        return ColorUtils.calculateLuminance(color.getValue());
    }

    /**
     * Set the alpha component of {@code color} to be {@code alpha}.
     *
     * @param color A color
     * @param alpha The new alpha value for the color, in the range 0 - 255.
     * @return A the given {@code color}, with the given {@code alpha}
     * @see {@link ColorUtils#setAlphaComponent(int, int)}
     */
    public static MaterialColor setAlphaComponent(MaterialColor color, int alpha) {
        return new SgnColor(ColorUtils.setAlphaComponent(color.getValue(), alpha));
    }

    /**
     * Returns the contrast ratio between {@code foreground} and {@code background}.
     * {@code background} must be opaque.
     * <p>
     * @see {@link ColorUtils#calculateContrast(int, int)}
     */
    public static double calculateContrast(MaterialColor foreground, MaterialColor background) {
        return ColorUtils.calculateContrast(foreground.getValue(), background.getValue());
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
     * @see {@link ColorUtils#calculateMinimumAlpha(int, int, float)}
     */
    public static int calculateMinimumAlpha(MaterialColor foreground, MaterialColor background, float minContrastRatio) {
        return ColorUtils.calculateMinimumAlpha(foreground.getValue(), background.getValue(), minContrastRatio);
    }

}
