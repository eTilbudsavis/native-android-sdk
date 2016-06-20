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

import com.shopgun.android.sdk.network.Cache;
import com.shopgun.android.sdk.network.NetworkResponse;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.Response;
import com.shopgun.android.sdk.utils.Utils;

public class ByteRequest extends Request<byte[]> {

    private byte[] mRequestBody;
    private String mProtocolContentType;
    private Priority mPriority = Priority.MEDIUM;

    public ByteRequest(String url, Response.Listener<byte[]> listener) {
        super(Method.GET, url, listener);
    }

    public ByteRequest(Method method, String url, Response.Listener<byte[]> listener) {
        super(method, url, listener);
    }

    @Override
    public String getBodyContentType() {
        return mProtocolContentType;
    }

    public String setBodyContentType(String bodyContentType) {
        return mProtocolContentType = bodyContentType;
    }

    @Override
    public byte[] getBody() {
        return mRequestBody;
    }

    public ByteRequest setBody(byte[] body) {
        mRequestBody = body;
        return this;
    }

    @Override
    public Priority getPriority() {
        return mPriority;
    }

    public ByteRequest setPriority(Priority p) {
        mPriority = p;
        return this;
    }

    @Override
    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        String url = Utils.requestToUrlAndQueryString(this);
        Cache.Item c = new Cache.Item(response.data, getCacheTTL());
        getCache().put(url, c);
        return Response.fromSuccess(response.data, getCache());
    }

    @Override
    protected Response<byte[]> parseCache(Cache c) {
        String url = Utils.requestToUrlAndQueryString(this);
        Cache.Item ci = c.get(url);
        if (ci != null && ci.object instanceof byte[]) {
            return Response.fromSuccess((byte[]) ci.object, null);
        }
        return null;
    }

}
