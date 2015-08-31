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

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.utils.Version;

import junit.framework.TestCase;

public class VersionTest extends TestCase {

    public static final String TAG = Constants.getTag(VersionTest.class);

    public static void test() {

        SdkTest.start(TAG);
        testVersion();

    }

    public static void testVersion() {

        int[] ints = new int[]{0, 1, 1000, 1001, 100000, 101000, 101001};
        String[] strings = new String[]{ "0.0.0", "0.0.1", "0.1.0", "0.1.1", "1.1.0", "1.1.1"};

        for (int i = 0; i < ints.length; i++) {
            int intVersion = ints[i];
            Version v = new Version(intVersion);
            assertEquals(intVersion, v.getCode());
            assertEquals(v.toString(), strings[i]);
        }

        Version version = new Version(0);
        assertEquals(version.getMajor(), 0);
        assertEquals(version.getMinor(), 0);
        assertEquals(version.getPatch(), 0);

        version = new Version(101001);
        assertEquals(version.getMajor(), 1);
        assertEquals(version.getMinor(), 1);
        assertEquals(version.getPatch(), 1);

        SdkTest.logTest(TAG, "Version");
    }

}
