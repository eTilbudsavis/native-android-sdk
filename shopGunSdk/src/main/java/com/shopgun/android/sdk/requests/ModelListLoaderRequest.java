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

import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.interfaces.ICatalog;
import com.shopgun.android.sdk.model.interfaces.IDealer;
import com.shopgun.android.sdk.model.interfaces.IStore;
import com.shopgun.android.sdk.network.Request;

import java.util.ArrayList;
import java.util.List;

public abstract class ModelListLoaderRequest<T> extends LoaderRequest<T> {

    public static final String TAG = Constants.getTag(ModelListLoaderRequest.class);

    private boolean mPages = false;
    private boolean mDealer = false;
    private boolean mStore = false;
    private boolean mHotspots = false;
    private boolean mCatalog = false;

    public ModelListLoaderRequest(Listener<T> l) {
        super(l);
    }

    public ModelListLoaderRequest(T data, Listener<T> l) {
        super(data, l);
    }

    protected boolean loadPages() {
        return mPages;
    }

    protected void loadPages(boolean pages) {
        mPages = pages;
    }

    protected boolean loadDealer() {
        return mDealer;
    }

    protected void loadDealer(boolean dealer) {
        mDealer = dealer;
    }

    protected boolean loadStore() {
        return mStore;
    }

    protected void loadStore(boolean store) {
        mStore = store;
    }

    protected boolean loadHotspots() {
        return mHotspots;
    }

    protected void loadHotspots(boolean hotspots) {
        mHotspots = hotspots;
    }

    protected boolean loadCatalog() {
        return mCatalog;
    }

    protected void loadCatalog(boolean catalog) {
        mCatalog = catalog;
    }

    @Override
    public List<Request> createRequests(T data) {
        return (data instanceof List) ? getReqs((List)data) : new ArrayList<Request>();
    }

    List<Request> getReqs(List<?> data) {
        ArrayList<Request> requests = new ArrayList<Request>();
        if (data.isEmpty()) {
            return requests;
        }
        Object item = data.get(0);
        if (mPages && item instanceof Catalog) {
            requests.addAll(RequestCreator.getPagesListRequestOrEmpty(this, (List<Catalog>)data));
        }
        if (mStore && item instanceof IStore) {
            requests.add(RequestCreator.getStoresRequestOrNull(this, (List<IStore<?>>) data));
        }
        if (mDealer && item instanceof IDealer) {
            requests.add(RequestCreator.getDealersRequestOrNull(this, (List<IDealer<?>>) data));
        }
        if (mHotspots && item instanceof Catalog) {
            requests.addAll(RequestCreator.getHotspotsListRequestOrEmpty(this, (List<Catalog>)data));
        }
        if (mCatalog && item instanceof ICatalog) {
            requests.add(RequestCreator.createCatalogRequest(this, (List<ICatalog<?>>)data, true));
        }
        return requests;
    }

}
