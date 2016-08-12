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
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.requests.ModelListLoaderRequest;

import java.util.List;

public class CatalogListLoaderRequest extends ModelListLoaderRequest<List<Catalog>> {

    public static final String TAG = Constants.getTag(CatalogListLoaderRequest.class);

    public CatalogListLoaderRequest(Listener<List<Catalog>> l) {
        super(l);
    }

    public CatalogListLoaderRequest(List<Catalog> data, Listener<List<Catalog>> l) {
        super(data, l);
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
