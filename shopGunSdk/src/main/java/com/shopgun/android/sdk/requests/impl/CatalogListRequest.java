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
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.Store;
import com.shopgun.android.sdk.requests.LoaderRequest;
import com.shopgun.android.sdk.requests.ModelListLoaderRequest;
import com.shopgun.android.sdk.requests.ModelListRequest;

import org.json.JSONArray;

import java.util.List;

public class CatalogListRequest extends ModelListRequest<List<Catalog>> {

    public static final String TAG = Constants.getTag(CatalogListRequest.class);

    public CatalogListRequest(LoaderRequest.Listener<List<Catalog>> listener) {
        this(new CatalogListLoaderRequest(null, listener), listener);
    }

    public CatalogListRequest(ModelListLoaderRequest<List<Catalog>> loaderRequest, LoaderRequest.Listener<List<Catalog>> listener) {
        this(Endpoints.CATALOG_LIST, loaderRequest, listener);
    }

    public CatalogListRequest(String url, LoaderRequest.Listener<List<Catalog>> listener) {
        this(url, new CatalogListLoaderRequest(null, listener), listener);
    }

    public CatalogListRequest(String url, ModelListLoaderRequest<List<Catalog>> loaderRequest, LoaderRequest.Listener<List<Catalog>> listener) {
        super(url, loaderRequest, listener);
    }

    @Override
    public List<Catalog> parse(JSONArray response) {
        return Catalog.fromJSON(response);
    }

    @Override
    public boolean loadDealer() {
        return super.loadDealer();
    }

    @Override
    public CatalogListRequest loadDealer(boolean dealer) {
        super.loadDealer(dealer);
        return this;
    }

    @Override
    public boolean loadHotspots() {
        return super.loadHotspots();
    }

    @Override
    public CatalogListRequest loadHotspots(boolean hotspots) {
        super.loadHotspots(hotspots);
        return this;
    }

    @Override
    public boolean loadPages() {
        return super.loadPages();
    }

    @Override
    public CatalogListRequest loadPages(boolean pages) {
        super.loadPages(pages);
        return this;
    }

    @Override
    public boolean loadStore() {
        return super.loadStore();
    }

    @Override
    public CatalogListRequest loadStore(boolean store) {
        super.loadStore(store);
        return this;
    }

}
