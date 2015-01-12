/*******************************************************************************
* Copyright 2014 eTilbudsavis
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
*******************************************************************************/
package com.eTilbudsavis.etasdk.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.Interface.IJson;
import com.eTilbudsavis.etasdk.Log.EtaLog;

/**
 * Helper class designed to simplify working with JSON in Android - specifically the eTilbudsavis Android SDK.
 * The class holds some static methods for converting data, and ensuring that valid data returns.
 * 
 * @author Danny Hvam - danny@etilbudsavis.dk
 *
 */
public class Json {

	public static final String TAG = Eta.TAG_PREFIX + Json.class.getSimpleName();

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
	 * @return Returns the value mapped to the key if it exists, coercing it if necessary else null.
	 */
	public static JSONArray getArray(JSONObject object, String key, JSONArray defValue) {
		if (object == null || key == null) {
			return null;
		}
		try {
			return object.isNull(key) ? null : object.getJSONArray(key);
		} catch (JSONException e) {
			EtaLog.e(TAG, e.getMessage(), e);
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
	 * Searches the JSONObject for the key and returns the matching value if it exists.
	 * @param object An object to get data from
	 * @param key A key to map to a value
	 * @return Returns the value mapped to the key if it exists, coercing it if necessary else null.
	 */
	public static JSONObject getObject(JSONObject object, String key, JSONObject defValue) {
		if (object == null || key == null) {
			return null;
		}
		try {
			return object.isNull(key) ? null : object.getJSONObject(key);
		} catch (JSONException e) {
			EtaLog.e(TAG, e.getMessage(), e);
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
		if (object == null || key == null) {
			return null;
		}
		return valueOf(object, key, null);
	}
	
	/**
	 * Searches the JSONObject for the key and returns the matching value if it exists.
	 * @param object An object to get data from
	 * @param key A key to map to a value
	 * @param defValue A default value to return, if key doesn't exist or causes a JSONException
	 * @return Returns the value mapped to the key if it exists, coercing it if necessary else defValue.
	 */
	public static String valueOf(JSONObject object, String key, String defValue) {
		try {
			return object.isNull(key) ? defValue : object.getString(key);
		} catch (Exception e) {
			EtaLog.e(TAG, null, e);
		}
		return defValue;
	}

	/**
	 * Searches the JSONObject for the key and returns the matching value if it exists.
	 * @param object An object to get data from
	 * @param key A key to map to a value
	 * @param defValue A default value to return, if key doesn't exist or causes a JSONException
	 * @return Returns the value mapped to the key if it exists, coercing it if necessary else defValue.
	 */
	public static int valueOf(JSONObject object, String key, int defValue) {
		try {
			return object.isNull(key) ? defValue : object.getInt(key);
		} catch (Exception e) {
			EtaLog.e(TAG, null, e);
		}
		return defValue;
	}

	/**
	 * Searches the JSONObject for the key and returns the matching value if it exists.
	 * @param object An object to get data from
	 * @param key A key to map to a value
	 * @param defValue A default value to return, if key doesn't exist or causes a JSONException
	 * @return Returns the value mapped to the key if it exists, coercing it if necessary else defValue.
	 */
	public static double valueOf(JSONObject object, String key, double defValue) {
		try {
			return object.isNull(key) ? defValue : object.getDouble(key);
		} catch (Exception e) {
			EtaLog.e(TAG, null, e);
		}
		return defValue;
	}

	/**
	 * Searches the JSONObject for the key and returns the matching value if it exists.
	 * @param object An object to get data from
	 * @param key A key to map to a value
	 * @param defValue A default value to return, if key doesn't exist or causes a JSONException
	 * @return Returns the value mapped to the key if it exists, coercing it if necessary else defValue.
	 */
	public static float valueOf(JSONObject object, String key, float defValue) {
		try {
			return object.isNull(key) ? defValue : (float)object.getDouble(key);
		} catch (Exception e) {
			EtaLog.d(TAG, key + ", " + object.toString());
			EtaLog.e(TAG, null, e);
		}
		return defValue;
	}
	
	/**
	 * Searches the JSONObject for the key and returns the matching value if it exists.
	 * @param object An object to get data from
	 * @param key A key to map to a value
	 * @param defValue A default value to return, if key doesn't exist or causes a JSONException
	 * @return Returns the value mapped to the key if it exists, coercing it if necessary else defValue.
	 */
	public static long valueOf(JSONObject object, String key, long defValue) {
		try {
			return object.isNull(key) ? defValue : object.getLong(key);
		} catch (Exception e) {
			EtaLog.e(TAG, null, e);
		}
		return defValue;
	}

	/**
	 * Searches the JSONObject for the key and returns the matching value if it exists.
	 * @param object An object to get data from
	 * @param key A key to map to a value
	 * @param defValue A default value to return, if key doesn't exist or causes a JSONException
	 * @return Returns the value mapped to the key if it exists, coercing it if necessary else defValue.
	 */
	public static boolean valueOf(JSONObject object, String key, boolean defValue) {
		try {
			return object.isNull(key) ? defValue : object.getBoolean(key);
		} catch (Exception e) {
			EtaLog.e(TAG, null, e);
		}
		return defValue;
	}
	
	/**
	 * Method for safely converting an EtaObject to JSON.
	 * @param object The EtaObject to convert
	 * @param defValue The default value to return in case of errors. 
	 * Typically {@link #JSOBObject.NULL JSOBObject.NULL} or null is used for this purpose
	 * @return A JSONObject, or defValue
	 */
	public static Object toJson(IJson<?> object, Object defValue) {
		return object == null ? defValue : object.toJSON();
	}

	/**
	 * Method for safely converting an EtaObject to JSON.
	 * @param object The EtaObject to convert
	 * @return A JSONObject, or JSONObject.NULL
	 */
	public static Object toJson(IJson<?> object) {
		return toJson(object, JSONObject.NULL);
	}
	
	/**
	 * If an object is null, method will return JSONObject.NULL, else the object it self.
	 * This is useful, when sending data to the eTilbudsavis API v2 as some keys are required
	 * by the API and they will be removed, when performing toString() on the object, if the value 
	 * mapped to the key is null. 
	 * @param object An object to check for null
	 * @return The object, or JSONObject.NULL
	 */
	public static <T> Object nullCheck(T object) {
		return object == null ? JSONObject.NULL : object;
	}

	/**
	 * This method calls {@link Json#colorValueOf(JSONObject, String, String)},
	 * with the defValue set to Color.TRANSPARENT.
	 * @see {@link Json#colorValueOf(JSONObject, String, String)}
	 */
	public static Integer colorValueOf(JSONObject object, String key) {
		return colorValueOf(object, key, null);
	}

	/**
	 * Searches the JSONObject for the key and returns the matching value if it exists.
	 * @param object An object to get data from
	 * @param key A key to map to a value
	 * @param defValue A default value to return, if key doesn't exist or causes a JSONException
	 * @return Returns the value mapped to the key if it exists, coercing it if necessary else defValue.
	 */
	public static Integer colorValueOf(JSONObject object, String key, int color) {
		return colorValueOf(object, key, Utils.colorToString(color));
	}
	
	/**
	 * Searches the JSONObject for the key and returns the matching value if it exists.
	 * @param object An object to get data from
	 * @param key A key to map to a value
	 * @param defValue A default value to return, if key doesn't exist or causes a JSONException
	 * @return Returns the value mapped to the key if it exists, coercing it if necessary else defValue.
	 */
	public static Integer colorValueOf(JSONObject object, String key, String defValue) {
		String rawColor = Json.valueOf(object, key, defValue);
		return Utils.stringToColor(rawColor);
	}
	
}
