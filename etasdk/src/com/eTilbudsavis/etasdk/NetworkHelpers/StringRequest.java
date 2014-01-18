package com.eTilbudsavis.etasdk.NetworkHelpers;

import java.io.UnsupportedEncodingException;

import com.eTilbudsavis.etasdk.NetworkInterface.Cache;
import com.eTilbudsavis.etasdk.NetworkInterface.NetworkResponse;
import com.eTilbudsavis.etasdk.NetworkInterface.Request;
import com.eTilbudsavis.etasdk.NetworkInterface.Response;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Method;
import com.eTilbudsavis.etasdk.NetworkInterface.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class StringRequest extends Request<String> {

	/** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE = String.format("text/plain; charset=%s", DEFAULT_PARAMS_ENCODING);
    
    private String mRequestBody;
    
    private Priority mPriority = Priority.MEDIUM;
    
    /**
     * 
     * @param url
     * @param listener
     */
	public StringRequest(String url, Listener<String> listener) {
		super(Method.GET, url, listener);	
		
	}
	
	/**
	 * 
	 * @param method
	 * @param url
	 * @param params
	 * @param listener
	 */
	public StringRequest(int method, String url, String requestBody, Listener<String> listener) {
		super(method, url, listener);
		boolean nonBodyRequest = (method == Method.GET || method == Method.DELETE);
		if (nonBodyRequest && requestBody != null) {
			EtaLog.d(TAG, "GET and DELETE requests doesn't take a body, and will be ignored.\n"
					+ "Please append any parameters to Request.putQueryParameters()");
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

    public StringRequest setPriority(Priority p) {
    	mPriority = p;
    	return this;
    }

    @Override
    public Priority getPriority() {
    	return mPriority;
    }
    
	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response) {
		String string;
        try {
            string = new String(response.data, getParamsEncoding());
        } catch (UnsupportedEncodingException e) {
            string = new String(response.data);
        }
        
		mCache.put(Utils.buildQueryString(this), new Cache.Item(string, DEFAULT_CACHE_TTL));
		        
        return Response.fromSuccess(string, null, false);
	}
	
}
