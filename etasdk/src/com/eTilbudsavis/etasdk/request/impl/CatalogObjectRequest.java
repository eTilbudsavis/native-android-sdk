package com.eTilbudsavis.etasdk.request.impl;

import java.util.ArrayList;
import java.util.List;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.Api;
import com.eTilbudsavis.etasdk.request.RequestAutoFill;

public class CatalogObjectRequest extends ObjectRequest<Catalog> {
	
	private CatalogObjectRequest(String url, Listener<Catalog> l) {
		super(url, l);
	}
	
	public static abstract class Builder extends ObjectRequest.Builder<Catalog> {
		
		public Builder(String catalogId, Listener<Catalog> l) {
			super(new CatalogObjectRequest(Api.Endpoint.catalogId(catalogId), l));
		}
		
		public ObjectRequest<Catalog> build() {
			ObjectRequest<Catalog> r = super.build();
			if (getAutofill()==null) {
				setAutoFiller(new CatalogAutoFill());
			}
			return r;
		}

		public void setAutoFill(CatalogAutoFill filler) {
			super.setAutoFiller(filler);
		}
		
	}
	
	public static class CatalogAutoFill extends RequestAutoFill<Catalog> {
		
		private boolean mPages;
		private boolean mDealer;
		private boolean mStore;
		
		public CatalogAutoFill() {
			this(false, false, false);
		}
		
		public CatalogAutoFill(boolean pages, boolean dealer, boolean store) {
			mPages = pages;
			mDealer = dealer;
			mStore = store;
		}
		
		@Override
		public List<Request<?>> createRequests(Catalog data) {
			
			List<Request<?>> reqs = new ArrayList<Request<?>>();
			
			if (data != null) {
				
				if (mStore) {
					reqs.add(getStoreRequest(data));
				}
				
				if (mDealer) {
					reqs.add(getDealerRequest(data));
				}
				
				if (mPages) {
					reqs.add(getPagesRequest(data));
				}
				
			}
			
			return reqs;
		}
		
	}

}
