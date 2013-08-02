package com.eTilbudsavis.etasdk.EtaObjects;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ResponseWrapper {

	private Object mData;
	private int mStatusCode = -1;
	
	public ResponseWrapper(int statusCode, String response) {
		
		mStatusCode = statusCode;
		if (response == null || response.length() == 0) {
			mData = response;
			return;
		}
		
		try {
			if (response.startsWith("[") && response.endsWith("]")) {
				mData = new JSONArray(response);
			} else if(response.startsWith("{") && response.endsWith("}")) {
				mData = new JSONObject(response);
			} else {
				mData = response;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			mData = response;
		}
	}

	public int getStatusCode() {
		return mStatusCode;
	}

	public JSONArray getJSONArray() {
		return isJSONArray() ? (JSONArray) mData : null;
	}

	public JSONObject getJSONObject() {
		return isJSONObject() ? (JSONObject) mData : null;
	}

	public String getString() {
		return mData == null ? null : mData.toString();
	}
	
	public Object getObject() {
		return mData;
	}
	
	public boolean isJSONObject() {
		return mData instanceof JSONObject;
	}

	public boolean isJSONArray() {
		return mData instanceof JSONArray;
	}
	
	public boolean isString() {
		return mData instanceof String;
	}
}
