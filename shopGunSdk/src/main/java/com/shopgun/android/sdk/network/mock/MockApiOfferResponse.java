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

import com.shopgun.android.sdk.api.Parameters;
import com.shopgun.android.sdk.network.NetworkResponse;
import com.shopgun.android.sdk.network.Request;

import org.json.JSONArray;

import java.util.HashSet;

public class MockApiOfferResponse extends MockApiNetworkResponse {

    static {

        HashSet<String> mOfferActions = new HashSet<String>();
        mOfferActions.add("search");
        mOfferActions.add("count");
        mOfferActions.add("suggest");
        mOfferActions.add("typeahead");
        mActions.put("offers", mOfferActions);

        HashSet<String> mOfferModelActions = new HashSet<String>();
        mOfferModelActions.add("search");
        mOfferModelActions.add("count");
        mOfferModelActions.add("suggest");
        mOfferModelActions.add("typeahead");
        mModelActions.put("offers", mOfferModelActions);
    }

    protected MockApiOfferResponse(Context mContext, Request<?> request) {
        super(mContext, request);
    }

    @Override
    public NetworkResponse getResponse() {

        JSONArray array = getAssetJSONArray(FILE_OFFER_LIST);

        String actionOrId = mPath.getActionOrId();
        if (actionOrId == null) {
            // Just a list
            JSONArray idArray = filterByIds(array, mRequest, Parameters.OFFER_IDS);
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
            array = trimToOffsetAndLimit(array, mRequest);
            return new NetworkResponse(200, array.toString().getBytes(), null);
        } else if ("count".equals(actionOrId)) {
            return getUnsupportedResponse();
        } else if ("typeahead".equals(actionOrId)) {
            return new NetworkResponse(200, getAssetAsString(FILE_TYPEAHEAD).getBytes(), null);
        }

        // The action must be an id
        String action = mPath.getItemAction();
        if (action == null) {
            return getItem(array, actionOrId);
        }

        if ("collect".equals(action)) {
            return new NetworkResponse(201, "{}".getBytes(), null);
        } else if ("light".equals(action)) {
            return getUnsupportedResponse();
        } else if ("webshop".equals(action)) {
            return getUnsupportedResponse();
        } else if ("stores".equals(action)) {
            return getUnsupportedResponse();
        }

        return getUnsupportedResponse();

    }

}
