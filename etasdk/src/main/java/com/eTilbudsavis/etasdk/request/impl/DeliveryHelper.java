/*******************************************************************************
 * Copyright 2015 eTilbudsavis
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

package com.eTilbudsavis.etasdk.request.impl;

import android.os.Handler;
import android.os.Looper;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.network.Delivery;
import com.eTilbudsavis.etasdk.network.EtaError;
import com.eTilbudsavis.etasdk.network.Request;
import com.eTilbudsavis.etasdk.network.Response;
import com.eTilbudsavis.etasdk.network.Response.Listener;

public class DeliveryHelper<T> implements Delivery {
	
	public static final String TAG = Constants.getTag(DeliveryHelper.class);

	private Request<?> mRequest;
	private Listener<T> mListener;
//	private Delivery mClientDelivery;
	
	public DeliveryHelper(Request<?> r, Listener<T> l) {
		mListener = l;
		mRequest = r;
	}
	
	public void postResponse(Request<?> request, Response<?> response) {
    	request.addEvent("post-response-to-executor-service");
    	Eta.getInstance().getExecutor().execute(new DeliveryRunnable(request, response));
	}
    
	public void deliver(T data, final EtaError error) {
		
		Runnable run = new DeliveryHelperRunnable(data, error);
		new Handler(Looper.getMainLooper()).post(run);
		
	}
	
//	public void setDelivery(Delivery d) {
//		mClientDelivery = d;
//	}
//	
//	public Delivery getDelivery() {
//		return mClientDelivery;
//	}
	
    public class DeliveryHelperRunnable implements Runnable {
    	
    	private T mData;
    	private EtaError mError;
    	
        public DeliveryHelperRunnable(T data, final EtaError error) {
            mData = data;
            mError = error;
        }
        
        public void run() {
        	
            mRequest.addEvent("request-on-new-thread");
            
            if (!mRequest.isCanceled()) {
            	mRequest.addEvent("performing-callback-to-original-listener");
            	if (mRequest.getDebugger() != null) {
            		mRequest.getDebugger().onDelivery(mRequest);
            	}
            	mListener.onComplete(mData, mError);
            }
            
       }
        
    }

}
