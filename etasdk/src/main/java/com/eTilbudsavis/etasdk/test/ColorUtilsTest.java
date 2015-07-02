package com.eTilbudsavis.etasdk.test;

import android.graphics.Color;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.utils.ColorUtils;

import junit.framework.Assert;

public class ColorUtilsTest {

    public static final String TAG = Constants.getTag(ColorUtilsTest.class);

    private ColorUtilsTest() {
        // empty
    }

    public static void test() {

        EtaSdkTest.start(TAG);
        testApplyAlpha();
        testColorToString(false);
        testStringToColor();
        testColorSanitize(false);

    }

    public static void testApplyAlpha() {

        int whiteTransparent = 0x00ffffff;
        int whiteOpaque = 0xffffffff;

        // max alpha = 255 or 0b11111111
        int maxAlpha = 0xff;
        Assert.assertNotSame(whiteOpaque, whiteTransparent);
        Assert.assertNotSame(whiteTransparent, Color.WHITE);
        Assert.assertEquals(whiteOpaque, Color.WHITE);
        Assert.assertTrue(ColorUtils.applyAlpha(whiteOpaque, maxAlpha) == Color.WHITE);
        Assert.assertTrue(ColorUtils.applyAlpha(whiteTransparent, maxAlpha) == Color.WHITE);

        int badAlpha = 0x100; // 256 or 0b100000000
        int c = ColorUtils.applyAlpha(whiteTransparent, badAlpha);
        // Even though we have a large number to represent alpha
        // the first 8 bits are 0 and therefore useless, so no alpha will be applied
        Assert.assertEquals(c, whiteTransparent);
        EtaSdkTest.logTest(TAG, (new MethodNameHelper() {
        }).getName());
    }

    public static void testColorToString(boolean showWarnings) {

        Assert.assertEquals("000000", ColorUtils.toString(Color.BLACK));
        Assert.assertEquals("FFFFFF", ColorUtils.toString(Color.WHITE));
        Assert.assertEquals("FF0000", ColorUtils.toString(Color.RED));
        Assert.assertEquals("00FF00", ColorUtils.toString(Color.GREEN));
        Assert.assertEquals("0000FF", ColorUtils.toString(Color.BLUE));
        Assert.assertEquals(null, ColorUtils.toString(null));

        Assert.assertNotSame("0000FF", ColorUtils.toString(Color.LTGRAY));
        Assert.assertNotSame("0000FF", ColorUtils.toString(Color.CYAN));
        Assert.assertNotSame("0000FF", ColorUtils.toString(Color.MAGENTA));
        Assert.assertNotSame("0000FF", ColorUtils.toString(Color.YELLOW));

        Assert.assertNotSame("thisColor", ColorUtils.toString(null));

        Assert.assertNotSame("danny", ColorUtils.toString(24, showWarnings));
        Assert.assertNotSame("bente", ColorUtils.toString(78, showWarnings));
        Assert.assertNotSame(null, ColorUtils.toString(78, showWarnings));
        Assert.assertNotSame("", ColorUtils.toString(78, showWarnings));
        Assert.assertEquals("000000", ColorUtils.toString(Color.TRANSPARENT, showWarnings));
        Assert.assertNotSame("00000000", ColorUtils.toString(Color.TRANSPARENT, showWarnings));

        // null cases
        String colorString = ColorUtils.toString(null, showWarnings, true);
        Assert.assertEquals(null, colorString);
        colorString = ColorUtils.toString(null, showWarnings, false);
        Assert.assertEquals(null, colorString);

        // White cases
        colorString = ColorUtils.toString(Color.WHITE, showWarnings, false);
        Assert.assertEquals("#FFFFFFFF", colorString);
        colorString = ColorUtils.toString(Color.WHITE, showWarnings, true);
        Assert.assertEquals("FFFFFF", colorString);

        // Black cases
        colorString = ColorUtils.toString(Color.BLACK, showWarnings, false);
        Assert.assertEquals("#FF000000", colorString);
        colorString = ColorUtils.toString(Color.BLACK, showWarnings, true);
        Assert.assertNotSame("000000", colorString);

        EtaSdkTest.logTest(TAG, (new MethodNameHelper() {
        }).getName());
    }

    public static void testStringToColor() {


        Assert.assertEquals(Integer.valueOf(Color.BLACK), ColorUtils.toColor("000000"));
        Assert.assertEquals(Integer.valueOf(Color.WHITE), ColorUtils.toColor("FFFFFF"));
        Assert.assertEquals(Integer.valueOf(Color.RED), ColorUtils.toColor("FF0000"));
        Assert.assertEquals(Integer.valueOf(Color.GREEN), ColorUtils.toColor("00FF00"));
        Assert.assertEquals(Integer.valueOf(Color.BLUE), ColorUtils.toColor("0000FF"));

        Assert.assertNotSame(Color.BLACK, ColorUtils.toColor("00FFF0"));
        Assert.assertNotSame(1, ColorUtils.toColor("00FFF0"));
        Assert.assertNotSame(Color.MAGENTA, ColorUtils.toColor("000000"));
        Assert.assertNotSame(Color.MAGENTA, ColorUtils.toColor(""));
        Assert.assertNotSame(Color.MAGENTA, ColorUtils.toColor(null));

        EtaSdkTest.logTest(TAG, (new MethodNameHelper() {
        }).getName());
    }

    public static void testColorSanitize(boolean showWarnings) {

        Assert.assertTrue(null == ColorUtils.stripAlpha(null));

        Assert.assertTrue(Color.BLUE == ColorUtils.stripAlpha(Color.BLUE));
        Assert.assertTrue(Color.RED == ColorUtils.stripAlpha(Color.RED));
        Assert.assertTrue(Color.GREEN == ColorUtils.stripAlpha(Color.GREEN));

        // Removing transparency
        Assert.assertFalse(Color.TRANSPARENT == ColorUtils.stripAlpha(Color.TRANSPARENT, showWarnings));
        Assert.assertTrue(Color.BLACK == ColorUtils.stripAlpha(Color.TRANSPARENT, showWarnings));

        int alphaRed = Color.parseColor("#55FF0000");
        Assert.assertFalse(alphaRed == ColorUtils.stripAlpha(alphaRed, showWarnings));

        EtaSdkTest.logTest(TAG, (new MethodNameHelper() {
        }).getName());
    }

}
