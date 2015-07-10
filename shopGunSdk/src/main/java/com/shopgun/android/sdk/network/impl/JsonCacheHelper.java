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

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.network.Cache;
import com.shopgun.android.sdk.network.Cache.Item;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.Response;
import com.shopgun.android.sdk.utils.Api;
import com.shopgun.android.sdk.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class JsonCacheHelper {

    public static final String TAG = Constants.getTag(JsonCacheHelper.class);

    private static final String ERN_FORMAT = "ern:%s:%s";
    private static Set<String> mErnTypes = new HashSet<String>();

    static {
        mErnTypes.add("catalogs");
        mErnTypes.add("offers");
        mErnTypes.add("dealers");
        mErnTypes.add("stores");
        mErnTypes.add("shoppinglists");
        mErnTypes.add("items");
    }

    public static Response<JSONArray> getJSONArray(Request<?> r, Cache c) {

        JSONArray jArray = new JSONArray();
        // Check if we've previously done this exact call
        Cache.Item cacheList = c.get(Utils.requestToUrlAndQueryString(r));
        if (cacheList != null && cacheList.object instanceof LinkedList<?>) {

            LinkedList<?> cacheListLinkedList = (LinkedList<?>) cacheList.object;
            if (!cacheListLinkedList.isEmpty() && cacheListLinkedList.get(0) instanceof String) {
                LinkedList<String> erns = (LinkedList<String>) cacheListLinkedList;
                for (String string : erns) {
                    Cache.Item jObject = c.get(string);
                    if (jObject != null && jObject.object instanceof JSONObject) {
                        jArray.put((JSONObject) jObject.object);
                    }
                }

                if (jArray.length() == erns.size()) {
                    return Response.fromSuccess(jArray, null);
                }
            }

        }

        // Lets try to see if it's possible to create a response from
        // previously cached items
        Set<String> keys = r.getParameters().keySet();
        boolean hasFilter = keys.contains(Api.Param.CATALOG_IDS) ||
                keys.contains(Api.Param.DEALER_IDS) ||
                keys.contains(Api.Param.OFFER_IDS) ||
                keys.contains(Api.Param.STORE_IDS);

        if (!hasFilter) {
            // Nothing to work with
            return null;
        }

        String[] path = r.getUrl().split("/");

        // if last element is a type, then we'll expect a list
        String type = path[path.length - 1];

        Set<String> ids = getIdsFromFilter(type, r.getParameters());

        // No ids? no catchable items...
        if (ids.size() == 0) {
            return null;
        }

        // Get all possible items requested from cache
        for (String id : ids) {
            String ern = buildErn(type, id);
            Cache.Item cacheId = c.get(ern);
            if (cacheId != null) {
                jArray.put((JSONObject) cacheId.object);
            }
        }

        // If cache had ALL items, then return the list.
        if (jArray.length() == ids.size()) {
            return Response.fromSuccess(jArray, null);
        }

        return null;
    }

    public static Response<JSONObject> getJSONObject(Request<?> r, Cache cache) {

        String url = r.getUrl();
        String[] path = url.split("/");

        // Test all paths with for, to make better checks
        String id = path[path.length - 1];
        String type = path[path.length - 2];

        String ern = buildErn(type, id);
        Item ci = cache.get(ern);
        if (ci != null && ci.object instanceof JSONObject) {
            return Response.fromSuccess((JSONObject) ci.object, null);
        }

//		for (int i = path.length-1; i == 0 ; i-- ) {
//
//			// Test all paths with for, to make better checks
//			String id = path[i];
//			String type = path[i-1];
//			
//			String ern = buildErn(type, id);
//			Item ci = c.get(ern);
//			if (c != null && ci.object instanceof JSONObject) {
//				return Response.fromSuccess((JSONObject)ci.object, null);
//			}
//			
//		}

        return null;

    }

    public static void cacheJSONArray(Request<?> r, JSONArray a) {

        LinkedList<String> ernlist = new LinkedList<String>();
        try {

            for (int i = 0; i < a.length(); i++) {
                Object o = a.get(i);
                if (o instanceof JSONObject) {
                    String ern = cacheJSONObject(r, (JSONObject) o);
                    if (ern != null) {
                        ernlist.add(ern);
                    }
                }

            }

        } catch (JSONException e) {
            SgnLog.e(TAG, "", e);
        }

        if (ernlist.isEmpty()) {
            return;
        }

        r.getCache().put(Utils.requestToUrlAndQueryString(r), new Cache.Item(ernlist, r.getCacheTTL()));

    }

    public static String cacheJSONObject(Request<?> r, JSONObject o) {

        try {

            if (o.has(Api.JsonKey.ERN)) {
                String ern = o.getString(Api.JsonKey.ERN);
                Cache.Item i = new Item(o, r.getCacheTTL());
                r.getCache().put(ern, i);
                return ern;
            }
        } catch (JSONException e) {
            SgnLog.e(TAG, "", e);
        }
        return null;
    }

    private static Set<String> getIdsFromFilter(String filterName, Map<String, String> apiParams) {

        String tmp = apiParams.get(filterName);
        Set<String> list = new HashSet<String>();
        if (tmp != null) {
            Collections.addAll(list, TextUtils.split(tmp, ","));
        }
        return list;
    }

    private static String buildErn(String type, String id) {

        if (mErnTypes.contains(type)) {
            type = type.substring(0, type.length() - 1);
            return String.format(ERN_FORMAT, type, id);
        }
        return type;
    }

}
