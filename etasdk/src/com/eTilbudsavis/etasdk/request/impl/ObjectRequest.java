package com.eTilbudsavis.etasdk.request.impl;

import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Network.Delivery;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Network.Impl.JsonObjectRequest;
import com.eTilbudsavis.etasdk.request.RequestAutoFill;
import com.eTilbudsavis.etasdk.request.RequestAutoFill.AutoFillParams;

public abstract class ObjectRequest<T> extends JsonObjectRequest {
	
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
		super.setDelivery(new DeliveryHelper<T>(this, listener));
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
		getAutoFill().run(new AutoFillParams(this), response, error, getRequestQueue(), new Listener<T>() {

			public void onComplete(T response, EtaError error) {
				((DeliveryHelper<T>)getDelivery()).deliver(response, error);
			}
		});
		
	}
	
	@Override
	public void cancel() {
		super.cancel();
		if (mAutoFiller != null) {
			mAutoFiller.cancel();
		}
	}
	
	@Override
	public Request<?> setDelivery(Delivery delivery) {
		String msg = "ObjectRequest does not support setting Delivery. All requests are returned to UI Thread";
		throw new UnsupportedOperationException(msg);
	}
	
	public static abstract class Builder<T> extends com.eTilbudsavis.etasdk.request.Builder<ObjectRequest<T>> {
		
		private RequestAutoFill<T> mAutofill;
		
		public ObjectRequest<T> build() {
			ObjectRequest<T> r = super.build();
			if (mAutofill != null) {
				r.setAutoFill(mAutofill);
			}
			return r;
		}
		
		public Builder(ObjectRequest<T> r) {
			super(r);
		}
		
		protected RequestAutoFill<T> getAutofill() {
			return mAutofill;
		}
		
		protected void setAutoFiller(RequestAutoFill<T> filler) {
			mAutofill = filler;
		}
		
	}
	
}
