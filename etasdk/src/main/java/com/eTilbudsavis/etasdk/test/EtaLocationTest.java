/*******************************************************************************
 * Copyright 2015 eTilbudsavis
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

package com.eTilbudsavis.etasdk.test;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.EtaLocation;
import com.eTilbudsavis.etasdk.utils.Utils;

import junit.framework.Assert;

/**
 * Created by oizo on 15/05/15.
 */
public class EtaLocationTest {

    public static final String TAG = Constants.getTag(EtaLocationTest.class);

    private EtaLocationTest() {
        // empty
    }

    public static void test() {

        EtaSdkTest.start(TAG);
        testEtaLocation();

    }

    public static void testEtaLocation() {

        double lat = 56.0d;
        double lng = 8.0d;
        int radius = 50000;
        String address = "random";

        EtaLocation l = new EtaLocation();
        l.setLatitude(lat);
        l.setLongitude(lng);
        l.setRadius(radius);
        l.setAddress(address);

        EtaLocation pl = Utils.copyParcelable(l, EtaLocation.CREATOR);

        // android.location.Location doesn't implement equals
        Assert.assertEquals(l.getLatitude(), pl.getLatitude());
        Assert.assertEquals(l.getLongitude(), pl.getLongitude());
        Assert.assertEquals(l.getAddress(), pl.getAddress());
        Assert.assertEquals(l.getRadius(), pl.getRadius());

        EtaSdkTest.logTest(TAG, (new MethodNameHelper() {
        }).getName());
    }

}
