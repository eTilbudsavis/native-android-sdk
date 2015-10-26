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

package com.shopgun.android.sdk.utils;

import android.graphics.Color;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.interfaces.IJson;
import com.shopgun.android.sdk.palette.MaterialColor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Helper class designed to simplify working with JSON in Android - specifically the ShopGun Android SDK.
 * The class holds some static methods for converting data, and ensuring that valid data returns.
 */
public class Json {

    public static final String TAG = Constants.getTag(Json.class);

    /**
     * Searches the JSONObject for the key and returns the matching value if it exists.
     * @param object An object to get data from
     * @param key A key to map to a value
     * @return Returns the value mapped to the key if it exists, coercing it if necessary else null.
     */
    public static JSONArray getArray(JSONObject object, String key) {
        return getArray(object, key, null);
    }

    /**
     * Searches the JSONObject for the key and returns the matching value if it exists.
     * @param object An object to get data from
     * @param key A key to map to a value
     * @param defValue Value to return if key does not exist or if a null
     *     value is associated with the given key.
     * @return Returns the value mapped to the key if it exists, coercing it if necessary else null.
     */
    public static JSONArray getArray(JSONObject object, String key, JSONArray defValue) {
        if (object == null || key == null) {
            return defValue;
        }
        try {
            return object.isNull(key) ? defValue : object.getJSONArray(key);
        } catch (JSONException e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }
        return defValue;
    }

    /**
     * Searches the JSONObject for the key and returns the matching value if it exists.
     * @param object An object to get data from
     * @param key A key to map to a value
     * @return Returns the value mapped to the key if it exists, coercing it if necessary else null.
     */
    public static JSONObject getObject(JSONObject object, String key) {
        return getObject(object, key, null);
    }

    /**
     * Searches the {@code object} for the {@code key} and returns the matching value if it exists.
     * @param object An object to get data from
     * @param key A key to map to a value
     * @param defValue Value to return if key does not exist or if a null
     *     value is associated with the given key.
     * @return Returns the value mapped to the key if it exists, coercing it if necessary else {@code defValue}.
     */
    public static JSONObject getObject(JSONObject object, String key, JSONObject defValue) {
        if (object == null || key == null) {
            return defValue;
        }
        try {
            return object.isNull(key) ? defValue : object.getJSONObject(key);
        } catch (JSONException e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }
        return defValue;
    }

    /**
     * Returns the value at {@code index} if it exists and is a {@code JSONObject}.
     * @param array An JSONArray to get data from
     * @param index The index to get
     * @return Returns the value mapped to the {@code index} if it exists, coercing it if necessary else {@code null}.
     */
    public static JSONObject getObject(JSONArray array, int index) {
        return getObject(array, index, null);
    }

    /**
     * Returns the value at {@code index} if it exists and is a {@code JSONObject}.
     * @param array An JSONArray to get data from
     * @param index The index to get
     * @param defValue Value to return if key does not exist or if a null
     *     value is associated with the given key.
     * @return Returns the value mapped to the {@code index} if it exists, coercing it if necessary else {@code defValue}.
     */
    public static JSONObject getObject(JSONArray array, int index, JSONObject defValue) {
        if (array == null) {
            return defValue;
        }
        try {
            return array.isNull(index) ? defValue : array.getJSONObject(index);
        } catch (JSONException e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }
        return defValue;
    }

    /**
     * Searches the JSONObject for the key and returns the matching value if it exists.
     * @param object An object to get data from
     * @param key A key to map to a value
     * @return Returns the value mapped to the key if it exists, coercing it if necessary else null.
     */
    public static String valueOf(JSONObject object, String key) {
        return valueOf(object, key, null);
    }

    /**
     * Searches the JSONObject for the key and returns the matching value if it exists.
     * @param object An object to get data from
     * @param key A key to map to a value
     * @param defValue Value to return if key does not exist or if a null
     *     value is associated with the given key.
     * @return Returns the value mapped to the key if it exists, coercing it if necessary else defValue.
     */
    public static String valueOf(JSONObject object, String key, String defValue) {
        try {
            return object.isNull(key) ? defValue : object.getString(key);
        } catch (Exception e) {
            SgnLog.e(TAG, null, e);
        }
        return defValue;
    }

    /**
     * Searches the JSONObject for the key and returns the matching value if it exists.
     * @param object An object to get data from
     * @param key A key to map to a value
     * @param defValue Value to return if key does not exist or if a null
     *     value is associated with the given key.
     * @return Returns the value mapped to the key if it exists, coercing it if necessary else defValue.
     */
    public static int valueOf(JSONObject object, String key, int defValue) {
        try {
            return object.isNull(key) ? defValue : object.getInt(key);
        } catch (Exception e) {
            SgnLog.e(TAG, null, e);
        }
        return defValue;
    }

    /**
     * Searches the JSONObject for the key and returns the matching value if it exists.
     * @param object An object to get data from
     * @param key A key to map to a value
     * @param defValue Value to return if key does not exist or if a null
     *     value is associated with the given key.
     * @return Returns the value mapped to the key if it exists, coercing it if necessary else defValue.
     */
    public static double valueOf(JSONObject object, String key, double defValue) {
        try {
            return object.isNull(key) ? defValue : object.getDouble(key);
        } catch (Exception e) {
            SgnLog.e(TAG, null, e);
        }
        return defValue;
    }

    /**
     * Searches the JSONObject for the key and returns the matching value if it exists.
     * @param object An object to get data from
     * @param key A key to map to a value
     * @param defValue Value to return if key does not exist or if a null
     *     value is associated with the given key.
     * @return Returns the value mapped to the key if it exists, coercing it if necessary else defValue.
     */
    public static float valueOf(JSONObject object, String key, float defValue) {
        try {
            return object.isNull(key) ? defValue : (float) object.getDouble(key);
        } catch (Exception e) {
            SgnLog.d(TAG, key + ", " + object.toString());
            SgnLog.e(TAG, null, e);
        }
        return defValue;
    }

    /**
     * Searches the JSONObject for the key and returns the matching value if it exists.
     * @param object An object to get data from
     * @param key A key to map to a value
     * @param defValue Value to return if key does not exist or if a null
     *     value is associated with the given key.
     * @return Returns the value mapped to the key if it exists, coercing it if necessary else defValue.
     */
    public static long valueOf(JSONObject object, String key, long defValue) {
        try {
            return object.isNull(key) ? defValue : object.getLong(key);
        } catch (Exception e) {
            SgnLog.e(TAG, null, e);
        }
        return defValue;
    }

    /**
     * Searches the JSONObject for the key and returns the matching value if it exists.
     * @param object An object to get data from
     * @param key A key to map to a value
     * @param defValue Value to return if key does not exist or if a null
     *     value is associated with the given key.
     * @return Returns the value mapped to the key if it exists, coercing it if necessary else defValue.
     */
    public static boolean valueOf(JSONObject object, String key, boolean defValue) {
        try {
            return object.isNull(key) ? defValue : object.getBoolean(key);
        } catch (Exception e) {
            SgnLog.e(TAG, null, e);
        }
        return defValue;
    }

    /**
     * Method for safely converting a ShopGun model object to JSON.
     * @param object The ShopGun model object to convert
     * @param defValue The default value to return in case of errors.
     * Typically {@link JSONObject#NULL JSOBObject.NULL} or null is used for this purpose
     * @return A JSONObject, or defValue
     */
    public static Object toJson(IJson<?> object, Object defValue) {
        return object == null ? defValue : object.toJSON();
    }

    /**
     * Method for safely converting a ShopGun model object to JSON.
     * @param object The ShopGun model object to convert
     * @return A {@link JSONObject}, or {@link JSONObject#NULL}
     */
    public static Object toJson(IJson<?> object) {
        return toJson(object, JSONObject.NULL);
    }

    /**
     * If an object is null, method will return JSONObject.NULL, else the object it self.
     * This is useful, when sending data to the ShopGun API v2 as some keys are required
     * by the API and they will be removed, when performing toString() on the object, if the value
     * mapped to the key is null.
     * @param object An object to check for null
     * @param <T> Any type
     * @return The object, or {@link JSONObject#NULL}
     */
    public static <T> Object nullCheck(T object) {
        return object == null ? JSONObject.NULL : object;
    }

    /**
     * This method calls {@link Json#colorValueOf(JSONObject, String, int)},
     * with the defValue set to Color.BLACK.
     * @param object An object to get data from
     * @param key A key to map to a value
     * @return Returns the value mapped to the key if it exists, coercing it if necessary, else {@link Color#BLACK}.
     */
    public static int colorValueOf(JSONObject object, String key) {
        return colorValueOf(object, key, Color.BLACK);
    }

    /**
     * Searches the JSONObject for the key and returns the matching value if it exists.
     * @param object An object to get data from
     * @param key A key to map to a value
     * @param defValue A default value to return, if key doesn't exist or causes a JSONException
     * @return Returns the value mapped to the key if it exists, coercing it if necessary, else {@code defValue}.
     */
    public static int colorValueOf(JSONObject object, String key, int defValue) {
        String rawColor = Json.valueOf(object, key);
        if (rawColor != null) {
            try {
                if (rawColor.charAt(0) != '#') {
                    rawColor = '#' + rawColor;
                }
                return Color.parseColor(rawColor);
            } catch (Exception e) {
                // ignore
            }
        }
        return defValue;
    }

    public static Object colorToSgnJson(MaterialColor color) {
        return nullCheck(colorToString(color));
    }

    /**
     * Convert a color into a ShopGun valid formatted string.
     * @param color A color
     * @return A string representing the color in a ShopGun valid format. Or {@link JSONObject#NULL} if input color is null
     */
    public static String colorToString(MaterialColor color) {
        return color == null ? null : colorToString(color.getValue());
    }

    /**
     * Convert a color into a ShopGun valid formatted string.
     * @param color A color
     * @return A String, representing the color in a ShopGun valid format.
     */
    public static String colorToString(int color) {
        if (Color.alpha(color) != 255) {
            SgnLog.w(TAG, "ShopGun api doesn't support transparency. Transparency will be stripped.");
        }
        return String.format("%06X", 0xFFFFFF & color);
    }

    /**
     * Method for generating a consistent HashCode for a given JSONArray
     * @param a A JSONArray
     * @return A hashCode
     */
    public static int jsonArrayHashCode(JSONArray a) {
        try {
            return jsonArrayHashCodeInternal(a);
        } catch (JSONException e) {
            e.printStackTrace();
            return a.hashCode();
        }
    }

    private static int jsonArrayHashCodeInternal(JSONArray a) throws JSONException {
        if (a == null) {
            return 0;
        }
        final int prime = 31;
        int result = 1;
        for (int i = 0; i < a.length(); i++) {
            Object o = a.get(i);
            int hash = 0;
            if (o instanceof JSONObject) {
                hash = jsonObjectHashCode((JSONObject) o);
            } else if (o instanceof JSONArray) {
                hash = jsonArrayHashCode((JSONArray) o);
            } else {
                hash = (o == null) ? 0 : o.hashCode();
            }
            result = prime * result + hash;
        }
        return result;
    }

    /**
     * Method for determining equality in two JSONArray's
     * @param one A JSONArray
     * @param two A JSONArray
     * @return true if they are equal, else false
     */
    public static boolean jsonArrayEquals(JSONArray one, JSONArray two) {
        try {
            return jsonEqualsInternal(one, two);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean jsonEqualsInternal(JSONArray one, JSONArray two) throws JSONException {

        if (one == null || two == null) {
            return one == two;
        }
        if (one.length() != two.length()) {
            return false;
        }

        // Set of elements that have been comparedand found equal
        // and therefore cannot be used again
        Set<Integer> used = new HashSet<Integer>(two.length());

        // bubble sort check for equals isn't very efficient
        outerloop:
        for (int i = 0; i < one.length(); i++) {

            Object objOne = one.isNull(i) ? null : one.get(i);

            for (int j = 0; j < two.length(); j++) {

                if (used.contains(i)) {
                    continue;
                }

                Object objTwo = two.isNull(j) ? null : two.get(j);

                if (objOne == null || objTwo == null) {
                    if (objOne == objTwo) {
                        // both null, just continue
                        used.add(j);
                        continue outerloop;
                    } else {
                        return false;
                    }
                }

                if (isEqualJson(objOne, objTwo)) {
                    used.add(j);
                    continue outerloop;
                }

            }
            // we'll only reach this if there wasn't a match/mismatch in the above loop
            return false;

        }
        return true;
    }

    /**
     * Method for generating a consistent HashCode for a given JSONObject
     * @param o A JSONObject
     * @return A hashCode
     */
    public static int jsonObjectHashCode(JSONObject o) {
        try {
            return jsonObjectHashCodeInternal(o);
        } catch (JSONException e) {
            e.printStackTrace();
            return o.hashCode();
        }
    }

    private static int jsonObjectHashCodeInternal(JSONObject o) throws JSONException {
        if (o == null) {
            return 0;
        }
        List<String> keys = new ArrayList<String>();
        Iterator<String> it = o.keys();
        while (it.hasNext()) {
            keys.add(it.next());
        }
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();

        final int prime = 31;
        int result = 1;

        for (String key : keys) {
            Object tmp = o.get(key);
            if (tmp instanceof JSONObject) {
                result = prime * result + jsonObjectHashCode((JSONObject) tmp);
            } else if (tmp instanceof JSONArray) {
                result = prime * result + jsonArrayHashCode((JSONArray) tmp);
            } else {
                sb.append(key).append(tmp);
            }
        }
        result = prime * result + sb.toString().hashCode();
        return result;
    }

    /**
     * Method for determining equality in two JSONObject's
     * @param one A JSONObject
     * @param two A JSONObject
     * @return true if they are equal, else false
     */
    public static boolean jsonObjectEquals(JSONObject one, JSONObject two) {
        try {
            return jsonEqualsInternal(one, two);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean jsonEqualsInternal(JSONObject one, JSONObject two) throws JSONException {
        if (one == null || two == null) {
            return one == two;
        }
        if (one.length() != two.length()) {
            return false;
        }

        Iterator<String> it = one.keys();
        while (it.hasNext()) {
            String key = it.next();
            if (!two.has(key)) {
                return false;
            }

            Object objOne = one.isNull(key) ? null : one.get(key);
            Object objTwo = two.isNull(key) ? null : two.get(key);

            if (objOne == null || objTwo == null) {
                if (objOne == objTwo) {
                    // both null, just continue
                    continue;
                } else {
                    return false;
                }
            }

            if (!isEqualJson(objOne, objTwo)) {
                return false;
            }

        }

        return true;
    }

    private static boolean isEqualJson(Object one, Object two) {

        if (one instanceof JSONObject) {
            return (two instanceof JSONObject) && jsonObjectEquals((JSONObject) one, (JSONObject) two);
        }

        if (one instanceof JSONArray) {
            return (two instanceof JSONArray) && jsonArrayEquals((JSONArray) one, (JSONArray) two);
        }

        return one.equals(two);

    }

}
