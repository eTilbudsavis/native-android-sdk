package com.eTilbudsavis.etasdk.NetworkInterface;

import java.util.concurrent.Executor;

import android.os.Handler;

public class Delivery {
	
	/** Used for posting responses, typically to the main thread. */
    private final Executor mResponsePoster;

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
    
    /**
     * Post the Response to a Request, back to the UI-thread, and then trigger the listener waiting for the callback.
     * @param request made by the user
     * @param a response response from the API fulfilling the Request
     */
    public void postResponse(Request<?> request, Response<?> response) {
    	request.addEvent("post-response");
    	
    	if (request.getHandler() != null) {
    		request.getHandler().post(new DeliveryRunnable(request, response));
    	} else {
            mResponsePoster.execute(new DeliveryRunnable(request, response));
    	}
    	
    	
    }
    
    /**
     * A Runnable used for delivering network responses to a listener on the UI-thread.
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
        	
            mRequest.addEvent("request-on-main-thread");
            
            // If this request has canceled, finish it and don't deliver.
            if (mRequest.isCanceled()) {
            	mRequest.finish("cancelled-at-delivery");
                return;
            }
            
        	mRequest.finish("execution-finished-succesfully");
        	mRequest.debugPrint();
            mRequest.deliverResponse(mResponse.result, mResponse.error);
            
       }
    }
}
