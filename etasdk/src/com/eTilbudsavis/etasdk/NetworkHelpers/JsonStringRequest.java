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
						putJSON(new JSONObject(json));
					} else if (json.startsWith("[") && json.endsWith("]")) {
						putJSON(new JSONArray(json));
					}
					
				} catch (JSONException e) {
					EtaLog.d(TAG, e);
				}
	            return Response.fromSuccess(json, getCache());
	            
			} else {
				
				return Response.fromError(EtaError.fromJSON(new JSONObject(json)));
				
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
    		
    		return cache;
    		
        }
		return null;
	}
	
	
	
}
