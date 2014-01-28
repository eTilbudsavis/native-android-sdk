package com.eTilbudsavis.etasdk.NetworkHelpers;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.NetworkInterface.Cache;
import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;
import com.eTilbudsavis.etasdk.NetworkInterface.Response;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class JsonStringRequest extends JsonRequest<String>{
	
	public JsonStringRequest(String url, Listener<String> listener) {
		super(url, listener);
	}
	
	public JsonStringRequest(int method, String url, String requestBody, Listener<String> listener) {
		super(method, url, requestBody, listener);
	}
	
	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response) {
		try {
			String json = new String(response.data);
			
			if (Utils.isSuccess(response.statusCode)) {
				
				try {
					if (json.startsWith("{") && json.endsWith("}")) {
						JSONObject jObject = new JSONObject(json);
						log(response.statusCode, response.headers, jObject, null);
						putJSON(jObject);
					} else if (json.startsWith("[") && json.endsWith("]")) {
						JSONArray jArray = new JSONArray(json);
						log(response.statusCode, response.headers, jArray, null);
						putJSON(jArray);
					}
					
				} catch (JSONException e) {
					EtaLog.d(TAG, e);
				}
	            return Response.fromSuccess(json, getCache());
	            
			} else {
				
				Response<String> r = Response.fromError(EtaError.fromJSON(new JSONObject(json)));
				log(response.statusCode, response.headers, new JSONObject(), r.error);
				return r;
				
			}
			
        } catch (Exception e) {
            return Response.fromError(new ParseError(e));
        }
	}

	@Override
	protected Response<String> parseCache(Cache c) {
		
		// This method of guessing isn't perfect, but atleast we won't get false data from cache
        String[] path = getUrl().split("/");
        String cacheString = null;
        if (getFilterTypes().containsKey(path[path.length-1])) {
        	Response<JSONArray> cacheArray = getJSONArray(c);
        	if (cacheArray != null) {
            	cacheString = cacheArray.result.toString();
        	}
        } else if (getFilterTypes().containsKey(path[path.length-2])) {
        	Response<JSONObject> cacheObject = getJSONObject(c);
        	if (cacheObject != null) {
        		cacheString = cacheObject.result.toString();
        	}
        }
        
        if (cacheString != null) {

    		Response<String> cache = Response.fromSuccess(cacheString, null);
    		

			try {
				
				if (cache.result.startsWith("{") && cache.result.endsWith("}")) {
					log(0, new HashMap<String, String>(), new JSONObject(cache.result), null);
				} else if (cache.result.startsWith("[") && cache.result.endsWith("]")) {
					log(0, new HashMap<String, String>(), new JSONArray(cache.result), null);
				}
				
			} catch (JSONException e) {
				EtaLog.d(TAG, e);
			}
    		
    		return cache;
    		
        }
		return null;
	}
	
	
	
}
