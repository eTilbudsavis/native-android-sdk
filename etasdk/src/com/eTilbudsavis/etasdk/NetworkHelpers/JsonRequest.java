package com.eTilbudsavis.etasdk.NetworkHelpers;

import java.io.UnsupportedEncodingException;

import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.EtaLog;

public abstract class JsonRequest<T> extends Request<T> {
	
	/** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE = String.format("application/json; charset=%s", DEFAULT_PARAMS_ENCODING);

    private String mRequestBody;
    
    private Priority mPriority = Priority.MEDIUM;
    
    public JsonRequest(String url, Listener<T> listener) {
		super(Method.GET, url, listener);
		
	}
    
    public JsonRequest(int method, String url, String requestBody, Listener<T> listener) {
		super(method, url, listener);
		boolean nonBodyRequest = (method == Method.GET || method == Method.DELETE);
		if (nonBodyRequest && requestBody != null) {
			EtaLog.d(TAG, "GET and DELETE requests doesn't take a body, and will be ignored.\n"
					+ "Please append any GET and DELETE parameters to Request.putQueryParameters()");
		}
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
    
    public Request<T> setPriority(Priority p) {
    	mPriority = p;
    	return this;
    }

    @Override
    public Priority getPriority() {
    	return mPriority;
    }
    
}
