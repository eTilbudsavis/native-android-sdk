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

import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.api.Parameters;
import com.shopgun.android.sdk.network.Cache;
import com.shopgun.android.sdk.network.NetworkResponse;
import com.shopgun.android.sdk.network.Response;
import com.shopgun.android.sdk.network.Response.Listener;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.utils.SgnUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class JsonStringRequest extends JsonRequest<String> {

    public static final String TAG = Constants.getTag(JsonStringRequest.class);

    // Define catchable types
    private static Map<String, String> mFilterTypes = new HashMap<String, String>();

    static {
        mFilterTypes.put("catalogs", Parameters.CATALOG_IDS);
        mFilterTypes.put("offers", Parameters.OFFER_IDS);
        mFilterTypes.put("dealers", Parameters.DEALER_IDS);
        mFilterTypes.put("stores", Parameters.STORE_IDS);
    }

    public JsonStringRequest(String url, Listener<String> listener) {
        super(url, listener);
    }

    public JsonStringRequest(Method method, String url, String requestBody, Listener<String> listener) {
        super(method, url, requestBody, listener);
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {

        String jsonString = null;
        try {
            jsonString = new String(response.data, getParamsEncoding()).trim();
        } catch (UnsupportedEncodingException e) {
            jsonString = new String(response.data).trim();
        }

        if (SgnUtils.isSuccess(response.statusCode)) {

            if (jsonString.startsWith("{") && jsonString.endsWith("}")) {

                try {
                    JSONObject jObject = new JSONObject(jsonString);
                    JsonCacheHelper.cacheJSONObject(this, jObject);
                } catch (JSONException e) {
                    return Response.fromError(new ParseError(e, JSONObject.class));
                }

            } else if (jsonString.startsWith("[") && jsonString.endsWith("]")) {

                try {
                    JSONArray jArray = new JSONArray(jsonString);
                    JsonCacheHelper.cacheJSONArray(this, jArray);
                } catch (JSONException e) {
                    return Response.fromError(new ParseError(e, JSONArray.class));
                }
            }

            return Response.fromSuccess(jsonString, getCache());

        } else {

            try {
                JSONObject jObject = new JSONObject(jsonString);
                ShopGunError e = ShopGunError.fromJSON(jObject);
                return Response.fromError(e);
            } catch (Exception e) {
                return Response.fromError(new ParseError(e, JSONObject.class));
            }

        }

    }

    @Override
    protected Response<String> parseCache(Cache c) {

        // This method of guessing isn't perfect, but atleast we won't get false data from cache
        String[] path = getUrl().split("/");
        String cacheString = null;
        if (mFilterTypes.containsKey(path[path.length - 1])) {
            Response<JSONArray> cacheArray = JsonCacheHelper.getJSONArray(this, c);
            if (cacheArray != null) {
                cacheString = cacheArray.result.toString();
            }
        } else if (mFilterTypes.containsKey(path[path.length - 2])) {
            Response<JSONObject> cacheObject = JsonCacheHelper.getJSONObject(this, c);
            if (cacheObject != null) {
                cacheString = cacheObject.result.toString();
            }
        }

        if (cacheString != null) {

            Response<String> cache = Response.fromSuccess(cacheString, null);

            return cache;

        }
        return null;
    }


}
