package com.eTilbudsavis.etasdk.request.impl;

import org.json.JSONException;

import android.os.Handler;
import android.os.Looper;

import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Log.EventLog;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.Response.Listener;

public class DeliveryHelper<T> implements Runnable {
	
	public static final String TAG = DeliveryHelper.class.getSimpleName();
	
	private Request<?> mRequest;
	private Listener<T> mListener;
	private T mData;
	private EtaError mError;

	public DeliveryHelper(Request<?> r, Listener<T> l) {
		mRequest = r;
		mListener = l;
	}
	
	public void deliver(T data, final EtaError error) {
		
		mData = data;
		mError = error;
		
		if (mRequest.getHandler() == null) {
			new Handler(Looper.getMainLooper()).post(this);
		} else {
			mRequest.getHandler().post(this);
		}
		
	}
	
	public void run() {
		
        mRequest.addEvent("request-on-new-thread");
        
        if (!mRequest.isCanceled()) {
        	mRequest.addEvent("performing-callback-to-original-listener");
        	
        	if (mRequest.getDebugger() != null) {
        		EtaLog.d(TAG, "Total duration: " + mRequest.getLog().getTotalDuration());
        	}
        	
        	mListener.onComplete(mData, mError);
        }
        
	}
	
}
