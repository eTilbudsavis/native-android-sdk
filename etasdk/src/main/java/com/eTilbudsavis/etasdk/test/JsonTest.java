package com.eTilbudsavis.etasdk.test;

import android.graphics.Color;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.utils.Json;
import com.eTilbudsavis.etasdk.utils.Utils;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonTest {

    public static final String TAG = Constants.getTag(JsonTest.class);

    public static void test() {

        EtaSdkTest.start(TAG);
        testJsonUtils();

        try {
            testJsonEquals();
        } catch (JSONException e) {
            throw new AssertionError(e);
        }

    }

    public static void testJsonUtils() {

        Assert.assertNull(Json.getObject(new JSONObject(), null));
        Assert.assertNull(Json.getObject(null, "myString"));
        Assert.assertNull(Json.getObject(null, null));

        String noKey = "not-a-key";

        String stringKey = "string";
        String stringValue = "myString";

        String doubleKey = "double";
        double doubleValue = 100.001d;

        String integerKey = "integer";
        int integerValue = 100;

        String longKey = "long";
        long longValue = Long.MAX_VALUE;

        String booleanKey = "boolean";
        boolean booleanValue = true;

        String jsonArrayKey = "jsonarray";
        JSONArray jsonArrayValue = new JSONArray();

        String jsonObjectKey = "jsonobject";
        JSONObject jsonObjectValue = new JSONObject();

        int colorIntValue = Color.BLACK;
        String colorStringKey = "colorstring";
        String colorStringValue = "000000";

        int colorAlphaIntValue = 0x80FF00FF;
        String colorAlphaStringKey = "coloralphastring";
        String colorAlphaStringValue = "80FF00FF";

        JSONObject o = new JSONObject();
        try {
            o.put(stringKey, stringValue);
            o.put(doubleKey, doubleValue);
            o.put(integerKey, integerValue);
            o.put(longKey, longValue);
            o.put(booleanKey, booleanValue);
            o.put(jsonArrayKey, jsonArrayValue);
            o.put(jsonObjectKey, jsonObjectValue);
            o.put(colorStringKey, colorStringValue);
            o.put(colorAlphaStringKey, colorAlphaStringValue);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Assert.assertNull(Json.valueOf(o, noKey));
        Assert.assertNull(Json.valueOf(o, noKey, null));
        Assert.assertNull(Json.getArray(o, noKey));
        Assert.assertNull(Json.getArray(o, noKey, null));
        Assert.assertNull(Json.getObject(o, noKey));
        Assert.assertNull(Json.getObject(o, noKey, null));
        Assert.assertNull(Json.colorValueOf(o, noKey));
        Assert.assertNull(Json.colorValueOf(o, noKey, null));

        Assert.assertEquals(stringValue, Json.valueOf(o, noKey, stringValue));
        Assert.assertEquals(stringValue, Json.valueOf(o, stringKey, stringValue));
        Assert.assertEquals(stringValue, Json.valueOf(o, stringKey, "shouldnt-return-this-string"));
        Assert.assertEquals(stringValue, Json.valueOf(o, stringKey, null));

        Assert.assertEquals(doubleValue, Json.valueOf(o, noKey, doubleValue));
        Assert.assertEquals(doubleValue, Json.valueOf(o, doubleKey, 0.000001d));
        Assert.assertEquals(doubleValue, Double.valueOf(Json.valueOf(o, doubleKey, null)));

        Assert.assertEquals(integerValue, Json.valueOf(o, noKey, integerValue));
        Assert.assertEquals(integerValue, Json.valueOf(o, integerKey, 1564));
        Assert.assertEquals(integerValue, (int)Integer.valueOf(Json.valueOf(o, integerKey, null)));

        Assert.assertEquals(longValue, Json.valueOf(o, noKey, longValue));
        Assert.assertEquals(longValue, Json.valueOf(o, longKey, longValue));
        Assert.assertEquals(longValue, (long) Long.valueOf(Json.valueOf(o, longKey, null)));

        Assert.assertEquals(booleanValue, Json.valueOf(o, noKey, booleanValue));
        Assert.assertEquals(booleanValue, Json.valueOf(o, booleanKey, false));
        Assert.assertEquals(booleanValue, (boolean) Boolean.valueOf(Json.valueOf(o, booleanKey, null)));

        // Yeah, JSONObject and JSONArray does not handle. equals very well. We'll just check for null
        Assert.assertNotNull(Json.getArray(o, noKey, jsonArrayValue));
        Assert.assertNotNull(Json.getArray(o, jsonArrayKey, null));
        Assert.assertNotNull(Json.getArray(o, jsonArrayKey, jsonArrayValue));
        Assert.assertNotNull(Json.getObject(o, noKey, jsonObjectValue));
        Assert.assertNotNull(Json.getObject(o, jsonObjectKey, null));
        Assert.assertNotNull(Json.getObject(o, jsonObjectKey, jsonObjectValue));

        Assert.assertEquals(colorIntValue, (int) Json.colorValueOf(o, colorStringKey));
        Assert.assertEquals(colorIntValue, (int) Json.colorValueOf(o, colorStringKey, colorStringValue));
        Assert.assertEquals(colorIntValue, (int) Json.colorValueOf(o, colorStringKey, colorIntValue));
        Assert.assertEquals(colorIntValue, (int) Json.colorValueOf(o, colorStringKey, "this-is-not-a-color"));

        // We do not allow alpha channel, so this will be stripped in the process
        Assert.assertEquals(colorAlphaIntValue, (int) Json.colorValueOf(o, colorAlphaStringKey));
        Assert.assertEquals(colorAlphaIntValue, (int) Json.colorValueOf(o, colorAlphaStringKey, colorAlphaStringValue));
        Assert.assertEquals(colorAlphaIntValue, (int) Json.colorValueOf(o, colorAlphaStringKey, colorAlphaIntValue));
        Assert.assertEquals(colorAlphaIntValue, (int) Json.colorValueOf(o, colorAlphaStringKey, "this-is-not-a-color"));

    }

    public static void testJsonEquals() throws JSONException {

        JSONObject one = null;
        JSONObject two = null;
        Assert.assertTrue(Json.jsonObjectEquals(one,two));
        Assert.assertTrue(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));

        one = new JSONObject();
        Assert.assertFalse(Json.jsonObjectEquals(one, two));
        Assert.assertFalse(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
        two = new JSONObject();
        Assert.assertTrue(Json.jsonObjectEquals(one, two));
        Assert.assertTrue(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));

        one.put("string", "string");
        Assert.assertFalse(Json.jsonObjectEquals(one, two));
        Assert.assertFalse(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
        two.put("string", "string");
        Assert.assertTrue(Json.jsonObjectEquals(one, two));
        Assert.assertTrue(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));

        one.put("jsonobject", new JSONObject());
        Assert.assertFalse(Json.jsonObjectEquals(one, two));
        Assert.assertFalse(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
        two.put("jsonobject", new JSONObject());
        Assert.assertTrue(Json.jsonObjectEquals(one, two));
        Assert.assertTrue(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));

        one.put("double", 0.01d);
        Assert.assertFalse(Json.jsonObjectEquals(one, two));
        Assert.assertFalse(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
        two.put("double", 0.01d);
        Assert.assertTrue(Json.jsonObjectEquals(one, two));
        Assert.assertTrue(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));

        // put null, will remove a key
        one.put("null", null);
        Assert.assertTrue(Json.jsonObjectEquals(one, two));
        Assert.assertTrue(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
        two.put("null", null);
        Assert.assertTrue(Json.jsonObjectEquals(one, two));
        Assert.assertTrue(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));

        one.put("json.null", JSONObject.NULL);
        Assert.assertFalse(Json.jsonObjectEquals(one, two));
        Assert.assertFalse(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
        two.put("json.null", JSONObject.NULL);
        Assert.assertTrue(Json.jsonObjectEquals(one, two));

        two.put("string", "not-string");
        Assert.assertFalse(Json.jsonObjectEquals(one, two));
        Assert.assertFalse(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
        two.put("string", "string");

        JSONArray aOne = new JSONArray();
        JSONArray aTwo = new JSONArray();
        Assert.assertTrue(Json.jsonArrayEquals(aOne, aTwo));
        Assert.assertTrue(Json.jsonArrayHashCode(aOne) == Json.jsonArrayHashCode(aTwo));

        one.put("jsonarray", aOne);
        Assert.assertFalse(Json.jsonObjectEquals(one, two));
        Assert.assertFalse(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
        two.put("jsonarray", aTwo);
        Assert.assertTrue(Json.jsonObjectEquals(one, two));
        Assert.assertTrue(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));

        aOne.put("MyString");
        Assert.assertFalse(Json.jsonArrayEquals(aOne, aTwo));
        Assert.assertFalse(Json.jsonObjectEquals(one, two));
        Assert.assertFalse(Json.jsonArrayHashCode(aOne) == Json.jsonArrayHashCode(aTwo));
        Assert.assertFalse(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));
        aTwo.put("MyString");
        Assert.assertTrue(Json.jsonArrayEquals(aOne, aTwo));
        Assert.assertTrue(Json.jsonObjectEquals(one, two));
        Assert.assertTrue(Json.jsonArrayHashCode(aOne) == Json.jsonArrayHashCode(aTwo));
        Assert.assertTrue(Json.jsonObjectHashCode(one) == Json.jsonObjectHashCode(two));

        EtaSdkTest.logTest(TAG, "JsonEquals");

    }

}
