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

import com.shopgun.android.sdk.network.NetworkResponse;
import com.shopgun.android.sdk.network.Request;

public class MockApiSimpleResponse extends MockApiNetworkResponse {

    private String mName;

    protected MockApiSimpleResponse(Context mContext, Request<?> request, String name) {
        super(mContext, request);
        mName = name;
    }

    @Override
    public NetworkResponse getResponse() {
        return new NetworkResponse(200, getAssetAsByteArray(mName), null);
    }

}
