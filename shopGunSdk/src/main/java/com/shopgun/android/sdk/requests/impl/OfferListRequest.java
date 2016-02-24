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
import com.shopgun.android.sdk.model.Offer;
import com.shopgun.android.sdk.requests.LoaderRequest;
import com.shopgun.android.sdk.requests.ModelListLoaderRequest;
import com.shopgun.android.sdk.requests.ModelListRequest;

import org.json.JSONArray;

import java.util.List;

public class OfferListRequest extends ModelListRequest<List<Offer>> {

    public static final String TAG = Constants.getTag(OfferListRequest.class);

    public OfferListRequest(LoaderRequest.Listener<List<Offer>> listener) {
        this(new OfferListLoaderRequest(listener), listener);
    }

    public OfferListRequest(ModelListLoaderRequest<List<Offer>> loaderRequest, LoaderRequest.Listener<List<Offer>> listener) {
        this(Endpoints.OFFER_LIST, loaderRequest, listener);
    }

    public OfferListRequest(String url, LoaderRequest.Listener<List<Offer>> listener) {
        this(url, new OfferListLoaderRequest(listener), listener);
    }

    public OfferListRequest(String url, ModelListLoaderRequest<List<Offer>> loaderRequest, LoaderRequest.Listener<List<Offer>> listener) {
        super(url, loaderRequest, listener);
    }

    @Override
    public List<Offer> parse(JSONArray response) {
        return Offer.fromJSON(response);
    }

    @Override
    public boolean loadDealer() {
        return super.loadDealer();
    }

    @Override
    public OfferListRequest loadDealer(boolean dealer) {
        super.loadDealer(dealer);
        return this;
    }

    @Override
    public boolean loadStore() {
        return super.loadStore();
    }

    @Override
    public OfferListRequest loadStore(boolean store) {
        super.loadStore(store);
        return this;
    }

    @Override
    public boolean loadCatalog() {
        return super.loadCatalog();
    }

    @Override
    public OfferListRequest loadCatalog(boolean catalog) {
        super.loadCatalog(catalog);
        return this;
    }

}
