package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ResponseWrapper {

	
	private Object mData;
	private int mStatusCode = -1;
	private Header[] mHeaders;
	
	public ResponseWrapper(HttpResponse httpResponse) {
		
		mStatusCode = httpResponse.getStatusLine().getStatusCode();
		mHeaders = httpResponse.getAllHeaders();
		String response = null;
		
		try {
			response = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
		} catch (ParseException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		setData(response);
	}
	
	public ResponseWrapper(EtaError error) {
		mStatusCode = -1;
		setData(error.toJSON().toString());
	}

	public ResponseWrapper(int StatusCode, String data) {
		mStatusCode = StatusCode;
		setData(data);
	}

	private void setData(String data) {

		if (data == null || data.length() == 0) {
			// TODO: Create a json-error object with timeout response
			mData = data;
			return;
		}
		
		try {
			if (data.startsWith("[") && data.endsWith("]")) {
				mData = new JSONArray(data);
			} else if(data.startsWith("{") && data.endsWith("}")) {
				mData = new JSONObject(data);
			} else {
				mData = data;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			mData = data;
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
	
	public Header[] getHeaders() {
		return mHeaders;
	}
}
