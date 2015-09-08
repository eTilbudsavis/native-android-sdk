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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RequestCreator {

    public static Request getStoreRequestOrNull(final FillerRequest request, final IStore<?> item) {
        boolean needStore = item.getStore() == null;
        return needStore ? getStoreRequest(request, item) : null;
    }

    public static Request getStoreRequest(final FillerRequest request, final IStore<?> item) {
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

    public static Request getStoresRequestOrNull(final FillerRequest request, final List<? extends IStore<?>> list) {
        Set<String> ids = getStoreIds(list);
        return ids.isEmpty() ? null : getStoreRequest(ids, request, list);
    }

    public static Request getStoresRequest(final FillerRequest request, final List<? extends IStore<?>> list) {
        Set<String> ids = getStoreIds(list);
        return getStoreRequest(ids, request, list);
    }

    private static Set<String> getStoreIds(List<? extends IStore<?>> list) {
        Set<String> ids = new HashSet<String>(list.size());
        for (IStore is : list) {
            if (is.getStore() == null) {
                ids.add(is.getStoreId());
            }
        }
        return ids;
    }

    private static Request getStoreRequest(final Set<String> ids, final FillerRequest request, final List<? extends IStore<?>> list) {
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

    public static Request getDealerRequestOrNull(final FillerRequest request, final IDealer<?> item) {
        boolean needDealer = item.getDealer() == null;
        return needDealer ? getDealerRequest(request, item) : null;
    }

    public static Request getDealerRequest(final FillerRequest request, final IDealer<?> item) {
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

    public static Request getDealersRequestOrNull(final FillerRequest request, final List<? extends IDealer<?>> list) {
        Set<String> ids = getDealerIds(list);
        return ids.isEmpty() ? null : getDealersRequest(ids, request, list);
    }

    public static Request getDealersRequest(final FillerRequest request, final List<? extends IDealer<?>> list) {
        Set<String> ids = getDealerIds(list);
        return getDealersRequest(ids, request, list);
    }

    private static Set<String> getDealerIds(List<? extends IDealer<?>> list) {
        Set<String> ids = new HashSet<String>(list.size());
        for (IDealer is : list) {
            if (is.getDealer() == null) {
                ids.add(is.getDealerId());
            }
        }
        return ids;
    }

    private static Request getDealersRequest(final Set<String> ids, final FillerRequest request, final List<? extends IDealer<?>> list) {
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

    public static List<Request> getPagesListRequestOrEmpty(final FillerRequest request, final List<Catalog> catalogs) {
        List<Request> list = new ArrayList<Request>();
        for (Catalog c : catalogs) {
            Request r = getPagesRequestOrNull(request, c);
            if (r != null) {
                list.add(r);
            }
        }
        return list;
    }

    public static List<Request> getPagesListRequest(final FillerRequest request, final List<Catalog> catalogs) {
        List<Request> list = new ArrayList<Request>();
        for (Catalog c : catalogs) {
            list.add(getPagesRequestOrNull(request, c));
        }
        return list;
    }

    public static Request getPagesRequestOrNull(final FillerRequest request, final Catalog c) {
        boolean needPages = c.getPages() == null || c.getPages().isEmpty();
        return needPages ? getPagesRequest(request, c) : null;
    }

    public static Request getPagesRequest(final FillerRequest request, final Catalog c) {
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

    public static List<Request> getHotspotsListRequestOrEmpty(final FillerRequest request, final List<Catalog> catalogs) {
        List<Request> list = new ArrayList<Request>();
        for (Catalog c : catalogs) {
            Request r = getHotspotsRequestOrNull(request, c);
            if (r != null) {
                list.add(r);
            }
        }
        return list;
    }

    public static List<Request> getHotspotsListRequest(final FillerRequest request, final List<Catalog> catalogs) {
        List<Request> list = new ArrayList<Request>();
        for (Catalog c : catalogs) {
            list.add(getHotspotsRequest(request, c));
        }
        return list;
    }

    public static Request getHotspotsRequestOrNull(final FillerRequest request, final Catalog c) {
        boolean needHotspots = c.getHotspots() == null;
        return needHotspots ? getHotspotsRequest(request, c) : null;
    }

    public static Request getHotspotsRequest(final FillerRequest request, final Catalog c) {

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

}
