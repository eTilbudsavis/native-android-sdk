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
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.network.impl.JsonArrayRequest;
import com.shopgun.android.sdk.network.impl.JsonObjectRequest;

public class MockEmptyNetwork extends MockNetwork {

    private Context mContext;

    public MockEmptyNetwork(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public NetworkResponse performRequest(Request<?> request) throws ShopGunError {
        super.performRequest(request);

        PathHelper pathHelper = new PathHelper(request);
        if ("sessions".equals(pathHelper.getType())) {
            // if we do not create a session, nothing will work
            return new MockApiSessionResponse(mContext, request).getResponse();
        } else if (request instanceof JsonObjectRequest) {
            return new NetworkResponse(200, "{}".getBytes(), null);
        } else if (request instanceof JsonArrayRequest) {
            return new NetworkResponse(200, "[]".getBytes(), null);
        }

        return new MockUnsupportedNetworkResponse(request);
    }

}
