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
package com.eTilbudsavis.etasdk.network.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.network.Cache;
import com.eTilbudsavis.etasdk.network.EtaError;
import com.eTilbudsavis.etasdk.network.NetworkResponse;
import com.eTilbudsavis.etasdk.network.Response;
import com.eTilbudsavis.etasdk.network.Response.Listener;
import com.eTilbudsavis.etasdk.utils.Api.Param;
import com.eTilbudsavis.etasdk.utils.Utils;

public class JsonStringRequest extends JsonRequest<String>{

	public static final String TAG = Eta.TAG_PREFIX + JsonStringRequest.class.getSimpleName();

	// Define catchable types
	private static Map<String, String> mFilterTypes = new HashMap<String, String>();
	
	static {
		mFilterTypes.put("catalogs", Param.CATALOG_IDS);
		mFilterTypes.put("offers", Param.OFFER_IDS);
		mFilterTypes.put("dealers", Param.DEALER_IDS);
		mFilterTypes.put("stores", Param.STORE_IDS);
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
					JsonCacheHelper.cacheJSONObject(this, jObject);
				} catch (JSONException e) {
					EtaLog.e(TAG, "", e);
		            return Response.fromError(new ParseError(e, JSONObject.class));
				}
				
			} else if (jsonString.startsWith("[") && jsonString.endsWith("]")) {

				try {
					JSONArray jArray = new JSONArray(jsonString);
					JsonCacheHelper.cacheJSONArray(this, jArray);
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
        	Response<JSONArray> cacheArray = JsonCacheHelper.getJSONArray(this, c);
        	if (cacheArray != null) {
            	cacheString = cacheArray.result.toString();
        	}
        } else if (mFilterTypes.containsKey(path[path.length-2])) {
        	Response<JSONObject> cacheObject = JsonCacheHelper.getJSONObject(this, c);
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
