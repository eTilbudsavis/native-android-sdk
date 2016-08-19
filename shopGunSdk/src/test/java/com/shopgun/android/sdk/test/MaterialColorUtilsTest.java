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

import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.utils.palette.MaterialColorUtils;
import com.shopgun.android.utils.palette.SgnColor;

import junit.framework.TestCase;

public class MaterialColorUtilsTest extends TestCase {

    public static final String TAG = Constants.getTag(MaterialColorUtilsTest.class);

    private MaterialColorUtilsTest() {
        // empty
    }

    public static void test() {

        assertNull(MaterialColorUtils.toARGBString(null));
        assertEquals("#FF000000", MaterialColorUtils.toARGBString(new SgnColor(Color.BLACK)));
        assertEquals("#FF0000FF", MaterialColorUtils.toARGBString(new SgnColor(Color.BLUE)));
        assertEquals("#FFFF0000", MaterialColorUtils.toARGBString(new SgnColor(Color.RED)));
        assertEquals("#FF00FF00", MaterialColorUtils.toARGBString(new SgnColor(Color.GREEN)));

        SdkTest.start(TAG);
    }

}
