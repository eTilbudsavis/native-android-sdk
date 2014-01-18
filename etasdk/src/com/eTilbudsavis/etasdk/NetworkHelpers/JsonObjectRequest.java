package com.eTilbudsavis.etasdk.NetworkHelpers;

import java.util.Set;

import org.json.JSONObject;

import android.os.Bundle;

import com.eTilbudsavis.etasdk.NetworkInterface.Cache;
import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;
import com.eTilbudsavis.etasdk.NetworkInterface.Response;
import com.eTilbudsavis.etasdk.NetworkInterface.Cache.Item;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.Param;
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
            putJsonObject(item);
            return Response.fromSuccess(item, null, false);
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

		String url = getUrl();
		String[] path = url.split("/");
		
		String id = path[path.length-1];
		String type = path[path.length-2];
		
		String ern = buildErn(type, id);
		Item ci = c.get(ern);
		if (c != null && ci.object instanceof JSONObject) {
			return Response.fromSuccess((JSONObject)ci.object, null, true);
		}
		return null;
		
	}
	
}
