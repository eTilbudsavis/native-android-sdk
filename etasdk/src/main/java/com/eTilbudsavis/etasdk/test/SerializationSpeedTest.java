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

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.model.Shoppinglist;

import junit.framework.Assert;

public class SerializationSpeedTest {

    public static final String TAG = Constants.getTag(SerializationSpeedTest.class);

    public static void test() {
        test(100);
    }

    public static void test(int count) {

        EtaSdkTest.start(TAG);
        EtaSdkTest.logTest(TAG, "testing " + count + " iterations of serilization");

        long start = System.currentTimeMillis();
        test(count, ModelCreator.getShoppinglist(), Shoppinglist.CREATOR);
        print("Shoppinglist", count, start);

    }

    private static void print(String name, int count, long start) {
        long time = (System.currentTimeMillis() - start);
        float avg = (time) / (float) count;
        String format = "total: %sms, avg: %.2fms";
        EtaSdkTest.logTest(TAG, String.format(format, time, avg));

    }

    private static <T extends Parcelable> void test(int count, T obj, Creator<? extends Object> c) {

        for (int i = 0; i < count; i++) {
            Parcel parcel = Parcel.obtain();
            obj.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            Object parceledObj = c.createFromParcel(parcel);
            Assert.assertEquals(obj, parceledObj);
        }

    }

    private static void testShoppinglistSerilization(int count) {

        Shoppinglist sl = ModelCreator.getShoppinglist();
        for (int i = 0; i < count; i++) {
            Parcel parcel = Parcel.obtain();
            sl.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            Shoppinglist parceledObj = Shoppinglist.CREATOR.createFromParcel(parcel);
        }

    }

}
