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

package com.shopgun.android.sdk.network.mock;

import android.content.Context;

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.network.NetworkResponse;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.utils.Api;
import com.shopgun.android.utils.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public abstract class MockApiNetworkResponse {

    public static final String TAG = MockApiNetworkResponse.class.getSimpleName();

    protected static final String FILE_CATALOG_LIST = "list-catalog.json";
    protected static final String FILE_STORE_LIST = "list-store.json";
    protected static final String FILE_OFFER_LIST = "list-offer.json";
    protected static final String FILE_DEALER_LIST = "list-dealer.json";
    protected static final String FILE_SESSIONS = "session.json";
    protected static final String FILE_TYPEAHEAD = "typeahead.json";
    protected static final String FILE_CURRENCY = "currency.json";
    protected static final String FILE_COUNTRIES = "countries.json";

    protected static HashMap<String, HashSet<String>> mActions = new HashMap<String, HashSet<String>>();
    protected static HashMap<String, HashSet<String>> mModelActions = new HashMap<String, HashSet<String>>();

    protected Context mContext;
    protected Request<?> mRequest;
    protected PathHelper mPath;

    protected MockApiNetworkResponse(Context mContext, Request<?> request) {
        this.mContext = mContext;
        mRequest = request;
        mPath = new PathHelper(request);
    }

    public abstract NetworkResponse getResponse();

    public static MockApiNetworkResponse create(Context ctx, Request request, String type) throws ShopGunError {

        if ("offers".equals(type)) {
            return new MockApiOfferResponse(ctx, request);
        } else if ("catalogs".equals(type)) {
            return new MockApiCatalogResponse(ctx, request);
        } else if ("stores".equals(type)) {
            return new MockApiStoreResponse(ctx, request);
        } else if ("dealers".equals(type)) {
            return new MockApiDealerResponse(ctx, request);
        } else if ("currencies".equals(type)) {
            return new MockApiSimpleResponse(ctx, request, FILE_CURRENCY);
        } else if ("countries".equals(type)) {
            return new MockApiSimpleResponse(ctx, request, FILE_COUNTRIES);
        }

        return new MockApiUnsupportedResponse(ctx, request);
    }

    protected NetworkResponse getUnsupportedResponse() {
        return new MockUnsupportedNetworkResponse(mRequest);
    }

    protected NetworkResponse getItem(JSONArray array,  String id) {
        // there is no further actions, lets get the specified catalog
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject item = array.getJSONObject(i);
                if (id.equals(item.getString("id"))) {
                    return new NetworkResponse(200, item.toString().getBytes(), null);
                }
            } catch (Exception e) {
                // Ignore
            }
        }
        return getUnsupportedResponse();
    }

    protected JSONArray getAssetJSONArray(String name) {
        try {
            return new JSONArray(getAssetAsString(name));
        } catch (Exception e) {
            SgnLog.e(getClass().getSimpleName(), e.getMessage(), e);
        }
        return new JSONArray();
    }

    protected JSONObject getAssetJSONObject(String name) {
        try {
            return new JSONObject(getAssetAsString(name));
        } catch (Exception e) {
            SgnLog.e(getClass().getSimpleName(), e.getMessage(), e);
        }
        return new JSONObject();
    }

    protected String getAssetAsString(String name) {
        return getAssetAsString(mContext, name);
    }

    protected byte[] getAssetAsByteArray(String name) {
        return getAssetAsByteArray(mContext, name);
    }

    public static String getAssetAsString(Context ctx, String name) {
        try {
            InputStream is = ctx.getAssets().open(name);
            return readString(is);
        } catch (Exception e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public static byte[] getAssetAsByteArray(Context ctx, String name) {
        try {
            return readBytes(ctx.getAssets().open(name));
        } catch (Exception e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    public static String readString(final InputStream inputStream) {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            SgnLog.e(TAG, e.getMessage(), e);
        }
        return total.toString();
    }

    public static byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static JSONArray trimToOffsetAndLimit(JSONArray array, Request<?> request) {

        JSONArray a = new JSONArray();
        int limit = 0;
        int offset = 0;
        try {
            Map<String, String> map = request.getParameters();
            limit = Integer.valueOf(map.get(Api.Param.LIMIT));
            offset = Integer.valueOf(map.get(Api.Param.OFFSET));
        } catch (NumberFormatException e) {
            SgnLog.e(TAG, e.getMessage(), e);
            return array;
        }

        SgnLog.d(TAG, String.format("offset:%s, limit:%s", offset, limit));

        if (offset > array.length()) {
            return a;
        }

        int max = Math.min(array.length(), offset+limit);
        for (int i = offset; i < max; i++) {
            try {
                a.put(array.get(i));
            } catch (JSONException e) {
                SgnLog.e(TAG, e.getMessage(), e);
            }
        }
        return a;
    }
    
    public static JSONArray filterByIds(JSONArray totalList, Request<?> request, String idParam) {

        String ids = request.getParameters().get(idParam);
        if (ids != null) {
            String[] split = TextUtils.split(ids, ",");
            JSONArray tmp = new JSONArray();
            for (String s : split) {
                for (int i = 0; i < totalList.length(); i++) {
                    JSONObject ernObject;
                    try {
                        ernObject = totalList.getJSONObject(i);
                        if (s.equals(ernObject.getString("id"))) {
                            tmp.put(ernObject);
                        }
                    } catch (JSONException e) {
                        SgnLog.e(TAG, e.getMessage(), e);
                    }
                }
            }
            return tmp;
        }
        return null;
    }

}
