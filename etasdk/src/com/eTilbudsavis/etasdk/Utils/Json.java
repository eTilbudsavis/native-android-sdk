package com.eTilbudsavis.etasdk.Utils;

import org.json.JSONObject;

import com.eTilbudsavis.etasdk.EtaObjects.EtaObject;

public class Json {

	public static final String TAG = "JsonUtils";
	
	public static String valueOf(JSONObject object, String key) {
		if (object == null || key == null) {
			return null;
		}
		return valueOf(object, key, null);
	}
	
	public static String valueOf(JSONObject object, String key, String defValue) {
		try {
			return object.isNull(key) ? defValue : object.getString(key);
		} catch (Exception e) {
			EtaLog.d(TAG, e);
		}
		return defValue;
	}
	
	public static int valueOf(JSONObject object, String key, int defValue) {
		try {
			return object.isNull(key) ? defValue : object.getInt(key);
		} catch (Exception e) {
			EtaLog.d(TAG, e);
		}
		return defValue;
	}

	public static double valueOf(JSONObject object, String key, double defValue) {
		try {
			return object.isNull(key) ? defValue : object.getDouble(key);
		} catch (Exception e) {
			EtaLog.d(TAG, e);
		}
		return defValue;
	}
	
	public static long valueOf(JSONObject object, String key, long defValue) {
		try {
			return object.isNull(key) ? defValue : object.getLong(key);
		} catch (Exception e) {
			EtaLog.d(TAG, e);
		}
		return defValue;
	}
	
	public static boolean valueOf(JSONObject object, String key, boolean defValue) {
		try {
			return object.isNull(key) ? defValue : object.getBoolean(key);
		} catch (Exception e) {
			EtaLog.d(TAG, e);
		}
		return defValue;
	}

	public static JSONObject toJson(EtaObject etaObject) {
		return (JSONObject) (etaObject == null ? JSONObject.NULL : etaObject.toJSON());
	}
	
	public static <T> Object nullCheck(T o) {
		return o == null ? JSONObject.NULL : o;
	}
	
}
