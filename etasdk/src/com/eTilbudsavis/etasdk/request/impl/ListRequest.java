package com.eTilbudsavis.etasdk.request.impl;

import org.json.JSONArray;

import android.os.Handler;
import android.os.Looper;

import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Network.Impl.JsonArrayRequest;
import com.eTilbudsavis.etasdk.request.RequestAutoFill;
import com.eTilbudsavis.etasdk.request.RequestAutoFill.AutoFillParams;
import com.eTilbudsavis.etasdk.request.RequestAutoFill.OnAutoFillCompleteListener;

public class ListRequest<T> extends JsonArrayRequest {
	
	private static final String ERROR_NO_REQUESTQUEUE = 
			"Request must initially be added to RequestQueue, subsequent pagination requests can be performed with next() method";
	
	private Listener<T> mListener;
	private RequestAutoFill<T> mFiller;
	
	public ListRequest(String url, Listener<JSONArray> listener, Listener<T> objListener) {
		super(url, listener);
		mListener = objListener;
	}
	
	public ListRequest(Method method, String url, Listener<JSONArray> listener) {
		super(method, url, listener);
	}
	
	public ListRequest(Method method, String url, JSONArray requestBody, Listener<JSONArray> listener) {
		super(method, url, requestBody, listener);
	}
	
	public void setAutoFill(RequestAutoFill<T> filler) {
		mFiller = filler;
	}
	
	@Override
	public void cancel() {
		mFiller.cancel();
		super.cancel();
	}
	
	protected void runAutoFiller(final T data, final EtaError error) {
		addEvent("callback-intercepted");
		if (data == null) {
			deliver(data, error);
		} else {
			mFiller.execute(data, getRequestQueue(), new AutoFillParams(this), new OnAutoFillCompleteListener() {

				public void onComplete() {
					deliver(data, error);
				}
				
			});
		}
	}
	
	private void deliver(final T data, final EtaError error) {
		
		Runnable r = new Runnable() {
			
			public void run() {
				
	            addEvent("request-on-new-thread");
	            
	            if (!isCanceled()) {
	            	addEvent("performing-callback-to-original-listener");
	            	mListener.onComplete(data, error);
	            }
			}
		};
		
		if (getHandler() == null) {
			new Handler(Looper.getMainLooper()).post(r);
		} else {
			getHandler().post(r);
		}
		
	}
	
	@Override
	public boolean deliverOnThread() {
		return mFiller != null;
	}
	
	public void nextPage() {
		getLog().add("request-next-page");
		pageChange(getOffset()+getLimit());
	}

	public void prevPage() {
		getLog().add("request-previous-page");
		pageChange(getOffset()-getLimit());
	}
	
	private void pageChange(int offset) {
		if (getRequestQueue() == null) {
			throw new IllegalStateException(ERROR_NO_REQUESTQUEUE);
		}
		setOffset(offset);
		resetstate();
		getRequestQueue().add(this);
	}
	
}
