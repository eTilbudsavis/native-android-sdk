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

package com.shopgun.android.sdk.test;

import android.content.res.Configuration;

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.pageflip.impl.DoublePageReaderConfig;
import com.shopgun.android.sdk.pageflip.Orientation;
import com.shopgun.android.sdk.pageflip.ReaderConfig;
import com.shopgun.android.sdk.pageflip.utils.PageflipUtils;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;

public class DoublePageReaderConfigTest extends TestCase {

    public static final String TAG = DoublePageReaderConfigTest.class.getSimpleName();

    public static void test() {

        SdkTest.start(TAG);

        DoublePageReaderConfig two = new DoublePageReaderConfig();
        two.setConfiguration(new Configuration());
        testIsLandscape(two);
        testPositionToPage(two);
        testPageToPosition(two);

    }

    public static void testIsLandscape(ReaderConfig r) {

        r.setConfiguration(null);
        assertEquals(Orientation.PORTRAIT, r.getOrientation());

        Configuration c = new Configuration();
        c.orientation = Configuration.ORIENTATION_LANDSCAPE;
        r.setConfiguration(c);
        assertTrue(r.isLandscape());

        c.orientation = Configuration.ORIENTATION_PORTRAIT;
        r.setConfiguration(c);
        assertFalse(r.isLandscape());

        c.orientation = Configuration.ORIENTATION_UNDEFINED;
        r.setConfiguration(c);
        assertFalse(r.isLandscape());

        c.orientation = Configuration.ORIENTATION_SQUARE;
        r.setConfiguration(c);
        assertFalse(r.isLandscape());

        SdkTest.logTest(TAG, (new MethodNameHelper() {
        }).getName());

    }

    public static void testPageToPosition(ReaderConfig r) {

        Configuration c = new Configuration();

        r.setHasIntro(false);
        r.setHasOutro(false);

        c.orientation = Configuration.ORIENTATION_LANDSCAPE;
        r.setConfiguration(c);
        testPageToPosition(r, 1, 0);
        testPageToPosition(r, 2, 1);
        testPageToPosition(r, 3, 1);
        testPageToPosition(r, 4, 2);
        testPageToPosition(r, 5, 2);

        c.orientation = Configuration.ORIENTATION_PORTRAIT;
        r.setConfiguration(c);
        testPageToPosition(r, 1, 0);
        testPageToPosition(r, 2, 1);
        testPageToPosition(r, 3, 2);
        testPageToPosition(r, 4, 3);

        r.setHasIntro(true);
        r.setHasOutro(false);

        c.orientation = Configuration.ORIENTATION_LANDSCAPE;
        r.setConfiguration(c);
        testPageToPosition(r, 1, 1);
        testPageToPosition(r, 2, 2);
        testPageToPosition(r, 3, 2);
        testPageToPosition(r, 4, 3);
        testPageToPosition(r, 5, 3);

        c.orientation = Configuration.ORIENTATION_PORTRAIT;
        r.setConfiguration(c);
        testPageToPosition(r, 1, 1);
        testPageToPosition(r, 2, 2);
        testPageToPosition(r, 3, 3);
        testPageToPosition(r, 4, 4);

        r.setHasIntro(false);
        r.setHasOutro(true);

        c.orientation = Configuration.ORIENTATION_LANDSCAPE;
        r.setConfiguration(c);
        testPageToPosition(r, 1, 0);
        testPageToPosition(r, 2, 1);
        testPageToPosition(r, 3, 1);
        testPageToPosition(r, 4, 2);
        testPageToPosition(r, 5, 2);

        c.orientation = Configuration.ORIENTATION_PORTRAIT;
        r.setConfiguration(c);
        testPageToPosition(r, 1, 0);
        testPageToPosition(r, 2, 1);
        testPageToPosition(r, 3, 2);
        testPageToPosition(r, 4, 3);

        r.setHasIntro(true);
        r.setHasOutro(true);

        c.orientation = Configuration.ORIENTATION_LANDSCAPE;
        r.setConfiguration(c);
        testPageToPosition(r, 1, 1);
        testPageToPosition(r, 2, 2);
        testPageToPosition(r, 3, 2);
        testPageToPosition(r, 4, 3);
        testPageToPosition(r, 5, 3);

        c.orientation = Configuration.ORIENTATION_PORTRAIT;
        r.setConfiguration(c);
        testPageToPosition(r, 1, 1);
        testPageToPosition(r, 2, 2);
        testPageToPosition(r, 3, 3);
        testPageToPosition(r, 4, 4);

        SdkTest.logTest(TAG, (new MethodNameHelper() {
        }).getName());

    }

    private static void testPageToPosition(ReaderConfig config, int page, int expectedPos) {
        Assert.assertEquals(expectedPos, config.pageToPosition(page));
    }

    public static void testPositionToPage(ReaderConfig r) {

        // Setup the test
        int PAGE_COUNT_LANDSCAPE = 8;
        ArrayList<ConfigTestCase> landscape = new ArrayList<ConfigTestCase>();

        int PAGE_COUNT_PORTRAIT = 4;
        ArrayList<ConfigTestCase> portrait = new ArrayList<ConfigTestCase>();

        Configuration c = new Configuration();

        // Create a test case for no intro, no outro
        r.setHasIntro(false);
        r.setHasOutro(false);
        landscape.clear();
        landscape.add(new ConfigTestCase(0, PAGE_COUNT_LANDSCAPE, new int[]{1}));
        landscape.add(new ConfigTestCase(1, PAGE_COUNT_LANDSCAPE, new int[]{2, 3}));
        landscape.add(new ConfigTestCase(2, PAGE_COUNT_LANDSCAPE, new int[]{4, 5}));
        landscape.add(new ConfigTestCase(3, PAGE_COUNT_LANDSCAPE, new int[]{6, 7}));
        landscape.add(new ConfigTestCase(4, PAGE_COUNT_LANDSCAPE, new int[]{8}));
        portrait.clear();
        portrait.add(new ConfigTestCase(0, PAGE_COUNT_PORTRAIT, new int[]{1}));
        portrait.add(new ConfigTestCase(1, PAGE_COUNT_PORTRAIT, new int[]{2}));
        portrait.add(new ConfigTestCase(2, PAGE_COUNT_PORTRAIT, new int[]{3}));
        portrait.add(new ConfigTestCase(3, PAGE_COUNT_PORTRAIT, new int[]{4}));
        portrait.add(new ConfigTestCase(4, PAGE_COUNT_PORTRAIT, new int[]{5}));

        c.orientation = Configuration.ORIENTATION_LANDSCAPE;
        r.setConfiguration(c);
        for (ConfigTestCase testCase : landscape) {
            testPositionToPage(r, testCase);
        }

        c.orientation = Configuration.ORIENTATION_PORTRAIT;
        r.setConfiguration(c);
        for (ConfigTestCase testCase : portrait) {
            testPositionToPage(r, testCase);
        }

        // Create a test case with intro, without outro
        r.setHasIntro(true);
        r.setHasOutro(false);
        landscape.clear();
        landscape.add(new ConfigTestCase(0, PAGE_COUNT_LANDSCAPE, new int[]{}));
        landscape.add(new ConfigTestCase(1, PAGE_COUNT_LANDSCAPE, new int[]{1}));
        landscape.add(new ConfigTestCase(2, PAGE_COUNT_LANDSCAPE, new int[]{2, 3}));
        landscape.add(new ConfigTestCase(3, PAGE_COUNT_LANDSCAPE, new int[]{4, 5}));
        landscape.add(new ConfigTestCase(4, PAGE_COUNT_LANDSCAPE, new int[]{6, 7}));
        landscape.add(new ConfigTestCase(5, PAGE_COUNT_LANDSCAPE, new int[]{8}));
        portrait.clear();
        portrait.add(new ConfigTestCase(0, PAGE_COUNT_PORTRAIT, new int[]{}));
        portrait.add(new ConfigTestCase(1, PAGE_COUNT_PORTRAIT, new int[]{1}));
        portrait.add(new ConfigTestCase(2, PAGE_COUNT_PORTRAIT, new int[]{2}));
        portrait.add(new ConfigTestCase(3, PAGE_COUNT_PORTRAIT, new int[]{3}));
        portrait.add(new ConfigTestCase(4, PAGE_COUNT_PORTRAIT, new int[]{4}));
        portrait.add(new ConfigTestCase(5, PAGE_COUNT_PORTRAIT, new int[]{5}));

        c.orientation = Configuration.ORIENTATION_LANDSCAPE;
        r.setConfiguration(c);
        for (ConfigTestCase testCase : landscape) {
            testPositionToPage(r, testCase);
        }

        c.orientation = Configuration.ORIENTATION_PORTRAIT;
        r.setConfiguration(c);
        for (ConfigTestCase testCase : portrait) {
            testPositionToPage(r, testCase);
        }

        // Create a test case without intro, with outro
        r.setHasIntro(false);
        r.setHasOutro(true);
        landscape.clear();
        landscape.add(new ConfigTestCase(0, PAGE_COUNT_LANDSCAPE, new int[]{1}));
        landscape.add(new ConfigTestCase(1, PAGE_COUNT_LANDSCAPE, new int[]{2, 3}));
        landscape.add(new ConfigTestCase(2, PAGE_COUNT_LANDSCAPE, new int[]{4, 5}));
        landscape.add(new ConfigTestCase(3, PAGE_COUNT_LANDSCAPE, new int[]{6, 7}));
        landscape.add(new ConfigTestCase(4, PAGE_COUNT_LANDSCAPE, new int[]{8}));
        landscape.add(new ConfigTestCase(5, PAGE_COUNT_LANDSCAPE, new int[]{}));
        portrait.clear();
        portrait.add(new ConfigTestCase(0, PAGE_COUNT_PORTRAIT, new int[]{1}));
        portrait.add(new ConfigTestCase(1, PAGE_COUNT_PORTRAIT, new int[]{2}));
        portrait.add(new ConfigTestCase(2, PAGE_COUNT_PORTRAIT, new int[]{3}));
        portrait.add(new ConfigTestCase(3, PAGE_COUNT_PORTRAIT, new int[]{4}));
        portrait.add(new ConfigTestCase(4, PAGE_COUNT_PORTRAIT, new int[]{}));

        c.orientation = Configuration.ORIENTATION_LANDSCAPE;
        r.setConfiguration(c);
        for (ConfigTestCase testCase : landscape) {
            testPositionToPage(r, testCase);
        }

        c.orientation = Configuration.ORIENTATION_PORTRAIT;
        r.setConfiguration(c);
        for (ConfigTestCase testCase : portrait) {
            testPositionToPage(r, testCase);
        }


        // Create a test case with intro, without outro
        r.setHasIntro(true);
        r.setHasOutro(true);
        landscape.clear();
        landscape.add(new ConfigTestCase(0, PAGE_COUNT_LANDSCAPE, new int[]{}));
        landscape.add(new ConfigTestCase(1, PAGE_COUNT_LANDSCAPE, new int[]{1}));
        landscape.add(new ConfigTestCase(2, PAGE_COUNT_LANDSCAPE, new int[]{2, 3}));
        landscape.add(new ConfigTestCase(3, PAGE_COUNT_LANDSCAPE, new int[]{4, 5}));
        landscape.add(new ConfigTestCase(4, PAGE_COUNT_LANDSCAPE, new int[]{6, 7}));
        landscape.add(new ConfigTestCase(5, PAGE_COUNT_LANDSCAPE, new int[]{8}));
        landscape.add(new ConfigTestCase(6, PAGE_COUNT_LANDSCAPE, new int[]{}));
        portrait.clear();
        portrait.add(new ConfigTestCase(0, PAGE_COUNT_PORTRAIT, new int[]{}));
        portrait.add(new ConfigTestCase(1, PAGE_COUNT_PORTRAIT, new int[]{1}));
        portrait.add(new ConfigTestCase(2, PAGE_COUNT_PORTRAIT, new int[]{2}));
        portrait.add(new ConfigTestCase(3, PAGE_COUNT_PORTRAIT, new int[]{3}));
        portrait.add(new ConfigTestCase(4, PAGE_COUNT_PORTRAIT, new int[]{4}));
        portrait.add(new ConfigTestCase(5, PAGE_COUNT_PORTRAIT, new int[]{}));

        c.orientation = Configuration.ORIENTATION_LANDSCAPE;
        r.setConfiguration(c);
        for (ConfigTestCase testCase : landscape) {
            testPositionToPage(r, testCase);
        }

        c.orientation = Configuration.ORIENTATION_PORTRAIT;
        r.setConfiguration(c);
        for (ConfigTestCase testCase : portrait) {
            testPositionToPage(r, testCase);
        }


        SdkTest.logTest(TAG, (new MethodNameHelper() {
        }).getName());

    }

    private static void testPositionToPage(ReaderConfig config, ConfigTestCase testCase) {
        int[] pages = config.positionToPages(testCase.pos, testCase.pageCount);
        boolean eq = Arrays.equals(pages, testCase.expectedPages);
        if (!eq) {
            SgnLog.d(TAG, "ReaderConfig:    " + config.toString());
            SgnLog.d(TAG, "ConfigTestCase:  " + testCase.toString());
            SgnLog.d(TAG, "positionToPages: " + PageflipUtils.join(",", pages));
        }
        Assert.assertTrue(eq);
    }

    private static class ConfigTestCase {
        int pos;
        int pageCount;
        int[] expectedPages;

        public ConfigTestCase(int pos, int pageCount, int[] expectedPages) {
            this.pos = pos;
            this.pageCount = pageCount;
            this.expectedPages = expectedPages;
        }

        @Override
        public String toString() {
            return String.format("%s[pos:%s, pageCount:%s, expectedPages:%s]", ConfigTestCase.class.getSimpleName(), pos, pageCount, PageflipUtils.join(",", expectedPages));
        }
    }
}
