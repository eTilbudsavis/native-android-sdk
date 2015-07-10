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

import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.Response.Listener;
import com.shopgun.android.sdk.request.RequestAutoFill;
import com.shopgun.android.sdk.request.RequestParameter;
import com.shopgun.android.sdk.utils.Api;
import com.shopgun.android.sdk.utils.Api.Endpoint;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CatalogListRequest extends ListRequest<List<Catalog>> {

    private CatalogListRequest(Listener<List<Catalog>> l) {
        super(Endpoint.CATALOG_LIST, l);
    }

    @Override
    public void deliverResponse(JSONArray response, ShopGunError error) {
        List<Catalog> mCatalogs = null;
        if (response != null) {
            mCatalogs = Catalog.fromJSON(response);
        }
        runAutoFill(mCatalogs, error);
    }

    public static class Builder extends ListRequest.Builder<List<Catalog>> {

        public Builder(Listener<List<Catalog>> l) {
            super(new CatalogListRequest(l));
        }

        public Builder(CatalogListParameterBuilder parameter, Listener<List<Catalog>> l) {
            super(new CatalogListRequest(l));
            setParameters(parameter);
        }

        public void setParameters(RequestParameter params) {
            super.setParameters(params);
        }

        public void setAutoFill(CatalogListAutoFill filler) {
            super.setAutoFiller(filler);
        }

        @Override
        public ListRequest<List<Catalog>> build() {

            if (getParameters() == null) {
                setParameters(new CatalogListParameterBuilder());
            }

            if (getAutofill() == null) {
                setAutoFiller(new CatalogListAutoFill());
            }

            return super.build();
        }

    }

    public static class CatalogListParameterBuilder extends ListParameterBuilder {

        public CatalogListParameterBuilder() {
            setDefaultOrder("-" + Api.Sort.POPULARITY);
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

        public void addCatalogFilter(String catalogId) {
            addFilter(Api.Param.CATALOG_IDS, catalogId);
        }

        public void addDealerFilter(String dealerId) {
            addFilter(Api.Param.DEALER_IDS, dealerId);
        }

        public void addStoreFilter(String storeId) {
            addFilter(Api.Param.STORE_IDS, storeId);
        }

        public void orderByPopularity(boolean descending) {
            addOrder(Api.Sort.POPULARITY, descending);
        }

        public void removeOrderPopularity() {
            removeOrder(Api.Sort.POPULARITY);
        }

        public void orderByDealer(boolean enable, boolean descending) {
            addOrder(Api.Sort.DEALER, descending);
        }

        public void removeOrderDealer() {
            removeOrder(Api.Sort.DEALER);
        }

        public void orderByCreated(boolean enable, boolean descending) {
            addOrder(Api.Sort.CREATED, descending);
        }

        public void removeOrderCreated() {
            removeOrder(Api.Sort.CREATED);
        }

        public void orderByExpirationDate(boolean enable, boolean descending) {
            addOrder(Api.Sort.EXPIRATION_DATE, descending);
        }

        public void removeOrderExpirationDate() {
            removeOrder(Api.Sort.EXPIRATION_DATE);
        }

        public void orderByPublicationDate(boolean enable, boolean descending) {
            addOrder(Api.Sort.PUBLICATION_DATE, descending);
        }

        public void removeOrderPublicationDate() {
            removeOrder(Api.Sort.PUBLICATION_DATE);
        }

        public void orderByDistance(boolean enable, boolean descending) {
            addOrder(Api.Sort.DISTANCE, descending);
        }

        public void removeOrderDistance() {
            removeOrder(Api.Sort.DISTANCE);
        }

    }

    public static class CatalogListAutoFill extends RequestAutoFill<List<Catalog>> {

        private boolean mPages;
        private boolean mDealer;
        private boolean mStore;
        private boolean mHotspots;

        public CatalogListAutoFill() {
            this(false, false, false, false);
        }

        public CatalogListAutoFill(boolean pages, boolean dealer, boolean store, boolean hotspots) {
            mPages = pages;
            mDealer = dealer;
            mStore = store;
            mHotspots = hotspots;
        }

        @Override
        public List<Request<?>> createRequests(List<Catalog> data) {

            List<Request<?>> reqs = new ArrayList<Request<?>>();

            if (!data.isEmpty()) {

                if (mStore) {
                    reqs.add(getStoreRequest(data));
                }

                if (mDealer) {
                    reqs.add(getDealerRequest(data));
                }

                if (mPages) {

                    for (Catalog c : data) {
                        reqs.add(getPagesRequest(c));
                    }

                }

                if (mHotspots) {

                    for (Catalog c : data) {
                        reqs.add(getHotspotsRequest(c));
                    }

                }

            }

            return reqs;
        }

    }

}
