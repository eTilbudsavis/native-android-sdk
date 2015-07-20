package com.shopgun.android.sdk.test;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.utils.PermissionUtils;
import com.shopgun.android.sdk.utils.Utils;

import junit.framework.Assert;

public class PermissionUtilsTest {

    public static final String TAG = Constants.getTag(PermissionUtilsTest.class);

    public static void test() {

        SdkTest.start(TAG);
        testAllow();
    }

    public static void testAllow() {

        SdkTest.logTest(TAG, "CreateUUID");

    }

}
