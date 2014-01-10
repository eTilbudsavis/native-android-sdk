package com.eTilbudsavis.etasdk.NetworkInterface;

import java.util.concurrent.Executor;

import android.os.Handler;

import com.eTilbudsavis.etasdk.NetworkHelpers.EtaError;

public class Delivery {
	
	/** Used for posting responses, typically to the main thread. */
    private final Executor mResponsePoster;
    public RequestQueue mRequestQueue;

    /**
     * Creates a new response delivery interface.
     * @param handler {@link Handler} to post responses on
     */
    public Delivery(final Handler handler) {
        // Make an Executor that just wraps the handler.
        mResponsePoster = new Executor() {
        	
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
    }
    
    public void postResponse(Request<?> request, Response<?> response) {
    	done(request);
        mResponsePoster.execute(new DeliveryRunnable(request, response));
    }

    public void postError(Request<?> request, EtaError error) {
    	done(request);
        Response<?> response = Response.fromError(error);
        mResponsePoster.execute(new DeliveryRunnable(request, response));
    }
    
    private void done(Request<?> r) {
    	if (mRequestQueue != null) {
        	mRequestQueue.complete(r);
    	}
    }
    
    /**
     * A Runnable used for delivering network responses to a listener on the
     * main thread.
     */
    @SuppressWarnings("rawtypes")
    private class DeliveryRunnable implements Runnable {
        private final Request mRequest;
        private final Response mResponse;

        public DeliveryRunnable(Request request, Response response) {
            mRequest = request;
            mResponse = response;
        }

        @SuppressWarnings("unchecked")
        public void run() {
            // If this request has canceled, finish it and don't deliver.
            if (mRequest.isCanceled()) {
                return;
            }
            
            mRequest.deliverResponse(mResponse);
            
       }
    }
}
