package com.shopgun.android.sdk.filler;

import com.shopgun.android.sdk.api.Endpoints;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.Dealer;
import com.shopgun.android.sdk.model.HotspotMap;
import com.shopgun.android.sdk.model.Images;
import com.shopgun.android.sdk.model.Store;
import com.shopgun.android.sdk.model.interfaces.IDealer;
import com.shopgun.android.sdk.model.interfaces.IStore;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.Response;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.network.impl.JsonArrayRequest;
import com.shopgun.android.sdk.network.impl.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

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
        if (mStore && c.getStore() == null) {
            list.add(getStoreRequest(c));
        }
        if (mDealer && c.getDealer() == null) {
            list.add(getDealerRequest(c));
        }
        if (mPages && (c.getPages() == null || c.getPages().isEmpty())) {
            list.add(getPagesRequest(c));
        }
        if (mHotspots && c.getHotspots() == null) {
            list.add(getHotspotsRequest(c));
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

    protected JsonObjectRequest getStoreRequest(final IStore<?> item) {
        return new JsonObjectRequest(Endpoints.storeId(item.getStoreId()), new Response.Listener<JSONObject>() {

            public void onComplete(JSONObject response, ShopGunError error) {
                if (response != null) {
                    item.setStore(Store.fromJSON(response));
                } else {
                    addError(error);
                }
            }
        });
    }

    protected JsonObjectRequest getDealerRequest(final IDealer<?> item) {
        return new JsonObjectRequest(Endpoints.dealerId(item.getDealerId()), new Response.Listener<JSONObject>() {

            public void onComplete(JSONObject response, ShopGunError error) {
                if (response != null) {
                    item.setDealer(Dealer.fromJSON(response));
                } else {
                    addError(error);
                }
            }
        });
    }

    protected JsonArrayRequest getPagesRequest(final Catalog c) {

        return new JsonArrayRequest(Endpoints.catalogPages(c.getId()), new Response.Listener<JSONArray>() {

            public void onComplete(JSONArray response, ShopGunError error) {
                if (response != null) {
                    c.setPages(Images.fromJSON(response));
                } else {
                    addError(error);
                }
            }
        });
    }

    protected JsonArrayRequest getHotspotsRequest(final Catalog c) {

        return new JsonArrayRequest(Endpoints.catalogHotspots(c.getId()), new Response.Listener<JSONArray>() {

            public void onComplete(JSONArray response, ShopGunError error) {
                if (response != null) {
                    c.setHotspots(HotspotMap.fromJSON(c.getDimension(), response));
                } else {
                    addError(error);
                }
            }
        });

    }

}
