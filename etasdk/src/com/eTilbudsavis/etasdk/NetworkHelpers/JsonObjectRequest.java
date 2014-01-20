package com.eTilbudsavis.etasdk.NetworkHelpers;

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
		try {
            String jsonString = new String(response.data);
            JSONObject item = new JSONObject(jsonString);
            putJSON(item);
            return Response.fromSuccess(item, mCache);
        } catch (Exception e) {
            return Response.fromError(new ParseError(this, response));
        }
	}

	@Override
	public long getCacheTTL() {
		return CACHE_TTL;
	}
	
	@Override
	public Response<JSONObject> parseCache(Cache c) {
		return getJSONObject(c);
	}
	
}
