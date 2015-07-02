/*******************************************************************************
 * Copyright 2015 eTilbudsavis
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

package com.eTilbudsavis.etasdk.request.impl;

import com.eTilbudsavis.etasdk.model.Store;
import com.eTilbudsavis.etasdk.network.EtaError;
import com.eTilbudsavis.etasdk.network.Request;
import com.eTilbudsavis.etasdk.network.Response.Listener;
import com.eTilbudsavis.etasdk.utils.Api;
import com.eTilbudsavis.etasdk.utils.Api.Endpoint;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StoreListRequest extends ListRequest<List<Store>> {

    private StoreListRequest(Listener<List<Store>> l) {
        super(Endpoint.STORE_LIST, l);
    }

    @Override
    public void deliverResponse(JSONArray response, EtaError error) {
        List<Store> offers = null;
        if (response != null) {
            offers = Store.fromJSON(response);
        }
        runAutoFill(offers, error);
    }

    public static class Builder extends ListRequest.Builder<List<Store>> {

        public Builder(Listener<List<Store>> l) {
            super(new StoreListRequest(l));
        }

        public void setParameters(Parameter params) {
            super.setParameters(params);
        }

        public void setAutoFill(StoreListAutoFill filler) {
            super.setAutoFiller(filler);
        }

        @Override
        public ListRequest<List<Store>> build() {

            if (getParameters() == null) {
                setParameters(new Parameter());
            }

            if (getAutofill() == null) {
                setAutoFiller(new StoreListAutoFill());
            }

            return super.build();
        }

    }

    public static class Parameter extends ListRequest.ListParameterBuilder {

        public void addOfferFilter(Set<String> offerIds) {
            addFilter(Api.Param.OFFER_IDS, offerIds);
        }

        public void addCatalogFilter(Set<String> catalogIds) {
            addFilter(Api.Param.CATALOG_IDS, catalogIds);
        }

        public void addDealerFilter(Set<String> dealerIds) {
            addFilter(Api.Param.DEALER_IDS, dealerIds);
        }

        public void addStoreFilter(Set<String> storeIds) {
            addFilter(Api.Param.STORE_IDS, storeIds);
        }

        public void addOfferFilter(String offerId) {
            addFilter(Api.Param.OFFER_IDS, offerId);
        }

        public void addCatalogFilter(String catalogId) {
            addFilter(Api.Param.CATALOG_IDS, catalogId);
        }

        public void addDealerFilter(String dealerId) {
            addFilter(Api.Param.DEALER_IDS, dealerId);
        }

        public void addStoreFilter(String storeId) {
            addFilter(Api.Param.STORE_IDS, storeId);
        }

    }

    public static class StoreListAutoFill extends ListRequest.ListAutoFill<List<Store>> {

        private boolean mDealer;

        public StoreListAutoFill() {
            this(false);
        }

        public StoreListAutoFill(boolean dealer) {
            mDealer = dealer;
        }

        @Override
        public List<Request<?>> createRequests(List<Store> data) {

            List<Request<?>> reqs = new ArrayList<Request<?>>();

            if (!data.isEmpty()) {

                if (mDealer) {
                    reqs.add(getDealerRequest(data));
                }

            }

            return reqs;
        }

    }

}
