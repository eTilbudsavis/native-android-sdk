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
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.requests.LoaderRequest;
import com.shopgun.android.sdk.requests.ModelRequest;
import com.shopgun.android.sdk.utils.Constants;

import org.json.JSONObject;

public class CatalogRequest extends ModelRequest<Catalog> {

    public static final String TAG = Constants.getTag(CatalogRequest.class);

    public CatalogRequest(String catalogId, LoaderRequest.Listener<Catalog> listener) {
        this(catalogId, new CatalogLoaderRequest(listener), listener);
    }

    public CatalogRequest(Catalog catalog, LoaderRequest.Listener<Catalog> listener) {
        this(catalog.getId(), new CatalogLoaderRequest(catalog, listener), listener);
    }

    public CatalogRequest(String catalogId, CatalogLoaderRequest loaderRequest, LoaderRequest.Listener<Catalog> listener) {
        super(Endpoints.catalogId(catalogId), loaderRequest, listener);
    }

    @Override
    public Catalog parse(JSONObject response) {
        return Catalog.fromJSON(response);
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
    public boolean loadHotspots() {
        return super.loadHotspots();
    }

    @Override
    public void loadHotspots(boolean hotspots) {
        super.loadHotspots(hotspots);
    }

    @Override
    public boolean loadPages() {
        return super.loadPages();
    }

    @Override
    public void loadPages(boolean pages) {
        super.loadPages(pages);
    }

    @Override
    public boolean loadStore() {
        return super.loadStore();
    }

    @Override
    public void loadStore(boolean store) {
        super.loadStore(store);
    }

}
