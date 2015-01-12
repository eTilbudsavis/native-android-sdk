package com.eTilbudsavis.etasdk.request.impl;

import java.util.ArrayList;
import java.util.List;

import com.eTilbudsavis.etasdk.model.Store;
import com.eTilbudsavis.etasdk.request.RequestAutoFill;
import com.eTilbudsavis.etasdk.utils.Api;

public class StoreObjectRequest extends ObjectRequest<Store> {
	
	private StoreObjectRequest(String url, Listener<Store> l) {
		super(url, l);
	}
	
	public static abstract class Builder extends ObjectRequest.Builder<Store> {
		
		public Builder(String storeId, Listener<Store> l) {
			super(new StoreObjectRequest(Api.Endpoint.storeId(storeId), l));
		}
		
		public ObjectRequest<Store> build() {
			ObjectRequest<Store> r = super.build();
			if (getAutofill()==null) {
				setAutoFiller(new StoreAutoFill());
			}
			return r;
		}

		public void setAutoFill(StoreAutoFill filler) {
			super.setAutoFiller(filler);
		}
		
	}
	
	public static class StoreAutoFill extends RequestAutoFill<Store> {

		private boolean mDealer;
		
		public StoreAutoFill() {
			this(false);
		}
		
		public StoreAutoFill(boolean dealer) {
			mDealer = dealer;
		}
		
		@Override
		public List<Request<?>> createRequests(Store data) {
			
			List<Request<?>> reqs = new ArrayList<Request<?>>();
			
			if (data != null) {
				
				if (mDealer) {
					reqs.add(getDealerRequest(data));
				}
				
			}
			
			return reqs;
		}
		
	}

}
