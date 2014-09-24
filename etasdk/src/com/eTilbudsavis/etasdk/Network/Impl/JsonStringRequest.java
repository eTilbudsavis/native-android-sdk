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
package com.eTilbudsavis.etasdk.Network.Impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Network.Cache;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.NetworkResponse;
import com.eTilbudsavis.etasdk.Network.Response;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.Api.Param;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class JsonStringRequest extends JsonRequest<String>{

	public static final String TAG = Eta.TAG_PREFIX + JsonStringRequest.class.getSimpleName();

	// Define catchable types
	private static Map<String, String> mFilterTypes = new HashMap<String, String>();
	
	static {
		mFilterTypes.put("catalogs", Param.FILTER_CATALOG_IDS);
		mFilterTypes.put("offers", Param.FILTER_OFFER_IDS);
		mFilterTypes.put("dealers", Param.FILTER_DEALER_IDS);
		mFilterTypes.put("stores", Param.FILTER_STORE_IDS);
	}
	
	public JsonStringRequest(String url, Listener<String> listener) {
		super(url, listener);
	}
	
	public JsonStringRequest(Method method, String url, String requestBody, Listener<String> listener) {
		super(method, url, requestBody, listener);
	}
	
	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response) {
		
		String jsonString = null;
		try {
			jsonString = new String(response.data, getParamsEncoding()).trim();
		} catch (UnsupportedEncodingException e) {
			jsonString = new String(response.data).trim();
		}
		
		if (Utils.isSuccess(response.statusCode)) {
			
			if (jsonString.startsWith("{") && jsonString.endsWith("}")) {
				
				try {
					JSONObject jObject = new JSONObject(jsonString);
					cacheJSONObject(jObject);
				} catch (JSONException e) {
					EtaLog.e(TAG, "", e);
		            return Response.fromError(new ParseError(e, JSONObject.class));
				}
				
			} else if (jsonString.startsWith("[") && jsonString.endsWith("]")) {

				try {
					JSONArray jArray = new JSONArray(jsonString);
					cacheJSONArray(jArray);
				} catch (JSONException e) {
					EtaLog.e(TAG, "", e);
		            return Response.fromError(new ParseError(e, JSONArray.class));
				}
			}
			
            return Response.fromSuccess(jsonString, getCache());
            
		} else {
			
			try {
				JSONObject jObject = new JSONObject(jsonString);
				EtaError e = EtaError.fromJSON(jObject);
				return Response.fromError(e);
	        } catch (Exception e) {
	            return Response.fromError(new ParseError(e, JSONObject.class));
	        }
			
		}
			
	}

	@Override
	protected Response<String> parseCache(Cache c) {
		
		// This method of guessing isn't perfect, but atleast we won't get false data from cache
        String[] path = getUrl().split("/");
        String cacheString = null;
        if (mFilterTypes.containsKey(path[path.length-1])) {
        	Response<JSONArray> cacheArray = getJSONArray(c);
        	if (cacheArray != null) {
            	cacheString = cacheArray.result.toString();
        	}
        } else if (mFilterTypes.containsKey(path[path.length-2])) {
        	Response<JSONObject> cacheObject = getJSONObject(c);
        	if (cacheObject != null) {
        		cacheString = cacheObject.result.toString();
        	}
        }
        
        if (cacheString != null) {

    		Response<String> cache = Response.fromSuccess(cacheString, null);
    		
    		return cache;
    		
        }
		return null;
	}
	
	
	
}
