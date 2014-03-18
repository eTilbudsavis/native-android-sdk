package com.eTilbudsavis.etasdk.NetworkHelpers;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.NetworkInterface.Cache;
import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;
import com.eTilbudsavis.etasdk.NetworkInterface.Response;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class JsonObjectRequest extends JsonRequest<JSONObject>{

    private static final long CACHE_TTL = 3 * Utils.MINUTE_IN_MILLIS;
    
	public JsonObjectRequest(String url, Listener<JSONObject> listener) {
		super(url, listener);
	}
	
	public JsonObjectRequest(int method, String url, JSONObject requestBody, Listener<JSONObject> listener) {
		super(method, url, requestBody == null ? null : requestBody.toString(), listener);
	}
	
	@Override
	protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
		
		String jsonString = null;
		
		try {
			
			try {
				jsonString = new String(response.data, getParamsEncoding());
			} catch (UnsupportedEncodingException e) {
				jsonString = new String(response.data);
			}
			
            JSONObject item = new JSONObject(jsonString);
			Response<JSONObject> r = null;
            if (Utils.isSuccess(response.statusCode)) {
                putJSON(item);
                r = Response.fromSuccess(item, mCache);
            } else {
            	EtaError e = EtaError.fromJSON(item);
            	r = Response.fromError(e);
            }
            
            log(response.statusCode, response.headers, r.result, r.error);
            
            return r;
            
        } catch (JSONException e) {
            return Response.fromError(new ParseError(e, JSONObject.class));
        }
		
	}

	@Override
	public long getCacheTTL() {
		return CACHE_TTL;
	}
	
	@Override
	public Response<JSONObject> parseCache(Cache c) {
		Response<JSONObject> cache = getJSONObject(c);
		if (cache != null) {
			log(0, new HashMap<String, String>(), cache.result, cache.error);
		}
		return cache;
	}
	
}
