package com.eTilbudsavis.etasdk.test;

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

        try {
            testJsonEquals();
        } catch (JSONException e) {
            throw new AssertionError(e);
        }

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
