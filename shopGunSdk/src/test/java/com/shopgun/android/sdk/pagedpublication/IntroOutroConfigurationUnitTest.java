package com.shopgun.android.sdk.pagedpublication;

import com.shopgun.android.sdk.pagedpublicationkit.impl.IntroOutroConfiguration;
import com.shopgun.android.utils.TextUtils;
import com.shopgun.android.utils.enums.Orientation;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;


@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class IntroOutroConfigurationUnitTest {

    public static final String TAG = IntroOutroConfigurationUnitTest.class.getSimpleName();

    @Test
    public void testConfigLandscape() throws Exception {

        int[][] none = new int[][]{ new int[]{ 0 }, new int[]{ 1, 2 }, new int[]{ 3 } };
        runConfigTest(new MockSpread(false, false, none),
                new MockIntroOutroConfiguration(4, Orientation.LANDSCAPE, false, false));

        int[][] intro = new int[][]{ new int[]{ 0 }, new int[]{ 1 }, new int[]{ 2, 3 }, new int[]{ 4 } };
        runConfigTest(new MockSpread(true, false, intro),
                new MockIntroOutroConfiguration(4, Orientation.LANDSCAPE, true, false));

        int[][] outro = new int[][]{ new int[]{ 0 }, new int[]{ 1, 2 }, new int[]{ 3 }, new int[]{ 4 } };
        runConfigTest(new MockSpread(false, true, outro),
                new MockIntroOutroConfiguration(4, Orientation.LANDSCAPE, false, true));

        int[][] introOutro = new int[][]{ new int[]{ 0 }, new int[]{ 1 }, new int[]{ 2, 3 }, new int[]{ 4 }, new int[]{ 5 } };
        runConfigTest(new MockSpread(true, true, introOutro),
                new MockIntroOutroConfiguration(4, Orientation.LANDSCAPE, true, true));

    }

    @Test
    public void testConfigPortrait() throws Exception {

        int[][] none = new int[][]{ new int[]{ 0 }, new int[]{ 1 } ,  new int[]{ 2 }, new int[]{ 3 } };
        runConfigTest(new MockSpread(false, false, none),
                new MockIntroOutroConfiguration(4, Orientation.PORTRAIT, false, false));

        int[][] intro = new int[][]{ new int[]{ 0 }, new int[]{ 1 }, new int[]{ 2 }, new int[]{ 3 }, new int[]{ 4 } };
        runConfigTest(new MockSpread(true, false, intro),
                new MockIntroOutroConfiguration(4, Orientation.PORTRAIT, true, false));

        int[][] outro = new int[][]{ new int[]{ 0 }, new int[]{ 1 }, new int[]{ 2 }, new int[]{ 3 }, new int[]{ 4 } };
        runConfigTest(new MockSpread(false, true, outro),
                new MockIntroOutroConfiguration(4, Orientation.PORTRAIT, false, true));

        int[][] introOutro = new int[][]{ new int[]{ 0 }, new int[]{ 1 }, new int[]{ 2 }, new int[]{ 3 }, new int[]{ 4 }, new int[]{ 5 } };
        runConfigTest(new MockSpread(true, true, introOutro),
                new MockIntroOutroConfiguration(4, Orientation.PORTRAIT, true, true));

    }

    @Test
    public void testConfigLandscapeWithMissingBack() throws Exception {

        int[][] none = new int[][]{ new int[]{ 0 }, new int[]{ 1, 2 } };
        runConfigTest(new MockSpread(false, false, none),
                new MockIntroOutroConfiguration(3, Orientation.LANDSCAPE, false, false));

        int[][] intro = new int[][]{ new int[]{ 0 }, new int[]{ 1 }, new int[]{ 2, 3 } };
        runConfigTest(new MockSpread(true, false, intro),
                new MockIntroOutroConfiguration(3, Orientation.LANDSCAPE, true, false));

        int[][] outro = new int[][]{ new int[]{ 0 }, new int[]{ 1, 2 }, new int[]{ 3 } };
        runConfigTest(new MockSpread(false, true, outro),
                new MockIntroOutroConfiguration(3, Orientation.LANDSCAPE, false, true));

        int[][] introOutro = new int[][]{ new int[]{ 0 }, new int[]{ 1 }, new int[]{ 2, 3 }, new int[]{ 4 } };
        runConfigTest(new MockSpread(true, true, introOutro),
                new MockIntroOutroConfiguration(3, Orientation.LANDSCAPE, true, true));

    }

    @Test
    public void testConfigPortraitWithMissingBack() throws Exception {

        int[][] none = new int[][]{ new int[]{ 0 }, new int[]{ 1 }, new int[]{ 2 } };
        runConfigTest(new MockSpread(false, false, none),
                new MockIntroOutroConfiguration(3, Orientation.PORTRAIT, false, false));

        int[][] intro = new int[][]{ new int[]{ 0 }, new int[]{ 1 }, new int[]{ 2 }, new int[]{ 3 } };
        runConfigTest(new MockSpread(true, false, intro),
                new MockIntroOutroConfiguration(3, Orientation.PORTRAIT, true, false));

        int[][] outro = new int[][]{ new int[]{ 0 }, new int[]{ 1 }, new int[]{ 2 }, new int[]{ 3 } };
        runConfigTest(new MockSpread(false, true, outro),
                new MockIntroOutroConfiguration(3, Orientation.PORTRAIT, false, true));

        int[][] introOutro = new int[][]{ new int[]{ 0 }, new int[]{ 1 }, new int[]{ 2 }, new int[]{ 3 }, new int[]{ 4 } };
        runConfigTest(new MockSpread(true, true, introOutro),
                new MockIntroOutroConfiguration(3, Orientation.PORTRAIT, true, true));

    }

    private static void runConfigTest(MockSpread spread, IntroOutroConfiguration config) {

        Assert.assertEquals(spread.pageCount, config.getPageCount());
        Assert.assertEquals(spread.spreadCount, config.getSpreadCount());

        for (int i = 0; i < spread.spreadCount; i++) {
            int[] pages = spread.spreads[i];
            assertEquals(pages, config.getPagesFromSpreadPosition(i));
            for (int j = 0; j < spread.spreads[i].length; j++) {
                int page = spread.spreads[i][j];
                Assert.assertEquals(i, config.getSpreadPositionFromPage(page));
            }
        }

    }

    private static void assertEquals(int[] expected, int[] actual) {
        Assert.assertTrue(
                "expected:<"+TextUtils.join(expected)+"> but was:<"+TextUtils.join(actual)+">",
                Arrays.equals(expected, actual));
    }

    private static class MockSpread {

        boolean intro;
        boolean outro;
        int[][] spreads;
        int spreadCount;
        int pageCount;

        MockSpread(boolean intro, boolean outro, int[][] spreads) {
            this.intro = intro;
            this.outro = outro;
            this.spreads = spreads;
            this.spreadCount = spreads.length;
            for (int[] ii : spreads) this.pageCount += ii.length;
        }

    }

}
