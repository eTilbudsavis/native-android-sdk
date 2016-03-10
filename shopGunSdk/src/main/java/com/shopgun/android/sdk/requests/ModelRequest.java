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
import com.shopgun.android.sdk.network.Cache;
import com.shopgun.android.sdk.network.Delivery;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.RequestQueue;
import com.shopgun.android.sdk.network.Response;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.network.impl.JsonObjectRequest;

import org.json.JSONObject;

import java.util.ArrayList;

public abstract class ModelRequest<T> extends JsonObjectRequest implements Delivery {

    public static final String TAG = Constants.getTag(ModelRequest.class);

    private final ModelLoaderRequest<T> mLoaderRequest;
    private final LoaderRequest.Listener<T> mLoaderListener;
    private final LoaderDelivery<T> mDelivery;

    public ModelRequest(String url, LoaderRequest.Listener<T> listener) {
        this(url, null, listener);
    }

    public ModelRequest(String url, ModelLoaderRequest<T> request, LoaderRequest.Listener<T> listener) {
        super(url, null);
        mLoaderRequest = request;
        mLoaderListener = listener;
        mDelivery = new LoaderDelivery<T>(mLoaderListener);
    }

    public ModelLoaderRequest<T> getLoaderRequest() {
        return mLoaderRequest;
    }

    @Override
    public synchronized Request setRequestQueue(RequestQueue requestQueue) {
        super.setRequestQueue(requestQueue);
        if (getTag() == null) {
            // Attach a tag if one haven't been provided
            // This will be used at a cancellation signal
            setTag(new Object());
        }
        super.setDelivery(this);
        return this;
    }

    @Override
    public Response<JSONObject> parseCache(Cache c) {
        if (mLoaderRequest != null && mLoaderRequest.getData() != null) {
            // ensure that the loader data is used, if it's present
            return Response.fromError(new InternalOkError());
        }
        return super.parseCache(c);
    }

    @Override
    public Request setDelivery(Delivery d) {
        throw new RuntimeException(new IllegalAccessException("Custom delivery for model requests is not allowed"));
    }

    public abstract T parse(JSONObject response);

    @Override
    public synchronized void cancel() {
        super.cancel();
        if (mLoaderRequest != null) {
            mLoaderRequest.cancel();
        }
    }

    @Override
    public synchronized void postResponse(Request<?> request, Response<?> response) {
        request.addEvent("post-response");

        if (isCanceled()) {

            // ignore callback
            request.addEvent("loaderRequest-have-been-canceled");

        } else if (response.isSuccess()) {

            // Standard request, we got the data, we'll parse and deliver
            request.addEvent("parsing-response-to-model-object");
            T data = parse((JSONObject) response.result);
            boolean intermediate = ModelRequestTools.runLoader(this, mLoaderRequest, data, getRequestQueue());
            mDelivery.deliver(this, response, data, new ArrayList<ShopGunError>(0), intermediate);

        } else if (response.error instanceof InternalOkError) {

            request.addEvent("running-loader-request-with-original-data");
            T data = mLoaderRequest.getData();
            boolean intermediate = ModelRequestTools.runLoader(this, mLoaderRequest, data, getRequestQueue());
            mDelivery.deliver(this, response, data, new ArrayList<ShopGunError>(0), intermediate);

        } else {

            // Something bad, ignore and deliver
            mDelivery.deliver(this, response, null, response.error, false);

        }

    }

    protected boolean loadDealer() {
        return mLoaderRequest.loadDealer();
    }

    protected void loadDealer(boolean dealer) {
        mLoaderRequest.loadDealer(dealer);
    }

    protected boolean loadHotspots() {
        return mLoaderRequest.loadHotspots();
    }

    protected void loadHotspots(boolean hotspots) {
        mLoaderRequest.loadHotspots(hotspots);
    }

    protected boolean loadPages() {
        return mLoaderRequest.loadPages();
    }

    protected void loadPages(boolean pages) {
        mLoaderRequest.loadPages(pages);
    }

    protected boolean loadStore() {
        return mLoaderRequest.loadStore();
    }

    protected void loadStore(boolean store) {
        mLoaderRequest.loadStore(store);
    }

    protected boolean loadCatalog() {
        return mLoaderRequest.loadCatalog();
    }

    protected void loadCatalog(boolean catalog) {
        mLoaderRequest.loadCatalog(catalog);
    }

}
