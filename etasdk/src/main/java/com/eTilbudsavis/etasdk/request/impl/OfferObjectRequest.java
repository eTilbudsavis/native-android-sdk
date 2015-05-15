package com.eTilbudsavis.etasdk.request.impl;

import com.eTilbudsavis.etasdk.model.Offer;
import com.eTilbudsavis.etasdk.model.Store;
import com.eTilbudsavis.etasdk.network.Request;
import com.eTilbudsavis.etasdk.network.Response.Listener;
import com.eTilbudsavis.etasdk.request.RequestAutoFill;
import com.eTilbudsavis.etasdk.request.impl.StoreObjectRequest.StoreAutoFill;
import com.eTilbudsavis.etasdk.utils.Api;

import java.util.ArrayList;
import java.util.List;

public class OfferObjectRequest extends ObjectRequest<Store> {
	
	private OfferObjectRequest(String url, Listener<Store> l) {
		super(url, l);
	}
	
	public static abstract class Builder extends ObjectRequest.Builder<Store> {
		
		public Builder(String storeId, Listener<Store> l) {
			super(new OfferObjectRequest(Api.Endpoint.storeId(storeId), l));
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
	
	public static class OfferAutoFill extends RequestAutoFill<Offer> {

		private boolean mCatalogs;
		private boolean mDealer;
		private boolean mStore;
		
		public OfferAutoFill() {
			this(false, false, false);
		}
		
		public OfferAutoFill(boolean catalogs, boolean dealer, boolean store) {
			mCatalogs = catalogs;
			mDealer = dealer;
			mStore = store;
		}
		
		@Override
		public List<Request<?>> createRequests(Offer data) {
			
			List<Request<?>> reqs = new ArrayList<Request<?>>();
			
			if (data != null) {
				
				if (mStore) {
					reqs.add(getStoreRequest(data));
				}
				
				if (mDealer) {
					reqs.add(getDealerRequest(data));
				}
				
				if (mCatalogs) {
					reqs.add(getCatalogRequest(data));
				}
				
			}
			
			return reqs;
		}
		
	}

}
