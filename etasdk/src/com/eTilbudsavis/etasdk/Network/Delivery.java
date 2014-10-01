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



public interface Delivery {
	
    /**
     * Post the {@link Request} and {@link Response}, on to another thread, 
     * and then trigger the listener waiting for the callback.
     * @param request A {@link Request}
     * @param response A {@link Response} to the {@link Request}
     */
    public void postResponse(Request<?> request, Response<?> response);
    
    public class DeliveryRunnable implements Runnable {
    	
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
