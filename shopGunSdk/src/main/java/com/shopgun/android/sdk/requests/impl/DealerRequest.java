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

package com.shopgun.android.sdk.requests.impl;

import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.api.Endpoints;
import com.shopgun.android.sdk.model.Dealer;
import com.shopgun.android.sdk.requests.LoaderRequest;
import com.shopgun.android.sdk.requests.ModelRequest;

import org.json.JSONObject;

public class DealerRequest extends ModelRequest<Dealer> {

    public static final String TAG = Constants.getTag(DealerRequest.class);

    public DealerRequest(Dealer dealer, LoaderRequest.Listener<Dealer> listener) {
        this(dealer.getId(), listener);
    }

    public DealerRequest(String dealer, LoaderRequest.Listener<Dealer> listener) {
        super(Endpoints.dealerId(dealer), null, listener);
    }

    @Override
    public Dealer parse(JSONObject response) {
        return Dealer.fromJSON(response);
    }

}
