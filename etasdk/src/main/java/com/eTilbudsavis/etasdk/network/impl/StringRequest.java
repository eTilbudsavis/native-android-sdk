/*******************************************************************************
 * Copyright 2015 eTilbudsavis
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
 ******************************************************************************/
package com.eTilbudsavis.etasdk.network.impl;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.network.Cache;
import com.eTilbudsavis.etasdk.network.NetworkResponse;
import com.eTilbudsavis.etasdk.network.Request;
import com.eTilbudsavis.etasdk.network.Response;
import com.eTilbudsavis.etasdk.network.Response.Listener;
import com.eTilbudsavis.etasdk.utils.Utils;

import java.io.UnsupportedEncodingException;

public class StringRequest extends Request<String> {

    public static final String TAG = Constants.getTag(StringRequest.class);

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
            EtaLog.i(TAG, "GET and DELETE requests doesn't take a body, and will be ignored.\n"
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

    @Override
    public Priority getPriority() {
        return mPriority;
    }

    public StringRequest setPriority(Priority p) {
        mPriority = p;
        return this;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String string;
        try {
            string = new String(response.data, getParamsEncoding());
        } catch (UnsupportedEncodingException e) {
            string = new String(response.data);
        }

        String url = Utils.requestToUrlAndQueryString(this);
        Cache.Item c = new Cache.Item(string, getCacheTTL());
        getCache().put(url, c);

        Response<String> r = Response.fromSuccess(string, getCache());

        return r;
    }

    @Override
    protected Response<String> parseCache(Cache c) {
        String url = Utils.requestToUrlAndQueryString(this);
        Cache.Item ci = c.get(url);
        if (ci != null && ci.object instanceof String) {
            return Response.fromSuccess((String) ci.object, null);
        }
        return null;
    }

}
