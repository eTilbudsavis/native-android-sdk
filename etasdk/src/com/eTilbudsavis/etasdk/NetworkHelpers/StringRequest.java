package com.eTilbudsavis.etasdk.NetworkHelpers;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.NetworkInterface.Response;
import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;

public class StringRequest extends Request<String> {

	/** Return interface */
	private final Listener<String> mListener;
	
	
	public StringRequest(int method, String url, Listener<String> listener) {
		super(method, url, listener);	
		mListener = listener;
		
	}

	public StringRequest(int method, String url, Map<String, String> params, Listener<String> listener) {
		super(method, url, listener);
		mListener = listener;
		
	}

	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response) {
		String parsed;
        try {
            parsed = new String(response.data, getParamsEncoding());
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.fromSuccess(parsed, false);
	}
	
	@Override
	protected void deliverResponse(String response) {
		// TODO Auto-generated method stub
		
	}

}
