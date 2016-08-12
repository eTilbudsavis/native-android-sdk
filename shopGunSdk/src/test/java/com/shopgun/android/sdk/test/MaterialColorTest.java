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


import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.utils.ColorUtils;
import com.shopgun.android.utils.palette.MaterialColor;
import com.shopgun.android.utils.palette.Shade;

import junit.framework.TestCase;

import java.util.Map;

public class MaterialColorTest extends TestCase {

    public static final String TAG = Constants.getTag(MaterialColorTest.class);

    private MaterialColorTest() {
        // empty
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();


    }

    public static void test() {

        SgnLog.v(TAG, "MaterialColorTest isn't working");

        Map<MaterialColor, MaterialColorTestCreator.MaterialTestColor> map = MaterialColorTestCreator.getTestMap();

        for (Map.Entry<MaterialColor, MaterialColorTestCreator.MaterialTestColor> e : map.entrySet()) {
            MaterialColor c = e.getKey();
            MaterialColorTestCreator.MaterialTestColor v = e.getValue();
            testMaterialColor(c, v);
        }

        SdkTest.start(TAG);
    }

    public static void testMaterialColor(MaterialColor c, MaterialColorTestCreator.MaterialTestColor v) {

        assertEquals(c.getValue(), v.value);
        assertEquals(c.getLuminance(), v.luminance, 0.00001);
        assertEquals(c.getPrimaryText(), v.primaryText);
        assertEquals(c.getSecondaryText(), v.secondaryText);
        assertEquals(c.getDisabledText(), v.disabledText);
        for (Shade s : Shade.values()) {
            testShade(s, c, v.shades.get(s));
        }

    }

    public static void testShade(Shade s, MaterialColor c, Float[] expectedHSV) {

        int color = c.getColor(s).getValue();
        float[] hsv = ColorUtils.toHSV(color);

        float delta = 0.01f;
        for (int i = 0; i < 2; i++) {
            assertEquals(expectedHSV[i], hsv[i], delta);
        }
    }

}
