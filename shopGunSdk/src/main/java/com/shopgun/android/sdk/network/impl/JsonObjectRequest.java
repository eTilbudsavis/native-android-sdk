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

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.network.Cache;
import com.shopgun.android.sdk.network.NetworkResponse;
import com.shopgun.android.sdk.network.Response;
import com.shopgun.android.sdk.network.Response.Listener;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

public class JsonObjectRequest extends JsonRequest<JSONObject> {

    public static final String TAG = Constants.getTag(JsonObjectRequest.class);

    private static final long CACHE_TTL = TimeUnit.MINUTES.toMillis(3);

    public JsonObjectRequest(String url, Listener<JSONObject> listener) {
        super(url, listener);
    }

    public JsonObjectRequest(Method method, String url, JSONObject requestBody, Listener<JSONObject> listener) {
        super(method, url, requestBody == null ? null : requestBody.toString(), listener);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {

        String jsonString = null;

        try {

            try {
                jsonString = new String(response.data, getParamsEncoding());
            } catch (UnsupportedEncodingException e) {
                jsonString = new String(response.data);
            }

            JSONObject item = new JSONObject(jsonString);
            Response<JSONObject> r = null;
            if (Utils.isSuccess(response.statusCode)) {
                JsonCacheHelper.cacheJSONObject(this, item);
                r = Response.fromSuccess(item, getCache());
            } else {

                ShopGunError e = ShopGunError.fromJSON(item);
                r = Response.fromError(e);
            }

            return r;

        } catch (JSONException e) {
            return Response.fromError(new ParseError(e, JSONObject.class));
        }

    }

    @Override
    public long getCacheTTL() {
        return CACHE_TTL;
    }

    @Override
    public Response<JSONObject> parseCache(Cache c) {
        Response<JSONObject> cache = JsonCacheHelper.getJSONObject(this, c);
        return cache;
    }

}
