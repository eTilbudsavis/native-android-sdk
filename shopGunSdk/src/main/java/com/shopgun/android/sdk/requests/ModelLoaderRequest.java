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

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.interfaces.ICatalog;
import com.shopgun.android.sdk.model.interfaces.IDealer;
import com.shopgun.android.sdk.model.interfaces.IStore;
import com.shopgun.android.sdk.network.Request;

import java.util.ArrayList;
import java.util.List;

public class ModelLoaderRequest<T> extends LoaderRequest<T> {

    public static final String TAG = Constants.getTag(ModelLoaderRequest.class);

    private boolean mPages = false;
    private boolean mDealer = false;
    private boolean mStore = false;
    private boolean mHotspots = false;
    private boolean mCatalog = false;

    public ModelLoaderRequest(Listener<T> l) {
        super(l);
    }

    public ModelLoaderRequest(T data, Listener<T> l) {
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
        ArrayList<Request> requests = new ArrayList<Request>();
        if (mPages && data instanceof Catalog) {
            requests.add(RequestCreator.getPagesRequestOrNull(this, (Catalog)data));
        }
        if (mStore && data instanceof IStore) {
            requests.add(RequestCreator.getStoreRequestOrNull(this, (IStore<?>) data));
        }
        if (mDealer && data instanceof IDealer) {
            requests.add(RequestCreator.getDealerRequestOrNull(this, (IDealer<?>) data));
        }
        if (mHotspots && data instanceof Catalog) {
            requests.add(RequestCreator.getHotspotsRequestOrNull(this, (Catalog)data));
        }
        if (mCatalog && data instanceof ICatalog) {
            requests.add(RequestCreator.createCatalogRequest(this, (ICatalog<?>)data, true));
        }
        return requests;
    }

}
