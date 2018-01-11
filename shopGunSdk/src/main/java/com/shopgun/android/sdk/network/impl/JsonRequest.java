/*******************************************************************************
 * Copyright 2015 ShopGun
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

package com.shopgun.android.sdk.network.impl;

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.Response.Listener;
import com.shopgun.android.sdk.utils.Constants;

import java.io.UnsupportedEncodingException;

public abstract class JsonRequest<T> extends Request<T> {

    public static final String TAG = Constants.getTag(JsonRequest.class);

    /** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE = String.format("application/json; charset=%s", DEFAULT_PARAMS_ENCODING);

    private String mRequestBody;

    private Priority mPriority = Priority.MEDIUM;

    public JsonRequest(String url, Listener<T> listener) {
        super(Method.GET, url, listener);

    }

    public JsonRequest(Method method, String url, String requestBody, Listener<T> listener) {
        super(method, url, listener);
        boolean nonBodyRequest = (method == Method.GET || method == Method.DELETE);
        if (nonBodyRequest && requestBody != null) {
            SgnLog.i(TAG, "GET and DELETE requests doesn't take a body, and will be ignored.\n"
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

    /**
     * Returns a complete printable representation of this Request, e.g:
     *
     * <ul>
     *      <li>GET: https://api.etilbudsavis.dk/v2/catalogs/{catalog_id}?param1=value1&amp;param2=value2</li>
     *      <li>PUT: https://api.etilbudsavis.dk/v2/catalogs/{catalog_id}?param1=value1&amp;param2=value2&amp;body=[json_string]</li>
     * </ul>
     *
     * <p>Body data is appended as the last query parameter for convenience, as
     * seen in the examples above.</p>
     */
    @Override
    public String toString() {
        if (mRequestBody != null) {
            return super.toString() + "&body=[" + mRequestBody + "]";
        } else {
            return super.toString();
        }
    }

}
