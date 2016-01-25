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
import com.shopgun.android.sdk.network.mock.response.MockNetworkResponse;

import java.net.MalformedURLException;
import java.net.URL;

public class MockUnsupportedResponse extends MockNetworkResponse {

    public static final String TAG = MockUnsupportedResponse.class.getSimpleName();

    public MockUnsupportedResponse(Context mContext, Request<?> request) {
        super(mContext, request);
    }

    @Override
    public NetworkResponse getResponse() {

        ShopGunError error;
        try {
            URL url = new URL(mRequest.getUrl());
            error = new ShopGunError(Integer.MAX_VALUE, "Path not supported", url.getPath());
        } catch (MalformedURLException e) {
            SgnLog.e(TAG, e.getMessage(), e);
            error = new ShopGunError(Integer.MAX_VALUE, "Malformed URL", e.getMessage() + ", " + mRequest.getUrl());
        }
        return new NetworkResponse(404, error.toJSON().toString().getBytes(), null);

    }

    @Override
    protected NetworkResponse getUnsupportedResponse() {
        return getResponse();
    }
}
