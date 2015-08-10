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

package com.shopgun.android.sdk.request.impl;

import com.shopgun.android.sdk.model.Offer;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.Response.Listener;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.utils.Api;
import com.shopgun.android.sdk.utils.Api.Endpoint;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OfferListRequest extends ListRequest<List<Offer>> {

    private OfferListRequest(Listener<List<Offer>> l) {
        super(Endpoint.OFFER_LIST, l);
    }

    @Override
    public void deliverResponse(JSONArray response, ShopGunError error) {
        List<Offer> offers = null;
        if (response != null) {
            offers = Offer.fromJSON(response);
        }
        runAutoFill(offers, error);
    }

    public static class Builder extends ListRequest.Builder<List<Offer>> {

        public Builder(Listener<List<Offer>> l) {
            super(new OfferListRequest(l));
        }

        public void setParameters(Parameter params) {
            super.setParameters(params);
        }

        public void setAutoFill(OfferListAutoFill filler) {
            super.setAutoFiller(filler);
        }

        @Override
        public ListRequest<List<Offer>> build() {

            if (getParameters() == null) {
                setParameters(new Parameter());
            }

            if (getAutofill() == null) {
                setAutoFiller(new OfferListAutoFill());
            }

            return super.build();
        }

    }

    public static class Parameter extends ListRequest.ListParameterBuilder {

        public Parameter() {
            setDefaultOrder("-" + Api.Sort.POPULARITY);
        }

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

        public void orderByPopularity(boolean enable, boolean descending) {
            addOrder(Api.Sort.POPULARITY, descending);
        }

        public void removeOrderPopularity() {
            remove(Api.Sort.POPULARITY);
        }

        public void orderByPage(boolean enable, boolean descending) {
            addOrder(Api.Sort.PAGE, descending);
        }

        public void removeOrderPage() {
            remove(Api.Sort.PAGE);
        }

        public void orderByCreated(boolean enable, boolean descending) {
            addOrder(Api.Sort.CREATED, descending);
        }

        public void removeOrderCreated() {
            remove(Api.Sort.CREATED);
        }

        public void orderByPrice(boolean enable, boolean descending) {
            addOrder(Api.Sort.PRICE, descending);
        }

        public void removeOrderPrice() {
            remove(Api.Sort.PRICE);
        }

        public void orderBySavings(boolean enable, boolean descending) {
            addOrder(Api.Sort.SAVINGS, descending);
        }

        public void removeOrderSavings() {
            remove(Api.Sort.SAVINGS);
        }

        public void orderByQuantity(boolean enable, boolean descending) {
            addOrder(Api.Sort.QUANTITY, descending);
        }

        public void removeOrderQuantity() {
            remove(Api.Sort.QUANTITY);
        }

        public void orderByCount(boolean enable, boolean descending) {
            addOrder(Api.Sort.COUNT, descending);
        }

        public void removeOrderCount() {
            remove(Api.Sort.COUNT);
        }

        public void orderByExpirationDate(boolean enable, boolean descending) {
            addOrder(Api.Sort.EXPIRATION_DATE, descending);
        }

        public void removeOrderExpirationDate() {
            remove(Api.Sort.EXPIRATION_DATE);
        }

        public void orderByPublicationDate(boolean enable, boolean descending) {
            addOrder(Api.Sort.PUBLICATION_DATE, descending);
        }

        public void removeOrderPublicationDate() {
            remove(Api.Sort.PUBLICATION_DATE);
        }

        public void orderByValidDate(boolean enable, boolean descending) {
            addOrder(Api.Sort.VALID_DATE, descending);
        }

        public void removeOrderValidDate() {
            remove(Api.Sort.VALID_DATE);
        }

        public void orderByDealer(boolean enable, boolean descending) {
            addOrder(Api.Sort.DEALER, descending);
        }

        public void removeOrderDealer() {
            remove(Api.Sort.DEALER);
        }

        public void orderByDistance(boolean enable, boolean descending) {
            addOrder(Api.Sort.DISTANCE, descending);
        }

        public void removeOrderDistance() {
            remove(Api.Sort.DISTANCE);
        }

    }

    public static class OfferListAutoFill extends ListRequest.ListAutoFill<List<Offer>> {

        private boolean mCatalogs;
        private boolean mDealer;
        private boolean mStore;

        public OfferListAutoFill() {
            this(false, false, false);
        }

        public OfferListAutoFill(boolean catalogs, boolean dealer, boolean store) {
            mCatalogs = catalogs;
            mDealer = dealer;
            mStore = store;
        }

        @Override
        public List<Request<?>> createRequests(List<Offer> data) {

            List<Request<?>> reqs = new ArrayList<Request<?>>();

            if (!data.isEmpty()) {

                if (mStore) {
                    reqs.add(getStoreRequest(data));
                }

                if (mDealer) {
                    reqs.add(getDealerRequest(data));
                }

                if (mCatalogs) {
                    reqs.add(getCatalogRequest(data));
                }

            }

            return reqs;
        }

    }

}
