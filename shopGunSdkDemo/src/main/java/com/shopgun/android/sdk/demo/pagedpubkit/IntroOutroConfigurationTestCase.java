package com.shopgun.android.sdk.demo.pagedpubkit;

import android.view.View;
import android.view.ViewGroup;

import com.shopgun.android.sdk.pagedpublicationkit.impl.IntroOutroConfiguration;
import com.shopgun.android.utils.TextUtils;
import com.shopgun.android.utils.enums.Orientation;
import com.shopgun.android.verso.VersoSpreadProperty;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import java.util.Arrays;

public class IntroOutroConfigurationTestCase {

    public static void test() {
        testNone();
        testIntro();
        testOutro();
        testIntroOutro();
        testIntroMissingBack();
        testOutroMissingBack();
        testIntroOutroMissingBack();
    }

    private static void testNone() {

        IntroOutroConfiguration config = new TestIntroOutroConfiguration(4, Orientation.LANDSCAPE, false, false);

        Assert.assertEquals(4, config.getPageCount());

        Assert.assertEquals(3, config.getSpreadCount());

        assertEquals(new int[]{ 0 }, config.getPagesFromSpreadPosition(0));
        assertEquals(new int[]{ 1, 2 }, config.getPagesFromSpreadPosition(1));
        assertEquals(new int[]{ 3 }, config.getPagesFromSpreadPosition(2));

        Assert.assertEquals(0 , config.getSpreadPositionFromPage(0));
        Assert.assertEquals(1 , config.getSpreadPositionFromPage(1));
        Assert.assertEquals(1 , config.getSpreadPositionFromPage(2));
        Assert.assertEquals(2 , config.getSpreadPositionFromPage(3));

        try {
            config.getIntroSpreadOverlay(null, new int[]{ 0 });
            throw new AssertionTryError();
        } catch (AssertionFailedError e) {
            // this is good
        }

        try {
            config.getIntroPageView(null, 0);
            throw new AssertionTryError();
        } catch (AssertionFailedError e) {
            // this is good
        }

        try {
            config.getIntroSpreadProperty(0, new int[]{ 0 });
            throw new AssertionTryError();
        } catch (AssertionFailedError e) {
            // this is good
        }

        try {
            config.getOutroSpreadOverlay(null, new int[]{ 0 });
            throw new AssertionTryError();
        } catch (AssertionFailedError e) {
            // this is good
        }

        try {
            config.getOutroPageView(null, 0);
            throw new AssertionTryError();
        } catch (AssertionFailedError e) {
            // this is good
        }

        try {
            config.getOutroSpreadProperty(0, new int[]{ 0 });
            throw new AssertionTryError();
        } catch (AssertionFailedError e) {
            // this is good
        }

    }

    private static void testIntro() {

        IntroOutroConfiguration config = new TestIntroOutroConfiguration(4, Orientation.LANDSCAPE, true, false);

        Assert.assertEquals(5, config.getPageCount());

        Assert.assertEquals(4, config.getSpreadCount());

        assertEquals(new int[]{ 0 }, config.getPagesFromSpreadPosition(0));
        assertEquals(new int[]{ 1 }, config.getPagesFromSpreadPosition(1));
        assertEquals(new int[]{ 2, 3 }, config.getPagesFromSpreadPosition(2));
        assertEquals(new int[]{ 4 }, config.getPagesFromSpreadPosition(3));

        Assert.assertEquals(0 , config.getSpreadPositionFromPage(0));
        Assert.assertEquals(1 , config.getSpreadPositionFromPage(1));
        Assert.assertEquals(2 , config.getSpreadPositionFromPage(2));
        Assert.assertEquals(2 , config.getSpreadPositionFromPage(3));
        Assert.assertEquals(3 , config.getSpreadPositionFromPage(4));

        config.getIntroSpreadOverlay(null, new int[]{ 0 });
        config.getIntroPageView(null, 0);
        config.getIntroSpreadProperty(0, new int[]{ 0 });

        try {
            config.getOutroSpreadOverlay(null, new int[]{ 0 });
            throw new AssertionTryError();
        } catch (AssertionFailedError e) {
            // this is good
        }

        try {
            config.getOutroPageView(null, 0);
            throw new AssertionTryError();
        } catch (AssertionFailedError e) {
            // this is good
        }

        try {
            config.getOutroSpreadProperty(0, new int[]{ 0 });
            throw new AssertionTryError();
        } catch (AssertionFailedError e) {
            // this is good
        }

    }

    private static void testOutro() {

        IntroOutroConfiguration config = new TestIntroOutroConfiguration(4, Orientation.LANDSCAPE, false, true);

        Assert.assertEquals(5, config.getPageCount());

        Assert.assertEquals(4, config.getSpreadCount());

        assertEquals(new int[]{ 0 }, config.getPagesFromSpreadPosition(0));
        assertEquals(new int[]{ 1, 2 }, config.getPagesFromSpreadPosition(1));
        assertEquals(new int[]{ 3 }, config.getPagesFromSpreadPosition(2));
        assertEquals(new int[]{ 4 }, config.getPagesFromSpreadPosition(3));

        Assert.assertEquals(0 , config.getSpreadPositionFromPage(0));
        Assert.assertEquals(1 , config.getSpreadPositionFromPage(1));
        Assert.assertEquals(1 , config.getSpreadPositionFromPage(2));
        Assert.assertEquals(2 , config.getSpreadPositionFromPage(3));
        Assert.assertEquals(3 , config.getSpreadPositionFromPage(4));

        try {
            config.getIntroSpreadOverlay(null, new int[]{ 0 });
            throw new AssertionTryError();
        } catch (AssertionFailedError e) {
            // this is good
        }

        try {
            config.getIntroPageView(null, 0);
            throw new AssertionTryError();
        } catch (AssertionFailedError e) {
            // this is good
        }

        try {
            config.getIntroSpreadProperty(0, new int[]{ 0 });
            throw new AssertionTryError();
        } catch (AssertionFailedError e) {
            // this is good
        }

        config.getOutroSpreadOverlay(null, new int[]{ 0 });
        config.getOutroPageView(null, 0);
        config.getOutroSpreadProperty(0, new int[]{ 0 });

    }

    private static void testIntroOutro() {

        IntroOutroConfiguration config = new TestIntroOutroConfiguration(4, Orientation.LANDSCAPE, true, true);

        Assert.assertEquals(6, config.getPageCount());

        Assert.assertEquals(5, config.getSpreadCount());

        assertEquals(new int[]{ 0 }, config.getPagesFromSpreadPosition(0));
        assertEquals(new int[]{ 1 }, config.getPagesFromSpreadPosition(1));
        assertEquals(new int[]{ 2, 3 }, config.getPagesFromSpreadPosition(2));
        assertEquals(new int[]{ 4 }, config.getPagesFromSpreadPosition(3));
        assertEquals(new int[]{ 5 }, config.getPagesFromSpreadPosition(4));

        Assert.assertEquals(0 , config.getSpreadPositionFromPage(0));
        Assert.assertEquals(1 , config.getSpreadPositionFromPage(1));
        Assert.assertEquals(2 , config.getSpreadPositionFromPage(2));
        Assert.assertEquals(2 , config.getSpreadPositionFromPage(3));
        Assert.assertEquals(3 , config.getSpreadPositionFromPage(4));
        Assert.assertEquals(4 , config.getSpreadPositionFromPage(5));

        config.getIntroSpreadOverlay(null, new int[]{ 0 });
        config.getIntroPageView(null, 0);
        config.getIntroSpreadProperty(0, new int[]{ 0 });

        config.getOutroSpreadOverlay(null, new int[]{ 0 });
        config.getOutroPageView(null, 0);
        config.getOutroSpreadProperty(0, new int[]{ 0 });

    }

    private static void testIntroMissingBack() {

        IntroOutroConfiguration config = new TestIntroOutroConfiguration(3, Orientation.LANDSCAPE, true, false);

        Assert.assertEquals(4, config.getPageCount());

        Assert.assertEquals(3, config.getSpreadCount());

        assertEquals(new int[]{ 0 }, config.getPagesFromSpreadPosition(0));
        assertEquals(new int[]{ 1 }, config.getPagesFromSpreadPosition(1));
        assertEquals(new int[]{ 2, 3 }, config.getPagesFromSpreadPosition(2));

        Assert.assertEquals(0 , config.getSpreadPositionFromPage(0));
        Assert.assertEquals(1 , config.getSpreadPositionFromPage(1));
        Assert.assertEquals(2 , config.getSpreadPositionFromPage(2));
        Assert.assertEquals(2 , config.getSpreadPositionFromPage(3));

        config.getIntroSpreadOverlay(null, new int[]{ 0 });
        config.getIntroPageView(null, 0);
        config.getIntroSpreadProperty(0, new int[]{ 0 });

        try {
            config.getOutroSpreadOverlay(null, new int[]{ 0 });
            throw new AssertionTryError();
        } catch (AssertionFailedError e) {
            // this is good
        }

        try {
            config.getOutroPageView(null, 0);
            throw new AssertionTryError();
        } catch (AssertionFailedError e) {
            // this is good
        }

        try {
            config.getOutroSpreadProperty(0, new int[]{ 0 });
            throw new AssertionTryError();
        } catch (AssertionFailedError e) {
            // this is good
        }

    }

    private static void testOutroMissingBack() {

        IntroOutroConfiguration config = new TestIntroOutroConfiguration(3, Orientation.LANDSCAPE, false, true);

        Assert.assertEquals(4, config.getPageCount());

        Assert.assertEquals(3, config.getSpreadCount());

        assertEquals(new int[]{ 0 }, config.getPagesFromSpreadPosition(0));
        assertEquals(new int[]{ 1, 2 }, config.getPagesFromSpreadPosition(1));
        assertEquals(new int[]{ 3 }, config.getPagesFromSpreadPosition(2));

        Assert.assertEquals(0 , config.getSpreadPositionFromPage(0));
        Assert.assertEquals(1 , config.getSpreadPositionFromPage(1));
        Assert.assertEquals(1 , config.getSpreadPositionFromPage(2));
        Assert.assertEquals(2 , config.getSpreadPositionFromPage(3));

        try {
            config.getIntroSpreadOverlay(null, new int[]{ 0 });
            throw new AssertionTryError();
        } catch (AssertionFailedError e) {
            // this is good
        }

        try {
            config.getIntroPageView(null, 0);
            throw new AssertionTryError();
        } catch (AssertionFailedError e) {
            // this is good
        }

        try {
            config.getIntroSpreadProperty(0, new int[]{ 0 });
            throw new AssertionTryError();
        } catch (AssertionFailedError e) {
            // this is good
        }

        config.getOutroSpreadOverlay(null, new int[]{ 0 });
        config.getOutroPageView(null, 0);
        config.getOutroSpreadProperty(0, new int[]{ 0 });

    }

    private static void testIntroOutroMissingBack() {

        IntroOutroConfiguration config = new TestIntroOutroConfiguration(3, Orientation.LANDSCAPE, true, true);

        Assert.assertEquals(5, config.getPageCount());

        Assert.assertEquals(4, config.getSpreadCount());

        assertEquals(new int[]{ 0 }, config.getPagesFromSpreadPosition(0));
        assertEquals(new int[]{ 1 }, config.getPagesFromSpreadPosition(1));
        assertEquals(new int[]{ 2, 3 }, config.getPagesFromSpreadPosition(2));
        assertEquals(new int[]{ 4 }, config.getPagesFromSpreadPosition(3));

        Assert.assertEquals(0 , config.getSpreadPositionFromPage(0));
        Assert.assertEquals(1 , config.getSpreadPositionFromPage(1));
        Assert.assertEquals(2 , config.getSpreadPositionFromPage(2));
        Assert.assertEquals(2 , config.getSpreadPositionFromPage(3));
        Assert.assertEquals(3 , config.getSpreadPositionFromPage(4));

        config.getIntroSpreadOverlay(null, new int[]{ 0 });
        config.getIntroPageView(null, 0);
        config.getIntroSpreadProperty(0, new int[]{ 0 });

        config.getOutroSpreadOverlay(null, new int[]{ 0 });
        config.getOutroPageView(null, 0);
        config.getOutroSpreadProperty(0, new int[]{ 0 });

    }

    public static void assertEquals(int[] expected, int[] actual) {
        Assert.assertTrue(format(expected, actual), Arrays.equals(expected, actual));
    }

    public static class AssertionTryError extends AssertionError {
        public AssertionTryError() {
            super("Code in Try block must fail");
        }
    }

    public static String format(int[] expected, int[] actual) {
        return "expected:<"+TextUtils.join(expected)+"> but was:<"+TextUtils.join(actual)+">";
    }

}
