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
import com.shopgun.android.sdk.log.SgnLog;

public class SdkTest {

    public static final String TAG = Constants.getTag(SdkTest.class);
    private static final boolean LOG = false;
    private static String mCurrentTitle;
    private static long mStartScoped = 0;

    public static void test() {

        long start = System.currentTimeMillis();

        ValidatorTest.test();
        UtilsTest.test();
        JsonTest.test();
        ColorUtilsTest.test();
        ModelTest.test();
        SerializationSpeedTest.test();
        EnvironmentTest.test();
        EventTest.test();
        SgnLocationTest.test();
        PageflipUtilsTest.test();
        ListUtilsTest.test();

        String ok = "*   All test passed (" + (System.currentTimeMillis() - start) + "ms)   *";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ok.length(); i++) {
            sb.append("*");
        }
        String header = sb.toString();

        SgnLog.d(TAG, "Done: " + mCurrentTitle + " (" + (System.currentTimeMillis() - mStartScoped) + "ms)");
        SgnLog.d(TAG, header);
        SgnLog.d(TAG, ok);
        SgnLog.d(TAG, header);
        mCurrentTitle = null;

    }

    protected static void start(String testName) {

        if (mCurrentTitle == null) {
            mStartScoped = System.currentTimeMillis();
            SgnLog.d(TAG, "Performing: " + testName);
        } else if (!mCurrentTitle.equals(testName)) {
            SgnLog.d(TAG, "Done: " + mCurrentTitle + " (" + (System.currentTimeMillis() - mStartScoped) + "ms)");
            mStartScoped = System.currentTimeMillis();
            SgnLog.d(TAG, "Performing: " + testName);
        }

        mCurrentTitle = testName;

    }

    protected static void logTest(String tag, String testName) {
        if (LOG) {
            SgnLog.d(tag, " - " + testName);
        }
    }

    protected static void logTestWarning(String tag, String testName, String warning) {
        SgnLog.w(tag, " - " + testName + " - " + warning);
    }

}
