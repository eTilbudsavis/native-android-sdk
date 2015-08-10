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

import com.shopgun.android.sdk.model.Dealer;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.Response.Listener;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.utils.Api;
import com.shopgun.android.sdk.utils.Api.Endpoint;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DealerListRequest extends ListRequest<List<Dealer>> {

    private DealerListRequest(Listener<List<Dealer>> l) {
        super(Endpoint.DEALER_LIST, l);
    }

    @Override
    public void deliverResponse(JSONArray response, ShopGunError error) {
        List<Dealer> dealers = null;
        if (response != null) {
            dealers = Dealer.fromJSON(response);
        }
        runAutoFill(dealers, error);
    }

    public static class Builder extends ListRequest.Builder<List<Dealer>> {

        public Builder(Listener<List<Dealer>> l) {
            super(new DealerListRequest(l));
        }

        public void setAutoFill(DealerListAutoFill filler) {
            super.setAutoFiller(filler);
        }

        @Override
        public ListRequest<List<Dealer>> build() {

            if (getParameters() == null) {
                setParameters(new Parameter());
            }

            if (getAutofill() == null) {
                setAutoFiller(new DealerListAutoFill());
            }

            return super.build();
        }

    }

    public static class Parameter extends ListRequest.ListParameterBuilder {

        public void addDealerFilter(Set<String> dealerIds) {
            addFilter(Api.Param.DEALER_IDS, dealerIds);
        }

        public void addDealerFilter(String dealerId) {
            addFilter(Api.Param.DEALER_IDS, dealerId);
        }

    }

    public static class DealerListAutoFill extends ListRequest.ListAutoFill<List<Dealer>> {

        @Override
        public List<Request<?>> createRequests(List<Dealer> data) {
            return new ArrayList<Request<?>>();
        }

    }

}
