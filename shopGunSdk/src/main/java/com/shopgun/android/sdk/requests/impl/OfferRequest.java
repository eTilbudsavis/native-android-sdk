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
import com.shopgun.android.sdk.requests.ModelRequest;

import org.json.JSONObject;

public class OfferRequest extends ModelRequest<Offer> {

    public static final String TAG = Constants.getTag(OfferRequest.class);

    public OfferRequest(String offerId, LoaderRequest.Listener<Offer> listener) {
        this(offerId, new OfferLoaderRequest(listener), listener);
    }

    public OfferRequest(Offer offer, LoaderRequest.Listener<Offer> listener) {
        this(offer.getId(), new OfferLoaderRequest(offer, listener), listener);
    }

    public OfferRequest(String offerId, OfferLoaderRequest request, LoaderRequest.Listener<Offer> listener) {
        super(Endpoints.offerId(offerId), request, listener);
    }

    @Override
    public Offer parse(JSONObject response) {
        return Offer.fromJSON(response);
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
    protected boolean loadCatalog() {
        return super.loadCatalog();
    }

    @Override
    protected void loadCatalog(boolean catalog) {
        super.loadCatalog(catalog);
    }

}
