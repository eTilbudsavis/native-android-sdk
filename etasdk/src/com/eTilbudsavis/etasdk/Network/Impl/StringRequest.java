/*******************************************************************************
* Copyright 2014 eTilbudsavis
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.eTilbudsavis.etasdk.Network.Impl;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Network.Cache;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.NetworkResponse;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.Response;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class StringRequest extends Request<String> {

	/** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE = String.format("text/plain; charset=%s", DEFAULT_PARAMS_ENCODING);
    
    private String mRequestBody;
    
    private Priority mPriority = Priority.MEDIUM;
    
	public StringRequest(String url, Listener<String> listener) {
		super(Method.GET, url, listener);
	}
	
	public StringRequest(Method method, String url, String requestBody, Listener<String> listener) {
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
        
		mCache.put(Utils.buildQueryString(this), new Cache.Item(string, getCacheTTL()));
		
        Response<String> r = Response.fromSuccess(string, mCache);
        
        log(response.statusCode, response.headers, r.result, r.error);
        
        return r;
	}

	@Override
	protected Response<String> parseCache(Cache c) {
		
		String url = Utils.buildQueryString(this);
		Cache.Item ci = c.get(url);
		Response<String> r = null;
		if (ci != null && ci.object instanceof String ) {
			r = Response.fromSuccess((String)ci.object, null);
			log(0, new HashMap<String, String>(), r.result, r.error);
		}
		
		return r;
	}

	protected void log(int statusCode, Map<String, String> headers, String successData, EtaError error) {
		
		try {
			
			// Server Response
			JSONObject response = new JSONObject();
			response.put("statuscode", statusCode);
			int sl = (successData == null ? 0 : successData.length());
			response.put("success", "length: " + sl);
			response.put("error", error == null ? null : error.toJSON());
			response.put("headers", headers == null ? new JSONObject() : new JSONObject(headers));
			
			// Client request
			JSONObject request = new JSONObject();
			request.put("method", getMethod().toString());
			request.put("url", Utils.buildQueryString(this));
			request.put(HTTP.CONTENT_TYPE, getBodyContentType());
			request.put("headers", new JSONObject(getHeaders()));
			request.put("time", Utils.parseDate(new Date()));
			request.put("response", response);
			
			getLog().setSummary(request);
			
		} catch (JSONException e) {
			EtaLog.e(TAG, e);
		}
		
	}
	
	
}
