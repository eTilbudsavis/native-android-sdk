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
import com.eTilbudsavis.etasdk.network.Request;
import com.eTilbudsavis.etasdk.network.Response.Listener;
import com.eTilbudsavis.etasdk.request.RequestAutoFill;
import com.eTilbudsavis.etasdk.utils.Api;

import java.util.ArrayList;
import java.util.List;

public class StoreObjectRequest extends ObjectRequest<Store> {

    private StoreObjectRequest(String url, Listener<Store> l) {
        super(url, l);
    }

    public static abstract class Builder extends ObjectRequest.Builder<Store> {

        public Builder(String storeId, Listener<Store> l) {
            super(new StoreObjectRequest(Api.Endpoint.storeId(storeId), l));
        }

        public ObjectRequest<Store> build() {
            ObjectRequest<Store> r = super.build();
            if (getAutofill() == null) {
                setAutoFiller(new StoreAutoFill());
            }
            return r;
        }

        public void setAutoFill(StoreAutoFill filler) {
            super.setAutoFiller(filler);
        }

    }

    public static class StoreAutoFill extends RequestAutoFill<Store> {

        private boolean mDealer;

        public StoreAutoFill() {
            this(false);
        }

        public StoreAutoFill(boolean dealer) {
            mDealer = dealer;
        }

        @Override
        public List<Request<?>> createRequests(Store data) {

            List<Request<?>> reqs = new ArrayList<Request<?>>();

            if (data != null) {

                if (mDealer) {
                    reqs.add(getDealerRequest(data));
                }

            }

            return reqs;
        }

    }

}
