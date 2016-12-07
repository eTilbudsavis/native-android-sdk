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


import android.graphics.Color;

import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.SgnJson;
import com.shopgun.android.sdk.utils.SgnUtils;
import com.shopgun.android.utils.ColorUtils;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
@Ignore("SgnJson tests missing")
public class SgnJsonUnitTest {

    public static final String TAG = Constants.getTag(SgnJsonUnitTest.class);

    @Test
    public void testJsonColorMethods() throws Exception {

//        // Testing conversion of int values
//        Assert.assertEquals("000000", Json.colorToString(Color.BLACK));
//        Assert.assertEquals("000000", Json.colorToString(Color.TRANSPARENT));
//        Assert.assertEquals("0000FF", Json.colorToString(Color.BLUE));
//        Assert.assertEquals("0000FF", Json.colorToString(ColorUtils.setAlphaComponent(Color.BLUE, 0)));
//
//        // Testing object values
//        Assert.assertEquals(null, Json.colorToString(null));
//        Assert.assertEquals("000000", Json.colorToString(new SgnColor()));
//        Assert.assertEquals("00FF00", Json.colorToString(new SgnColor(Color.GREEN)));
//        Assert.assertEquals("000000", Json.colorToString(new SgnColor(Color.TRANSPARENT)));
//
//        // Testing object values
//        Assert.assertEquals(JSONObject.NULL, Json.colorToSgnJson(null));
//        Assert.assertEquals("000000", Json.colorToSgnJson(new SgnColor()));
//        Assert.assertEquals("00FF00", Json.colorToSgnJson(new SgnColor(Color.GREEN)));
//        Assert.assertEquals("000000", Json.colorToSgnJson(new SgnColor(Color.TRANSPARENT)));
//
//        JSONObject o = new JSONObject();
//        try {
//            o.put("#ARGB", "#FF00FF00");
//            o.put("#RGB", "#00FF00");
//            o.put("ARGB", "0100FF00");
//            o.put("RGB", "00FF00");
//            o.put("black", "000000");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        // API doesn't support alpha, so we default to black
//        assertEquals(Color.BLACK, Json.colorValueOf(o, "noKey"));
//        // unless we specify it
//        assertEquals(Color.GREEN, Json.colorValueOf(o, "noKey", Color.GREEN));
//
//        // A valid #ARGB color
//        assertEquals(Color.GREEN, Json.colorValueOf(o, "#ARGB"));
//        // A valid #RGB color
//        assertEquals(Color.GREEN, Json.colorValueOf(o, "#RGB"));
//        // ARGB value contains 16 in alpha channel, lets test
//        int greenAlpha = ColorUtils.setAlphaComponent(Color.GREEN, 1);
//        assertEquals(greenAlpha, Json.colorValueOf(o, "ARGB"));
//        // An API green
//        assertEquals(Color.GREEN, Json.colorValueOf(o, "RGB"));
//        // An API Black
//        assertEquals(Color.BLACK, Json.colorValueOf(o, "black"));


    }

    @Test
    public void testJsonPrimitives() throws Exception {

//        Assert.assertNull(Json.getObject(new JSONObject(), null));
//        Assert.assertNull(Json.getObject(null, "myString"));
//        Assert.assertNull(Json.getObject(null, null));
//
//        String noKey = "not-a-key";
//
//        String stringKey = "string";
//        String stringValue = "myString";
//
//        String doubleKey = "double";
//        double doubleValue = 100.001d;
//
//        String integerKey = "integer";
//        int integerValue = 100;
//
//        String longKey = "long";
//        long longValue = Long.MAX_VALUE;
//
//        String booleanKey = "boolean";
//        boolean booleanValue = true;
//
//        String jsonArrayKey = "jsonarray";
//        JSONArray jsonArrayValue = new JSONArray();
//
//        String jsonObjectKey = "jsonobject";
//        JSONObject jsonObjectValue = new JSONObject();
//
//        JSONObject o = new JSONObject();
//        try {
//            o.put(stringKey, stringValue);
//            o.put(doubleKey, doubleValue);
//            o.put(integerKey, integerValue);
//            o.put(longKey, longValue);
//            o.put(booleanKey, booleanValue);
//            o.put(jsonArrayKey, jsonArrayValue);
//            o.put(jsonObjectKey, jsonObjectValue);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        Assert.assertNull(Json.valueOf(o, noKey));
//        Assert.assertNull(Json.valueOf(o, noKey, null));
//        Assert.assertNull(Json.getArray(o, noKey));
//        Assert.assertNull(Json.getArray(o, noKey, null));
//        Assert.assertNull(Json.getObject(o, noKey));
//        Assert.assertNull(Json.getObject(o, noKey, null));
//
//        Assert.assertEquals(stringValue, Json.valueOf(o, noKey, stringValue));
//        Assert.assertEquals(stringValue, Json.valueOf(o, stringKey, stringValue));
//        Assert.assertEquals(stringValue, Json.valueOf(o, stringKey, "shouldnt-return-this-string"));
//        Assert.assertEquals(stringValue, Json.valueOf(o, stringKey, null));
//
//        Assert.assertEquals(doubleValue, Json.valueOf(o, noKey, doubleValue));
//        Assert.assertEquals(doubleValue, Json.valueOf(o, doubleKey, 0.000001d));
//        Assert.assertEquals(doubleValue, Double.valueOf(Json.valueOf(o, doubleKey, null)));
//
//        Assert.assertEquals(integerValue, Json.valueOf(o, noKey, integerValue));
//        Assert.assertEquals(integerValue, Json.valueOf(o, integerKey, 1564));
//        Assert.assertEquals(integerValue, (int) Integer.valueOf(Json.valueOf(o, integerKey, null)));
//
//        Assert.assertEquals(longValue, Json.valueOf(o, noKey, longValue));
//        Assert.assertEquals(longValue, Json.valueOf(o, longKey, longValue));
//        Assert.assertEquals(longValue, (long) Long.valueOf(Json.valueOf(o, longKey, null)));
//
//        Assert.assertEquals(booleanValue, Json.valueOf(o, noKey, booleanValue));
//        Assert.assertEquals(booleanValue, Json.valueOf(o, booleanKey, false));
//        Assert.assertEquals(booleanValue, (boolean) Boolean.valueOf(Json.valueOf(o, booleanKey, null)));
//
//        // Yeah, JSONObject and JSONArray does not handle. equals very well. We'll just check for null
//        Assert.assertNotNull(Json.getArray(o, noKey, jsonArrayValue));
//        Assert.assertNotNull(Json.getArray(o, jsonArrayKey, null));
//        Assert.assertNotNull(Json.getArray(o, jsonArrayKey, jsonArrayValue));
//        Assert.assertNotNull(Json.getObject(o, noKey, jsonObjectValue));
//        Assert.assertNotNull(Json.getObject(o, jsonObjectKey, null));
//        Assert.assertNotNull(Json.getObject(o, jsonObjectKey, jsonObjectValue));


    }

    @Test
    public void testJsonEquals() throws Exception {

//        JSONObject one = null;
//        JSONObject two = null;
//        Assert.assertTrue(Json.jsonObjectEquals(one, two));
//        Assert.assertTrue(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
//
//        one = new JSONObject();
//        Assert.assertFalse(Json.jsonObjectEquals(one, two));
//        Assert.assertFalse(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
//        two = new JSONObject();
//        Assert.assertTrue(Json.jsonObjectEquals(one, two));
//        Assert.assertTrue(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
//
//        one.put("string", "string");
//        Assert.assertFalse(Json.jsonObjectEquals(one, two));
//        Assert.assertFalse(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
//        two.put("string", "string");
//        Assert.assertTrue(Json.jsonObjectEquals(one, two));
//        Assert.assertTrue(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
//
//        one.put("jsonobject", new JSONObject());
//        Assert.assertFalse(Json.jsonObjectEquals(one, two));
//        Assert.assertFalse(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
//        two.put("jsonobject", new JSONObject());
//        Assert.assertTrue(Json.jsonObjectEquals(one, two));
//        Assert.assertTrue(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
//
//        one.put("double", 0.01d);
//        Assert.assertFalse(Json.jsonObjectEquals(one, two));
//        Assert.assertFalse(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
//        two.put("double", 0.01d);
//        Assert.assertTrue(Json.jsonObjectEquals(one, two));
//        Assert.assertTrue(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
//
//        // put null, will remove a key
//        one.put("null", null);
//        Assert.assertTrue(Json.jsonObjectEquals(one, two));
//        Assert.assertTrue(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
//        two.put("null", null);
//        Assert.assertTrue(Json.jsonObjectEquals(one, two));
//        Assert.assertTrue(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
//
//        one.put("json.null", JSONObject.NULL);
//        Assert.assertFalse(Json.jsonObjectEquals(one, two));
//        Assert.assertFalse(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
//        two.put("json.null", JSONObject.NULL);
//        Assert.assertTrue(Json.jsonObjectEquals(one, two));
//
//        two.put("string", "not-string");
//        Assert.assertFalse(Json.jsonObjectEquals(one, two));
//        Assert.assertFalse(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
//        two.put("string", "string");
//
//        JSONArray aOne = new JSONArray();
//        JSONArray aTwo = new JSONArray();
//        Assert.assertTrue(Json.jsonArrayEquals(aOne, aTwo));
//        Assert.assertTrue(Json.jsonArrayHashCode(aOne) == Json.jsonArrayHashCode(aTwo));
//
//        one.put("jsonarray", aOne);
//        Assert.assertFalse(Json.jsonObjectEquals(one, two));
//        Assert.assertFalse(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
//        two.put("jsonarray", aTwo);
//        Assert.assertTrue(Json.jsonObjectEquals(one, two));
//        Assert.assertTrue(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
//
//        aOne.put("MyString");
//        Assert.assertFalse(Json.jsonArrayEquals(aOne, aTwo));
//        Assert.assertFalse(Json.jsonObjectEquals(one, two));
//        Assert.assertFalse(Json.jsonArrayHashCode(aOne) == Json.jsonArrayHashCode(aTwo));
//        Assert.assertFalse(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
//        aTwo.put("MyString");
//        Assert.assertTrue(Json.jsonArrayEquals(aOne, aTwo));
//        Assert.assertTrue(Json.jsonObjectEquals(one, two));
//        Assert.assertTrue(Json.jsonArrayHashCode(aOne) == Json.jsonArrayHashCode(aTwo));
//        Assert.assertTrue(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));

    }

}
