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

package com.shopgun.android.sdk.requests;

import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.network.Request;

import java.util.ArrayList;
import java.util.List;

public class CatalogListFillerRequest extends FillerRequest<List<Catalog>> {


    private boolean mPages = false;
    private boolean mDealer = false;
    private boolean mStore = false;
    private boolean mHotspots = false;

    public CatalogListFillerRequest(List<Catalog> data, Listener<List<Catalog>> l) {
        super(data, l);
    }

    @Override
    public List<Request> createRequests() {
        List<Catalog> data = getData();
        ArrayList<Request> list = new ArrayList<Request>();
        if (mStore) {
            list.add(RequestCreator.getStoresRequestOrNull(this, data));
        }
        if (mDealer) {
            list.add(RequestCreator.getDealersRequestOrNull(this, data));
        }
        if (mPages) {
            list.addAll(RequestCreator.getPagesListRequestOrEmpty(this, data));
        }
        if (mHotspots) {
            list.addAll(RequestCreator.getHotspotsListRequestOrEmpty(this, data));
        }
        return list;
    }

    public void appendPages(boolean pages) {
        mPages = pages;
    }

    public void appendDealer(boolean dealer) {
        mDealer = dealer;
    }

    public void appendStore(boolean store) {
        mStore = store;
    }

    public void appendHotspots(boolean hotspots) {
        mHotspots = hotspots;
    }

}
