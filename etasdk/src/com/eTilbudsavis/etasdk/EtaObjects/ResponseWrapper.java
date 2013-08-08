package com.eTilbudsavis.etasdk.EtaObjects;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
			
//			if (mStatusCode == 200) {
//				BufferedReader reader;
//					reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
//				
//			    StringBuilder sb = new StringBuilder();
//			    String line = null;
//			    try {
//			        while ((line = reader.readLine()) != null)
//			            sb.append(line);
//
//			    } catch (IOException e) {
//			        e.printStackTrace();
//			    } 
//			    response = sb.toString();
//			} 
			
			response = EntityUtils.toString(httpResponse.getEntity(), HTTP.UTF_8);
		} catch (ParseException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
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
	
	public Header[] getHeaders() {
		return mHeaders;
	}
}
