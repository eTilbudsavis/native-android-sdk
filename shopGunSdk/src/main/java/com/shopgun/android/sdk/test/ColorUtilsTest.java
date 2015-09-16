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

import android.graphics.Color;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.palette.SgnColor;
import com.shopgun.android.sdk.utils.ColorUtils;

import junit.framework.TestCase;

public class ColorUtilsTest extends TestCase {

    public static final String TAG = Constants.getTag(ColorUtilsTest.class);

    private ColorUtilsTest() {
        // empty
    }

    public static void test() {
        SgnLog.w(TAG, "ColorUtilsTest NOT being performed");
        assertEquals("#00000000", ColorUtils.toARGBString(Color.TRANSPARENT));
        assertEquals("#FF000000", ColorUtils.toARGBString(Color.BLACK));
        assertEquals("#FF0000FF", ColorUtils.toARGBString(Color.BLUE));
        assertEquals("#FFFF0000", ColorUtils.toARGBString(Color.RED));
        assertEquals("#FF00FF00", ColorUtils.toARGBString(Color.GREEN));

        assertNull(ColorUtils.toARGBString(null));
        assertEquals("#FF000000", ColorUtils.toARGBString(new SgnColor(Color.BLACK)));
        assertEquals("#FF0000FF", ColorUtils.toARGBString(new SgnColor(Color.BLUE)));
        assertEquals("#FFFF0000", ColorUtils.toARGBString(new SgnColor(Color.RED)));
        assertEquals("#FF00FF00", ColorUtils.toARGBString(new SgnColor(Color.GREEN)));

        assertEquals(0xFFFFFFFF, ColorUtils.getCompliment(0xFF000000));
        assertEquals(0xFF0000FF, ColorUtils.getCompliment(0xFFFFFF00));

        assertEquals(new float[]{0.0f, 1.0f, 1.0f}, ColorUtils.toHSV(0xFFFF0000), 0.01f);
        assertEquals(new float[]{240.0f, 1.0f, 1.0f}, ColorUtils.toHSV(0xFF0000FF), 0.01f);
        assertEquals(new float[]{60.0f, 1.0f, 1.0f}, ColorUtils.toHSV(0xFFFFFF00), 0.01f);

        SdkTest.start(TAG);
    }

    public static void assertEquals(float[] expected, float[] actual, float delta) {

        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], actual[i], delta);
        }

    }

}
