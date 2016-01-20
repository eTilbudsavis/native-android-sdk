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

package com.shopgun.android.sdk.network.mock.response;

import android.content.Context;

import com.shopgun.android.sdk.api.Parameters;
import com.shopgun.android.sdk.network.NetworkResponse;
import com.shopgun.android.sdk.network.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MockDealerResponse extends MockNetworkResponse {

    protected MockDealerResponse(Context mContext, Request<?> request) {
        super(mContext, request);
    }

    @Override
    public NetworkResponse getResponse() {

        JSONArray array = getAssetJSONArray(FILE_DEALER_LIST);

        String actionOrId = mPath.getActionOrId();
        if (actionOrId == null) {
            // Just a list
            JSONArray idArray = filterByIds(array, mRequest, Parameters.DEALER_IDS);
            if (idArray != null) {
                return new NetworkResponse(200, idArray.toString().getBytes(), null);
            }
            return new NetworkResponse(200, array.toString().getBytes(), null);
        }

        if ("search".equals(actionOrId)) {
            // 'query' is being ignored
            return new NetworkResponse(200, array.toString().getBytes(), null);
        } else if ("suggested".equals(actionOrId)) {
            return getUnsupportedResponse();
        } else if ("suggest".equals(actionOrId)) {
            return getUnsupportedResponse();
        }

        // The action must be an id
        String id = actionOrId;
        String action = mPath.getItemAction();
        if (action == null) {
            // there is no further actions, lets get the specified catalog
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject item = array.getJSONObject(i);
                    if (id.equals(item.getString("id"))) {
                        return new NetworkResponse(200, item.toString().getBytes(), null);
                    }
                } catch (JSONException e) {
                    // Ignore
                }
            }
            return getUnsupportedResponse();
        }

        // dealers doesn't have any actions for an item

        return getUnsupportedResponse();


    }
}
