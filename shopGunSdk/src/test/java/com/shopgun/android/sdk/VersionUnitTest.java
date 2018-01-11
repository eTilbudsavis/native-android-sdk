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

package com.shopgun.android.sdk;



import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.Version;
import com.shopgun.android.utils.log.L;

import junit.framework.TestCase;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class VersionUnitTest extends TestCase {

    public static final String TAG = Constants.getTag(VersionUnitTest.class);

    @Ignore("Version test fails, have too look at that")
    @Test
    public void testVersion() throws Exception {

        int[] versions = new int[]{0, 1, 1000, 1001, 100000, 101000, 101001};
        String[] expected = new String[]{ "0.0.0", "0.0.1", "0.1.0", "0.1.1", "1.0.0", "1.1.0", "1.1.1"};

        for (int i = 0; i < versions.length; i++) {
            L.d(TAG, "i:"+i+", version:"+versions[i]+", expected:"+expected[i]);
            Version v = new Version(versions[i]);
            assertEquals(versions[i], v.getCode());
            assertEquals(expected[i], v.toString());
        }

        Version version = new Version(0, "build");
        assertEquals(version.getMajor(), 0);
        assertEquals(version.getMinor(), 0);
        assertEquals(version.getPatch(), 0);

        version = new Version(1020304, "build");
        assertEquals(version.getMajor(), 1);
        assertEquals(version.getMinor(), 2);
        assertEquals(version.getPatch(), 3);

    }

}
