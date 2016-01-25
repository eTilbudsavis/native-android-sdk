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

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.api.Endpoints;
import com.shopgun.android.sdk.model.Store;
import com.shopgun.android.sdk.requests.LoaderRequest;
import com.shopgun.android.sdk.requests.ModelListLoaderRequest;
import com.shopgun.android.sdk.requests.ModelListRequest;

import org.json.JSONArray;

import java.util.List;

public class StoreListRequest extends ModelListRequest<List<Store>> {

    public static final String TAG = Constants.getTag(StoreListRequest.class);

    public StoreListRequest(LoaderRequest.Listener<List<Store>> listener) {
        this(new StoreListLoaderRequest(null, listener), listener);
    }

    public StoreListRequest(ModelListLoaderRequest<List<Store>> loaderRequest, LoaderRequest.Listener<List<Store>> listener) {
        this(Endpoints.STORE_LIST, loaderRequest, listener);
    }

    public StoreListRequest(String url, LoaderRequest.Listener<List<Store>> listener) {
        this(url, new StoreListLoaderRequest(null, listener), listener);
    }

    public StoreListRequest(String url, ModelListLoaderRequest<List<Store>> loaderRequest, LoaderRequest.Listener<List<Store>> listener) {
        super(url, loaderRequest, listener);
    }

    @Override
    public List<Store> parse(JSONArray response) {
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

    @Override
    public boolean loadStore() {
        return super.loadStore();
    }

    @Override
    public void loadStore(boolean store) {
        super.loadStore(store);
    }

    @Override
    public boolean loadCatalog() {
        return super.loadCatalog();
    }

    @Override
    public void loadCatalog(boolean catalog) {
        super.loadCatalog(catalog);
    }

}
