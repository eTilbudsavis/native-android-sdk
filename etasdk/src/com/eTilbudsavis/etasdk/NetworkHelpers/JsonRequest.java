package com.eTilbudsavis.etasdk.NetworkHelpers;

import java.io.UnsupportedEncodingException;

import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;

public abstract class JsonRequest<T> extends Request<T> {
	
	/** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE = String.format("application/json; charset=%s", DEFAULT_PARAMS_ENCODING);

    private String mRequestBody;

    public JsonRequest(String url, Listener<T> listener) {
		super(Method.GET, url, listener);
		
	}
    
    public JsonRequest(int method, String url, String requestBody, Listener<T> listener) {
		super(method, url, listener);
		mRequestBody = requestBody;
	}
    
    @Override
    public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }
    
    @Override
    public byte[] getBody() {
        try {
            return mRequestBody == null ? null : mRequestBody.getBytes(DEFAULT_PARAMS_ENCODING);
        } catch (UnsupportedEncodingException uee) {
            return null;
        }
    }

    @Override
    protected void deliverResponse(T response) {
//        getListener().onComplete(isCache, response, error);
    }

}
