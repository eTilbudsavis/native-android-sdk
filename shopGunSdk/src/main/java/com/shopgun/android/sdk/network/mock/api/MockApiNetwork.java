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

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.network.NetworkResponse;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.network.mock.MockUnsupportedNetworkResponse;
import com.shopgun.android.sdk.network.mock.MockNetwork;
import com.shopgun.android.sdk.network.mock.PathHelper;

import java.net.MalformedURLException;
import java.net.URL;

public class MockApiNetwork extends MockNetwork {

    public static final String TAG = Constants.getTag(MockApiNetwork.class);

    Context mContext;

    public MockApiNetwork(Context ctx) {
        this.mContext = ctx;
    }

    @Override
    public NetworkResponse performRequest(Request<?> request) throws ShopGunError {
        super.performRequest(request);
        try {

            URL url = new URL(request.getUrl());

            if (!url.getHost().contains("etilbudsavis")) {
                throw new ShopGunError(Integer.MAX_VALUE, "Host not supported", url.getHost());
            }

            PathHelper pathHelper = new PathHelper(request);
            String apiVersion = pathHelper.getApiVersion();
            if (!"v2".equals(apiVersion)) {
                throw new ShopGunError(Integer.MAX_VALUE, "API version not supported", "Api version given: " + apiVersion);
            }

            return MockApiNetworkResponse.create(mContext, request, pathHelper.getType()).getResponse();

        } catch (MalformedURLException e) {
            return new MockUnsupportedNetworkResponse(request);
        }

    }

}
