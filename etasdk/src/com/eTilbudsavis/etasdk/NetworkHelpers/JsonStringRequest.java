package com.eTilbudsavis.etasdk.NetworkHelpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;
import com.eTilbudsavis.etasdk.NetworkInterface.Response;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Method;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class JsonStringRequest extends JsonRequest<String>{

    private static final long CACHE_TTL = 3 * Utils.MINUTE_IN_MILLIS;
    
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
			
			try {
				if (json.startsWith("{") && json.endsWith("}")) {
					putJsonObject(new JSONObject(json));
				} else if (json.startsWith("[") && json.endsWith("]")) {
					putJsonArray(new JSONArray(json));
				}
				
			} catch (JSONException e) {
				EtaLog.d(TAG, e);
			}
			
            return Response.fromSuccess(json, null, false);
        } catch (Exception e) {
            return Response.fromError(new ParseError(this, response));
        }
	}
	
	
	
}
