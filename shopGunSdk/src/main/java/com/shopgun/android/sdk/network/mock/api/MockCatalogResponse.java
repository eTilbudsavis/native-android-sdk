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

package com.shopgun.android.sdk.network.mock.api;

import android.content.Context;

import com.shopgun.android.sdk.api.Parameters;
import com.shopgun.android.sdk.network.NetworkResponse;
import com.shopgun.android.sdk.network.Request;

import org.json.JSONArray;

public class MockCatalogResponse extends MockNetworkResponse {

    protected MockCatalogResponse(Context mContext, Request<?> request) {
        super(mContext, request);
    }

    @Override
    public NetworkResponse getResponse() {

        JSONArray array = getAssetJSONArray(FILE_CATALOG_LIST);

        String actionOrId = mPath.getActionOrId();
        if (actionOrId == null) {
            // Just a catalog list
            JSONArray idArray = filterByIds(array, mRequest, Parameters.CATALOG_IDS);
            if (idArray != null) {
                array = trimToOffsetAndLimit(idArray, mRequest);
                return new NetworkResponse(200, array.toString().getBytes(), null);
            }
            array = trimToOffsetAndLimit(array, mRequest);
            return new NetworkResponse(200, array.toString().getBytes(), null);
        }


        if ("search".equals(actionOrId)) {
            // 'query' is being ignored
            array = trimToOffsetAndLimit(array, mRequest);
            return new NetworkResponse(200, array.toString().getBytes(), null);
        } else if ("suggest".equals(actionOrId)) {
            return new NetworkResponse(200, array.toString().getBytes(), null);
        } else if ("count".equals(actionOrId)) {
            return getUnsupportedResponse();
        } else if ("typeahead".equals(actionOrId)) {
            return new NetworkResponse(200, getAssetAsString(FILE_TYPEAHEAD).getBytes(), null);
        }

        // The action must be an id
        String id = actionOrId;
        String action = mPath.getItemAction();
        if (action == null) {
            return getItem(array, id);
        }

        if ("pages".equals(action)) {

            String name = String.format("catalog-%s-pages.json", id);
            JSONArray pages = getAssetJSONArray(name);
            return new NetworkResponse(200, pages.toString().getBytes(), null);

        } else if ("hotspots".equals(action)) {

            String name = String.format("catalog-%s-hotspots.json", id);
            JSONArray hotspots = getAssetJSONArray(name);
            return new NetworkResponse(200, hotspots.toString().getBytes(), null);

        } else if ("stores".equals(action)) {
            return getUnsupportedResponse();
        } else if ("collect".equals(action)) {
            return new NetworkResponse(201, "{}".getBytes(), null);
        } else if ("download".equals(action)) {
            return getUnsupportedResponse();
        }

        return getUnsupportedResponse();

    }

}
