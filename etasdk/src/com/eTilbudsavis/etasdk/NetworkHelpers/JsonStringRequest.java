package com.eTilbudsavis.etasdk.NetworkHelpers;

import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;
import com.eTilbudsavis.etasdk.NetworkInterface.Response;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;

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
            return Response.fromSuccess(new String(response.data), null, false);
        } catch (Exception e) {
            return Response.fromError(new ParseError(this, response));
        }
	}
	
}
