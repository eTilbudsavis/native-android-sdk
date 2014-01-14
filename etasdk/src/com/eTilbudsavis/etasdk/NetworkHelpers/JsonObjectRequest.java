package com.eTilbudsavis.etasdk.NetworkHelpers;

import org.json.JSONObject;

import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;
import com.eTilbudsavis.etasdk.NetworkInterface.Response;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;

public class JsonObjectRequest extends JsonRequest<JSONObject>{
	
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
            return Response.fromSuccess(new JSONObject(jsonString), null, false);
        } catch (Exception e) {
            return Response.fromError(new ParseError(response));
        }
	}
	
}
