package com.eTilbudsavis.etasdk.Network.Impl;

import android.os.Handler;
import android.os.Looper;

import com.eTilbudsavis.etasdk.Network.Delivery;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.Response;

public class HandlerDelivery implements Delivery {
	
	/** Used for posting responses, typically to the main thread. */
	private final Handler mHandler;
	
    /**
     * Creates a new response delivery interface, that delivers the response to the UI thread.
     * @param handler {@link Handler} to post responses on
     */
    public HandlerDelivery() {
    	this(new Handler(Looper.getMainLooper()));
    }
    
    /**
     * Creates a new response delivery interface, that delivers the response to the thread attached to the delivery.
     * @param handler {@link Handler} to post responses on
     */
    public HandlerDelivery(Handler h) {
    	mHandler = h;
    }
    
    /**
     * Post the Response to a Request, back to the UI-thread, and then trigger the listener waiting for the callback.
     * @param request made by the user
     * @param a response response from the API fulfilling the Request
     */
    public void postResponse(Request<?> request, Response<?> response) {
    	request.addEvent("post-response");
    	
    	if (request.getDelivery() != null) {
    		request.getDelivery().postResponse(request, response);
    	} else {
        	mHandler.post(new DeliveryRunnable(request, response));
    	}
    	
    }
    
}