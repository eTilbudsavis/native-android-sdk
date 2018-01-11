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

import com.shopgun.android.sdk.api.Endpoints;
import com.shopgun.android.sdk.model.Dealer;
import com.shopgun.android.sdk.requests.LoaderRequest;
import com.shopgun.android.sdk.requests.ModelListRequest;
import com.shopgun.android.sdk.utils.Constants;

import org.json.JSONArray;

import java.util.List;

public class DealerListRequest extends ModelListRequest<List<Dealer>> {

    public static final String TAG = Constants.getTag(DealerListRequest.class);

    public DealerListRequest(LoaderRequest.Listener<List<Dealer>> listener) {
        this(Endpoints.DEALER_LIST, listener);
    }

    public DealerListRequest(String url, LoaderRequest.Listener<List<Dealer>> listener) {
        super(url, null, listener);
    }

    @Override
    public List<Dealer> parse(JSONArray response) {
        return Dealer.fromJSON(response);
    }

}
