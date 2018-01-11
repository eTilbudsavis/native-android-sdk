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

import com.shopgun.android.sdk.api.Endpoints;
import com.shopgun.android.sdk.api.Parameters;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.Dealer;
import com.shopgun.android.sdk.model.HotspotMap;
import com.shopgun.android.sdk.model.Images;
import com.shopgun.android.sdk.model.Store;
import com.shopgun.android.sdk.model.interfaces.ICatalog;
import com.shopgun.android.sdk.model.interfaces.IDealer;
import com.shopgun.android.sdk.model.interfaces.IStore;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.Response;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.network.impl.JsonArrayRequest;
import com.shopgun.android.sdk.network.impl.JsonObjectRequest;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.utils.IUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RequestCreator {

    public static final String TAG = Constants.getTag(RequestCreator.class);

    protected RequestCreator() {
        // empty
    }

    public static Request getStoreRequestOrNull(final LoaderRequest request, final IStore<?> item) {
        boolean needStore = item.getStore() == null;
        return needStore ? getStoreRequest(request, item) : null;
    }

    public static Request getStoreRequest(final LoaderRequest request, final IStore<?> item) {
        return new JsonObjectRequest(Endpoints.storeId(item.getStoreId()), new Response.Listener<JSONObject>() {

            public void onComplete(JSONObject response, ShopGunError error) {
                if (response != null) {
                    item.setStore(Store.fromJSON(response));
                } else {
                    request.addError(error);
                }
            }
        });
    }

    public static Request getStoresRequestOrNull(final LoaderRequest request, final List<? extends IStore<?>> list) {
        Set<String> ids = IUtils.getStoreIds(list);
        return ids.isEmpty() ? null : getStoreRequest(ids, request, list);
    }

    public static Request getStoresRequest(final LoaderRequest request, final List<? extends IStore<?>> list) {
        Set<String> ids = IUtils.getStoreIds(list);
        return getStoreRequest(ids, request, list);
    }

    private static Request getStoreRequest(final Set<String> ids, final LoaderRequest request, final List<? extends IStore<?>> list) {
        JsonArrayRequest r = new JsonArrayRequest(Endpoints.STORE_LIST, new Response.Listener<JSONArray>() {

            @Override
            public void onComplete(JSONArray response, ShopGunError error) {

                if (response != null) {
                    List<Store> stores = Store.fromJSON(response);
                    for (IStore is : list) {
                        for (Store s : stores) {
                            if (is.getStoreId().equals(s.getId())) {
                                is.setStore(s);
                                break;
                            }
                        }
                    }
                } else {
                    request.addError(error);
                }
            }
        });
        r.setIds(Parameters.STORE_IDS, ids);
        return r;
    }

    public static Request getDealerRequestOrNull(final LoaderRequest request, final IDealer<?> item) {
        boolean needDealer = item.getDealer() == null;
        return needDealer ? getDealerRequest(request, item) : null;
    }

    public static Request getDealerRequest(final LoaderRequest request, final IDealer<?> item) {
        return new JsonObjectRequest(Endpoints.dealerId(item.getDealerId()), new Response.Listener<JSONObject>() {

            public void onComplete(JSONObject response, ShopGunError error) {
                if (response != null) {
                    item.setDealer(Dealer.fromJSON(response));
                } else {
                    request.addError(error);
                }
            }
        });
    }

    public static Request getDealersRequestOrNull(final LoaderRequest request, final List<? extends IDealer<?>> list) {
        Set<String> ids = IUtils.getDealerIds(list);
        return ids.isEmpty() ? null : getDealersRequest(ids, request, list);
    }

    public static Request getDealersRequest(final LoaderRequest request, final List<? extends IDealer<?>> list) {
        Set<String> ids = IUtils.getDealerIds(list);
        return getDealersRequest(ids, request, list);
    }

    private static Request getDealersRequest(final Set<String> ids, final LoaderRequest request, final List<? extends IDealer<?>> list) {
        JsonArrayRequest r = new JsonArrayRequest(Endpoints.DEALER_LIST, new Response.Listener<JSONArray>() {

            @Override
            public void onComplete(JSONArray response, ShopGunError error) {

                if (response != null) {
                    List<Dealer> dealers = Dealer.fromJSON(response);
                    for (IDealer idealer : list) {
                        for (Dealer d : dealers) {
                            if (idealer.getDealerId().equals(d.getId())) {
                                idealer.setDealer(d);
                                break;
                            }
                        }
                    }
                } else {
                    request.addError(error);
                }
            }
        });
        r.setIds(Parameters.DEALER_IDS, ids);
        return r;
    }

    public static List<Request> getPagesListRequestOrEmpty(final LoaderRequest request, final List<Catalog> catalogs) {
        List<Request> list = new ArrayList<Request>();
        for (Catalog c : catalogs) {
            Request r = getPagesRequestOrNull(request, c);
            if (r != null) {
                list.add(r);
            }
        }
        return list;
    }

    public static List<Request> getPagesListRequest(final LoaderRequest request, final List<Catalog> catalogs) {
        List<Request> list = new ArrayList<Request>();
        for (Catalog c : catalogs) {
            list.add(getPagesRequestOrNull(request, c));
        }
        return list;
    }

    public static Request getPagesRequestOrNull(final LoaderRequest request, final Catalog c) {
        boolean needPages = c.getPages() == null || c.getPages().isEmpty();
        return needPages ? getPagesRequest(request, c) : null;
    }

    public static Request getPagesRequest(final LoaderRequest request, final Catalog c) {
        return new JsonArrayRequest(Endpoints.catalogPages(c.getId()), new Response.Listener<JSONArray>() {

            public void onComplete(JSONArray response, ShopGunError error) {
                if (response != null) {
                    c.setPages(Images.fromJSON(response));
                } else {
                    request.addError(error);
                }
            }
        });
    }

    public static List<Request> getHotspotsListRequestOrEmpty(LoaderRequest request, List<Catalog> catalogs) {
        List<Request> list = new ArrayList<Request>();
        for (Catalog c : catalogs) {
            Request r = getHotspotsRequestOrNull(request, c);
            if (r != null) {
                list.add(r);
            }
        }
        return list;
    }

    public static List<Request> getHotspotsListRequest(LoaderRequest request, List<Catalog> catalogs) {
        List<Request> list = new ArrayList<Request>();
        for (Catalog c : catalogs) {
            list.add(getHotspotsRequest(request, c));
        }
        return list;
    }

    public static Request getHotspotsRequestOrNull(LoaderRequest request, Catalog c) {
        boolean needHotspots = c.getHotspots() == null;
        return needHotspots ? getHotspotsRequest(request, c) : null;
    }

    public static Request getHotspotsRequest(final LoaderRequest request, final Catalog c) {

        return new JsonArrayRequest(Endpoints.catalogHotspots(c.getId()), new Response.Listener<JSONArray>() {

            public void onComplete(JSONArray response, ShopGunError error) {
                if (response != null) {
                    c.setHotspots(HotspotMap.fromJSON(c.getDimension(), response));
                } else {
                    request.addError(error);
                }
            }
        });

    }

    public static Request createCatalogRequest(LoaderRequest request, ICatalog<?> item, boolean replace) {
        return (replace || item.getCatalog() == null) ? createCatalogRequest(request, item) : null;
    }

    public static Request createCatalogRequest(final LoaderRequest request, final ICatalog<?> item) {
        return new JsonObjectRequest(Endpoints.catalogId(item.getCatalogId()), new Response.Listener<JSONObject>() {

            public void onComplete(JSONObject response, ShopGunError error) {
                if (response != null) {
                    item.setCatalog(Catalog.fromJSON(response));
                } else {
                    request.addError(error);
                }
            }
        });
    }

    public static Request createCatalogRequest(LoaderRequest request, List<? extends ICatalog<?>> list) {
        return createCatalogRequest(request, list, false);
    }

    public static Request createCatalogRequest(LoaderRequest request, List<? extends ICatalog<?>> list, boolean replace) {
        Set<String> ids = IUtils.getCatalogIds(list, replace);
        return ids.isEmpty() ? null : createCatalogRequest(ids, request, list) ;
    }

    private static Request createCatalogRequest(final Set<String> ids, final LoaderRequest request, final List<? extends ICatalog<?>> list) {
        JsonArrayRequest r = new JsonArrayRequest(Endpoints.CATALOG_LIST, new Response.Listener<JSONArray>() {

            @Override
            public void onComplete(JSONArray response, ShopGunError error) {

                if (response != null) {
                    List<Catalog> stores = Catalog.fromJSON(response);
                    for (ICatalog is : list) {
                        for (Catalog s : stores) {
                            if (is.getCatalogId().equals(s.getId())) {
                                is.setCatalog(s);
                                break;
                            }
                        }
                    }
                } else {
                    request.addError(error);
                }
            }
        });
        r.setIds(Parameters.CATALOG_IDS, ids);
        return r;
    }

}
