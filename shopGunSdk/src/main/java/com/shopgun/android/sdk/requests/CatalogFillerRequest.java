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
        Catalog c = getData();
        ArrayList<Request> list = new ArrayList<Request>();
        if (mStore) {
            Request r = RequestCreator.getStoreRequestOrNull(this, c);
            if (r != null) {
                list.add(r);
            }
        }
        if (mDealer) {
            Request r = RequestCreator.getDealerRequestOrNull(this, c);
            if (r != null) {
                list.add(r);
            }
        }
        if (mPages) {
            Request r = RequestCreator.getPagesRequestOrNull(this, c);
            if (r != null) {
                list.add(r);
            }
        }
        if (mHotspots) {
            Request r = RequestCreator.getHotspotsRequestOrNull(this, c);
            if (r != null) {
                list.add(r);
            }
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
