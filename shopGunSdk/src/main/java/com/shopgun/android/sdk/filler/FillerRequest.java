package com.shopgun.android.sdk.filler;

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
import java.util.List;

public abstract class FillerRequest<T> extends Request<T> implements Delivery {

    public static final String TAG = FillerRequest.class.getSimpleName();

    private static final int ERROR_OK = Integer.MAX_VALUE;

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
            setTag(new Object());
        }
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
        String s = "This technically isn't an error, but a fake OK signal to run the rest of the queue";
        return Response.fromError(new ShopGunError(ERROR_OK, "Internal OK hack", s));
    }

    /**
     * Method for creating the needed requests for filling out a given object
     * @return A list of Request
     */
    public abstract List<Request> createRequests();

    @Override
    public void deliverResponse(T response, ShopGunError error) {
        if (error != null && error.getCode() == ERROR_OK) {
            // Perform the rest of the request
            runFillerRequests();
        } else {
            // Something bad, ignore and deliver
            postResponseMain();
        }
    }

    private void runFillerRequests() {
        mRequests.clear();
        mRequests.addAll(createRequests());
        for (Request r : mRequests) {
            addRequest(r);
        }
    }

    private void addRequest(Request r) {
        // mimic parent behaviour
        r.setDebugger(getDebugger());
        r.setDelivery(this);
        r.setTag(getTag());
        r.setIgnoreCache(ignoreCache());
        r.setTimeOut(getTimeOut());
        r.setUseLocation(useLocation());
        getRequestQueue().add(r);
    }

    @Override
    public void cancel() {
        synchronized (FillerRequest.class) {
            super.cancel();
            for (Request r : mRequests) {
                r.cancel();
            }
        }
    }

    @Override
    public void postResponse(Request<?> request, Response<?> response) {
        request.addEvent("post-response");

        // Update catalog (shouldn't be using network threads to deliver responses)
        new DeliveryRunnable(request,response).run();

        // Deliver catalog if needed
        mRequests.remove(request);
        if (mRequests.isEmpty() && !isFinished()) {
            postResponseMain();
        }

    }

    private void postResponseMain() {
        finish("execution-finished-successfully");
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
            mListener.onFillComplete(mData, mErrors);
        }
    }

    /** Callback interface for delivering parsed responses. */
    public interface Listener<T> {
        /** Called when a response is received. */
        void onFillComplete(T response, List<ShopGunError> errors);
    }

}
