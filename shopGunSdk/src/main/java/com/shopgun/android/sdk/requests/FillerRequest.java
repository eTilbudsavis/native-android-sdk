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

import android.os.Handler;
import android.os.Looper;

import com.shopgun.android.sdk.network.Cache;
import com.shopgun.android.sdk.network.Delivery;
import com.shopgun.android.sdk.network.NetworkResponse;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.RequestQueue;
import com.shopgun.android.sdk.network.Response;
import com.shopgun.android.sdk.network.ShopGunError;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class FillerRequest<T> extends Request<T> implements Delivery {

    public static final String TAG = FillerRequest.class.getSimpleName();

    private static final int INTERNAL_ERROR_OK_SIGNAL = Integer.MAX_VALUE;

    private final Listener<T> mListener;
    private final ArrayList<Request> mRequests = new ArrayList<Request>();
    private final T mData;
    private final ArrayList<ShopGunError> mErrors = new ArrayList<ShopGunError>();

    public FillerRequest(T data, Listener<T> l) {
        super(Method.PUT, null, null);
        mData = data;
        mListener = l;
    }

    public T getData() {
        return mData;
    }

    public void addError(ShopGunError e) {
        mErrors.add(e);
    }

    @Override
    public Request setRequestQueue(RequestQueue requestQueue) {
        if (getTag() == null) {
            // Attach a tag if one haven't been provided
            // This will be used at a cancellation signal
            setTag(new Object());
        }
        setDelivery(this);
        return super.setRequestQueue(requestQueue);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        return getHackResponse();
    }

    @Override
    protected Response<T> parseCache(Cache c) {
        return getHackResponse();
    }

    private Response<T> getHackResponse() {
        String message = String.format("%s internal OK", TAG);
        String details = String.format("A %s internal signal to run the remaining %s request queue", TAG, TAG);
        return Response.fromError(new ShopGunError(INTERNAL_ERROR_OK_SIGNAL, message, details));
    }

    /**
     * Method for creating the needed requests for filling out a given object
     * @return A list of Request
     */
    public abstract List<Request> createRequests();

    @Override
    public void deliverResponse(T response, ShopGunError error) {
        if (error != null && error.getCode() == INTERNAL_ERROR_OK_SIGNAL) {
            // Perform the rest of the request
            runFillerRequests();
        } else {
            // Something bad, ignore and deliver
            postResponseMain();
        }
    }

    private void runFillerRequests() {
        synchronized (mRequests) {
            List<Request> tmp = createRequests();
            Iterator<Request> it = tmp.iterator();
            while (it.hasNext()) {
                Request r = it.next();
                if (r==null) {
                    it.remove();
                }
            }
            mRequests.addAll(tmp);
            if (mRequests.isEmpty()) {
                postResponseMain();
            } else {
                for (Request r : mRequests) {
                    applyState(r);
                    getRequestQueue().add(r);
                }
            }
        }
    }

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
        synchronized (FillerRequest.class) {
            if (!isCanceled()) {
                super.cancel();
                getRequestQueue().cancelAll(getTag());
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void postResponse(Request<?> request, Response<?> response) {
        request.addEvent("post-response");

        if (this.equals(request)) {

            // if it's this request
            deliverResponse( (T)response.result, response.error);

        } else {

            // Update catalog (shouldn't be using network threads to deliver responses)
            new DeliveryRunnable(request,response).run();

            // Deliver catalog if needed
            synchronized (mRequests) {
                mRequests.remove(request);
                if (mRequests.isEmpty()) {
                    postResponseMain();
                } else {
                    addEvent("intermediate-delivery");
                    mListener.onFillIntermediate(mData, mErrors);
                }
            }

        }
    }

    private void postResponseMain() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            internalDelivery();
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    addEvent("request-on-new-thread");
                    internalDelivery();
                }
            });
        }
    }

    private void internalDelivery() {
        if (isCanceled()) {
            finish("cancelled-at-delivery");
        } else {
            finish("execution-finished-successfully");
            mListener.onFillComplete(mData, mErrors);
        }
    }

    /** Callback interface for delivering parsed responses. */
    public interface Listener<T> {
        /**
         * Called when a response is received.
         * @param response The response data.
         * @param errors A list of {@link ShopGunError} that occurred during execution.
         */
        void onFillComplete(T response, List<ShopGunError> errors);

        /**
         * Called every time a request finishes.
         * <p>This is done on the network thread. Doing intensive work from this thread is discouraged</p>
         * @param response The current state of the response data, this is not a complete set.
         * @param errors A list of {@link ShopGunError} that occurred during execution.
         */
        void onFillIntermediate(T response, List<ShopGunError> errors);
    }

}
