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

public class CatalogFillerRequest extends FillerRequest<Catalog> {

    public static final String TAG = CatalogFillerRequest.class.getSimpleName();

    private boolean mPages = false;
    private boolean mDealer = false;
    private boolean mStore = false;
    private boolean mHotspots = false;

    public CatalogFillerRequest(Catalog c, Listener<Catalog> listener) {
        super(c, listener);
    }

    @Override
    public List<Request> createRequests() {
        ArrayList<Request> list = new ArrayList<Request>();
        if (mStore) {
            list.add(RequestCreator.getStoreRequestOrNull(this, getData()));
        }
        if (mDealer) {
            list.add(RequestCreator.getDealerRequestOrNull(this, getData()));
        }
        if (mPages) {
            list.add(RequestCreator.getPagesRequestOrNull(this, getData()));
        }
        if (mHotspots) {
            list.add(RequestCreator.getHotspotsRequestOrNull(this, getData()));
        }
        return list;
    }

    public boolean addPages() {
        return mPages;
    }

    public void addPages(boolean pages) {
        mPages = pages;
    }

    public boolean addDealer() {
        return mDealer;
    }

    public void addDealer(boolean dealer) {
        mDealer = dealer;
    }

    public boolean addStore() {
        return mStore;
    }

    public void addStore(boolean store) {
        mStore = store;
    }

    public boolean addHotspots() {
        return mHotspots;
    }

    public void addHotspots(boolean hotspots) {
        mHotspots = hotspots;
    }

}
