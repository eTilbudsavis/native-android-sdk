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


import android.os.Parcel;

import com.shopgun.android.sdk.utils.Constants;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class SgnLocationUnitTest {

    public static final String TAG = Constants.getTag(SgnLocationUnitTest.class);

    Parcel mParcel;

    @Before
    public void setup() {
        mParcel = Parcel.obtain();
    }

    @After
    public void tearDown() {
        mParcel.recycle();
    }

//    @Ignore("Robolectric ShadowLocation has a bug, https://github.com/robolectric/robolectric/issues/2702")
    @Test
    public void testParcelable() throws Exception {

        double lat = 56.0d;
        double lng = 8.0d;
        int radius = 50000;
        String address = "Arne Jacobsens Allé, 2300 København S";

        SgnLocation original = new SgnLocation();
        original.setLatitude(lat);
        original.setLongitude(lng);
        original.setRadius(radius);
        original.setAddress(address);

        original.writeToParcel(mParcel, 0);
        mParcel.setDataPosition(0);
        SgnLocation copy = SgnLocation.CREATOR.createFromParcel(mParcel);

        // android.location.Location doesn't implement equals
        Assert.assertEquals(original.getLatitude(), copy.getLatitude());
        Assert.assertEquals(original.getLongitude(), copy.getLongitude());
        Assert.assertEquals(original.getAddress(), copy.getAddress());
        Assert.assertEquals(original.getRadius(), copy.getRadius());

    }

}
