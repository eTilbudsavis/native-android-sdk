package com.eTilbudsavis.etasdk.request.impl;

import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Network.Impl.JsonObjectRequest;
import com.eTilbudsavis.etasdk.request.RequestAutoFill;
import com.eTilbudsavis.etasdk.request.RequestAutoFill.AutoFillParams;
import com.eTilbudsavis.etasdk.request.RequestAutoFill.OnAutoFillCompleteListener;

public abstract class ObjectRequest<T> extends JsonObjectRequest {
	
	private DeliveryHelper<T> mDelivery;
	private RequestAutoFill<T> mAutoFiller;
	
	public ObjectRequest(String url, Listener<T> listener) {
		super(url, null);
		init(listener);
	}
	
	public ObjectRequest(Method method, String url, JSONObject requestBody, Listener<T> listener) {
		super(method, url, requestBody, null);
		init(listener);
	}

	private void init(Listener<T> listener) {
		mDelivery = new DeliveryHelper<T>(this, listener);
		setDeliverOnThread(true);
	}
	
	public Request<?> setAutoFill(RequestAutoFill<T> filler) {
		mAutoFiller = filler;
		return this;
	}
	
	public RequestAutoFill<T> getAutoFill() {
		return mAutoFiller;
	}
	
	protected void runAutoFill(final T response, final EtaError error) {
		addEvent("delivery-intercepted");
		if (response == null) {
			mDelivery.deliver(response, error);
		} else {
			mAutoFiller.setAutoFillParams(new AutoFillParams(this));
			mAutoFiller.setOnAutoFillCompleteListener(new OnAutoFillCompleteListener() {

				public void onComplete() {
					mDelivery.deliver(response, error);
				}
			});
			mAutoFiller.execute(response, getRequestQueue());
		}
		
	}
	
	@Override
	public void cancel() {
		super.cancel();
		if (mAutoFiller != null) {
			mAutoFiller.cancel();
		}
	}
	
	public static abstract class Builder<T> extends com.eTilbudsavis.etasdk.request.Builder<T> {
		
		private ObjectRequest<T> mRequest;
		private RequestAutoFill<T> mAutofill;
		
		public ObjectRequest<T> build() {
			if (mAutofill != null) {
				mRequest.setAutoFill(mAutofill);
			}
			return mRequest;
		}
		
		public Builder(ObjectRequest<T> r) {
			super(r);
			mRequest = r;
		}
		
		protected RequestAutoFill<T> getAutofill() {
			return mAutofill;
		}
		
		protected void setAutoFiller(RequestAutoFill<T> filler) {
			mAutofill = filler;
		}
		
	}
	
}
