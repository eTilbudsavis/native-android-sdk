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

import android.text.TextUtils;

import com.shopgun.android.sdk.api.Parameters;
import com.shopgun.android.sdk.network.Cache;
import com.shopgun.android.sdk.network.NetworkResponse;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.Response;
import com.shopgun.android.sdk.network.Response.Listener;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class JsonArrayRequest extends JsonRequest<JSONArray> {

    /**
     * The default limit for API calls.<br>
     * By using this limit, queries are more likely to hit a cache on the server, hence making queries faster */
    public static final int DEFAULT_LIMIT = 24;
    private static final String ERROR_OFFSET_NEGATIVE = "Offset may not be negative";
    private static final String ERROR_LIMIT_NEGATIVE = "Limit may not be negative";
    private static final long CACHE_TTL = TimeUnit.MINUTES.toMillis(3);

    public JsonArrayRequest(String url, Listener<JSONArray> listener) {
        super(Method.GET, url, null, listener);
        init();
    }

    public JsonArrayRequest(Method method, String url, Listener<JSONArray> listener) {
        super(method, url, null, listener);
        init();
    }

    public JsonArrayRequest(Method method, String url, JSONArray requestBody, Listener<JSONArray> listener) {
        super(method, url, requestBody == null ? null : requestBody.toString(), listener);
        init();
    }

    public JsonArrayRequest(Method method, String url, JSONObject requestBody, Listener<JSONArray> listener) {
        super(method, url, requestBody == null ? null : requestBody.toString(), listener);
        init();
    }

    private void init() {
        setOffset(0);
        setLimit(DEFAULT_LIMIT);
    }

    @Override
    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {

        String jsonString = "";
        try {
            try {
                jsonString = new String(response.data, getParamsEncoding());
            } catch (UnsupportedEncodingException e) {
                jsonString = new String(response.data);
            }

            Response<JSONArray> r = null;
            if (Utils.isSuccess(response.statusCode)) {
                // Parse into array if it's successful
                JSONArray jArray = new JSONArray(jsonString);
                r = Response.fromSuccess(jArray, getCache());
                JsonCacheHelper.cacheJSONArray(this, r.result);

            } else {
                // Parse into object if it failed.
                JSONObject jObject = new JSONObject(jsonString);
                ShopGunError e = ShopGunError.fromJSON(jObject);
                r = Response.fromError(e);
            }

            return r;

        } catch (Exception e) {
            return Response.fromError(new ParseError(e, JSONArray.class));
        }
    }

    @Override
    public Response<JSONArray> parseCache(Cache c) {
        return JsonCacheHelper.getJSONArray(this, c);
    }

    @Override
    public long getCacheTTL() {
        return CACHE_TTL;
    }

    /**
     * Set the order the API should order the data by
     * @param order parameter to order data by
     * @return this object
     */
    public Request<?> setOrderBy(String order) {
        getParameters().put(Parameters.ORDER_BY, order);
        return this;
    }

    /**
     * Set a list of "order_by" parameters that the API should order the data by.
     * @param order parameters to order data by
     * @return this object
     */
    public Request<?> setOrderBy(List<String> order) {
        if (!order.isEmpty()) {
            String tmp = TextUtils.join(",", order);
            getParameters().put(Parameters.ORDER_BY, tmp);
        }
        return this;
    }

    /**
     * Get the order the API should order data by
     * @return the order as a String, or null if no order have been given.
     */
    public String getOrderBy() {
        return getParameters().get(Parameters.ORDER_BY);
    }

    /**
     * The API relies on pagination for retrieving data. Therefore you need to
     * define the offset to the first item in the requested list, when querying for data.
     * If no offset is set it will default to 0.
     * @param offset to first item in list
     * @return this object
     */
    public Request<?> setOffset(int offset) {
        if (offset < 0) {
            throw new IllegalStateException(ERROR_OFFSET_NEGATIVE);
        }
        getParameters().put(Parameters.OFFSET, String.valueOf(offset));
        return this;
    }

    /**
     * Get the offset parameter used for the query.
     * @return offset
     */
    public int getOffset() {
        return Integer.valueOf(getParameters().get(Parameters.OFFSET));
    }

    /**
     * The API relies on pagination for retrieving data. Therefore you need to
     * define a limit for the data you want to retrieve. If no limit is set
     * this will default to {@link #DEFAULT_LIMIT DEFAULT_LIMIT} if no limit is set.
     * @param limit A limit for the number of items returned
     * @return this object
     */
    public Request<?> setLimit(int limit) {
        if (limit < 0) {
            throw new IllegalStateException(ERROR_LIMIT_NEGATIVE);
        }
        getParameters().put(Parameters.LIMIT, String.valueOf(limit));
        return this;
    }

    /**
     * Get the upper limit on how many items the API should return.
     * @return max number of items API should return
     */
    public int getLimit() {
        return Integer.valueOf(getParameters().get(Parameters.LIMIT));
    }

    /**
     * Set a parameter for what specific id's to get from a given endpoint.<br><br>
     *
     *
     * @param    type The id type, e.g. Parameters.CATALOG_IDS
     * @param    ids The id's to get
     * @return this object
     */
    public Request<?> setIds(String type, Set<String> ids) {
        if (!ids.isEmpty()) {
            String idList = TextUtils.join(",", ids);
            getParameters().put(type, idList);
        }
        return this;
    }

}
