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

import com.shopgun.android.sdk.pageflip.DoublePageReaderConfig;
import com.shopgun.android.sdk.pageflip.ReaderConfig;

import junit.framework.Assert;
import junit.framework.TestCase;

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
        assertFalse(r.isLandscape());

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

        SdkTest.logTest(TAG, (new MethodNameHelper() {
        }).getName());

    }

    private static void testPageToPosition(ReaderConfig config, int page, int expectedPos) {
        Assert.assertEquals(expectedPos, config.pageToPosition(page));
    }

    public static void testPositionToPage(ReaderConfig r) {

        Configuration c = new Configuration();
        c.orientation = Configuration.ORIENTATION_LANDSCAPE;
        r.setConfiguration(c);

        int PAGE_COUNT = 8;
        testPositionToPage(r, 0, PAGE_COUNT, new int[]{1});
        testPositionToPage(r, 1, PAGE_COUNT, new int[]{2, 3});
        testPositionToPage(r, 2, PAGE_COUNT, new int[]{4, 5});
        testPositionToPage(r, 3, PAGE_COUNT, new int[]{6, 7});
        testPositionToPage(r, 4, PAGE_COUNT, new int[]{8});

        c.orientation = Configuration.ORIENTATION_PORTRAIT;
        r.setConfiguration(c);

        PAGE_COUNT = 4;
        testPositionToPage(r, 0, PAGE_COUNT, new int[]{1});
        testPositionToPage(r, 1, PAGE_COUNT, new int[]{2});
        testPositionToPage(r, 2, PAGE_COUNT, new int[]{3});
        testPositionToPage(r, 3, PAGE_COUNT, new int[]{4});
        testPositionToPage(r, 4, PAGE_COUNT, new int[]{5});

        SdkTest.logTest(TAG, (new MethodNameHelper() {
        }).getName());

    }

    private static void testPositionToPage(ReaderConfig config, int pos, int pageCount, int[] expectedPages) {
        int[] pages = config.positionToPages(pos, pageCount);
        Assert.assertTrue(Arrays.equals(pages, expectedPages));
    }

}
