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
import com.shopgun.android.sdk.network.NetworkResponse;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.RequestQueue;
import com.shopgun.android.sdk.network.Response;
import com.shopgun.android.sdk.network.ShopGunError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public abstract class LoaderRequest<T> extends Request<T> implements Delivery {

    public static final String TAG = Constants.getTag(LoaderRequest.class);

    private T mData;
    private final Listener<T> mListener;
    private final List<Request> mRequests = Collections.synchronizedList(new ArrayList<Request>());
    private final List<ShopGunError> mErrors = Collections.synchronizedList(new ArrayList<ShopGunError>());
    private final LoaderDelivery<T> mDelivery;
    private final Object LOCK = new Object();

    public LoaderRequest(Listener<T> l) {
        this(null, l);
    }

    public LoaderRequest(T data, Listener<T> l) {
        super(Method.PUT, null, null);
        mData = data;
        mListener = l;
        mDelivery = new LoaderDelivery<T>(mListener);
    }

    public T getData() {
        return mData;
    }

    public Request setData(T data) {
        mData = data;
        return this;
    }

    public Request addError(ShopGunError e) {
        mErrors.add(e);
        return this;
    }

    public List<ShopGunError> getErrors() {
        return mErrors;
    }

    @Override
    public Request setRequestQueue(RequestQueue requestQueue) {
        if (getTag() == null) {
            // Attach a tag if one haven't been provided
            // This will be used at a cancellation signal
            setTag(new Object());
        }
        super.setDelivery(this);
        super.setRequestQueue(requestQueue);

        synchronized (LOCK) {
            mRequests.addAll(createRequests(mData));

            // RequestCreator-class unfortunately returns null, so we'll have to clean the list
            for (Iterator<Request> it = mRequests.iterator(); it.hasNext();) {
                if (it.next() == null) it.remove();
            }

            if (mRequests.isEmpty()) {
                finish("loaderRequest-has-no-subRequests-to-perform");
            } else {
                addEvent("sub-requests-added-to-request-queue");
                for (Request r : mRequests) {
                    r.addEvent("added-from-loader-request");
                    applyState(r);
                    getRequestQueue().add(r);
                }
            }
        }

        addEvent("cancelled-to-force-cache-dispatcher-to-drop-loader-request");
        super.cancel();
        return this;
    }

    @Override
    public Request setDelivery(Delivery d) {
        throw new IllegalStateException("Custom delivery not allowed for LoaderRequests");
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        return Response.fromError(new InternalOkError());
    }

    @Override
    protected Response<T> parseCache(Cache c) {
        return Response.fromError(new InternalOkError());
    }

    /**
     * Method for creating the needed requests for filling out a given object
     * @return A list of Request
     */
    public abstract List<Request> createRequests(T data);

    /**
     * Method for applying the current request state to the given request
     * @param r A {@link Request} to apply state to.
     */
    private void applyState(Request r) {
        // mimic parent behaviour
        r.setDebugger(getDebugger());
        r.setDelivery(this);
        r.setTag(getTag());
        r.setIgnoreCache(ignoreCache());
        r.setTimeOut(getTimeOut());
        r.setUseLocation(useLocation());
    }

    @Override
    public void cancel() {
        synchronized (LOCK) {
            super.cancel();
            if (getRequestQueue() != null) {
                getRequestQueue().cancelAll(getTag());
            }
        }
    }

    @Override
    public Request finish(String reason) {
        if (super.isFinished()) {
            // If no sub-requests was generated in setRequestQueue(RequestQueue),
            // this LoaderRequest have already finished it self. But the CacheDispatcher
            // will also call finish(String), so we'll just silence it.
            return this;
        }
        return super.finish(reason);
    }

    @Override
    public boolean isFinished() {
        for (Request r : mRequests) {
            if (!r.isFinished()) {
                return false;
            }
        }
        return super.isFinished();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void postResponse(Request<?> request, Response<?> response) {

        synchronized (LOCK) {

            request.addEvent("post-response");

            // Deliver catalog if needed
            mRequests.remove(request);
            boolean finished = mRequests.isEmpty();
            mDelivery.deliver(request, response, mData, mErrors, !finished);

        }

    }

    /** Callback interface for delivering parsed responses. */
    public interface Listener<T> {
        /**
         * Called when a response is received.
         * @param response The response data.
         * @param errors A list of {@link ShopGunError} that occurred during execution.
         */
        void onRequestComplete(T response, List<ShopGunError> errors);

        /**
         * Called every time a request finishes.
         * <p>This is done on the network thread. Doing intensive work from this thread is discouraged</p>
         * @param response The current state of the response data, this is not a complete set.
         * @param errors A list of {@link ShopGunError} that occurred during execution.
         */
        void onRequestIntermediate(T response, List<ShopGunError> errors);
    }

}
