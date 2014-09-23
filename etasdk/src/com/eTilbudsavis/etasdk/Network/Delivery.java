/*******************************************************************************
* Copyright 2014 eTilbudsavis
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
*******************************************************************************/
package com.eTilbudsavis.etasdk.Network;

import java.util.concurrent.ExecutorService;

import android.os.Handler;
import android.os.Looper;

public class Delivery {
	
	/** Used for posting responses, typically to the main thread. */
	private final Handler mHandler;
    private final ExecutorService mExecutorService;

    /**
     * Creates a new response delivery interface.
     * @param handler {@link Handler} to post responses on
     */
    public Delivery() {
    	this(new Handler(Looper.getMainLooper()), null);
    }

    /**
     * Creates a new response delivery interface.
     * @param handler {@link Handler} to post responses on
     */
    public Delivery(ExecutorService executor) {
    	this(new Handler(Looper.getMainLooper()), executor);
    }
    
    /**
     * Creates a new response delivery interface.
     * @param handler {@link Handler} to post responses on
     */
    public Delivery(Handler h, ExecutorService executor) {
    	mHandler = h;
    	mExecutorService = executor;
    }
    
    /**
     * Post the Response to a Request, back to the UI-thread, and then trigger the listener waiting for the callback.
     * @param request made by the user
     * @param a response response from the API fulfilling the Request
     */
    public void postResponse(Request<?> request, Response<?> response) {
    	request.addEvent("post-response");
    	
    	DeliveryRunnable runner = new DeliveryRunnable(request, response);
    	
    	if (request.getHandler() != null) {
    		// Deliver to custom handler (custom thread)
    		request.getHandler().post(runner);
    	} else if (request.deliverOnThread() && mExecutorService != null) {
    		// Deliver to a SDK provided thread (to e.g. perform an autocomplete action)
    		mExecutorService.submit(runner);
    	} else {
    		// Return to the main thread, this is default behavior
            mHandler.post(runner);
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
        	
            mRequest.addEvent("request-on-new-thread");
            
            if (mRequest.isCanceled()) {
            	mRequest.finish("cancelled-at-delivery");
            } else {
            	mRequest.finish("execution-finished-succesfully");
                mRequest.deliverResponse(mResponse.result, mResponse.error);
            }
            
            
       }
    }
}
