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
import com.shopgun.android.sdk.model.Store;
import com.shopgun.android.sdk.requests.LoaderRequest;
import com.shopgun.android.sdk.requests.ModelRequest;
import com.shopgun.android.sdk.utils.Constants;

import org.json.JSONObject;

public class StoreRequest extends ModelRequest<Store> {

    public static final String TAG = Constants.getTag(StoreRequest.class);

    public StoreRequest(String storeId, LoaderRequest.Listener<Store> listener) {
        this(storeId, new StoreLoaderRequest(listener), listener);
    }

    public StoreRequest(Store store, LoaderRequest.Listener<Store> listener) {
        this(store.getId(), new StoreLoaderRequest(store, listener), listener);
    }

    public StoreRequest(String storeId, StoreLoaderRequest request, LoaderRequest.Listener<Store> listener) {
        super(Endpoints.storeId(storeId), request, listener);
    }

    @Override
    public Store parse(JSONObject response) {
        return Store.fromJSON(response);
    }

    @Override
    public boolean loadDealer() {
        return super.loadDealer();
    }

    @Override
    public void loadDealer(boolean dealer) {
        super.loadDealer(dealer);
    }

}
