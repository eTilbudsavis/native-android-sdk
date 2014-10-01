package com.eTilbudsavis.etasdk.request.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;

import com.eTilbudsavis.etasdk.EtaObjects.Store;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.Api;
import com.eTilbudsavis.etasdk.Utils.Api.Endpoint;

public class StoreListRequest extends ListRequest<List<Store>> {
	
	private StoreListRequest(Listener<List<Store>> l) {
		super(Endpoint.STORE_LIST, l);
	}
	
	@Override
	public void deliverResponse(JSONArray response, EtaError error) {
		List<Store> offers = null;
		if (response != null) {
			offers = Store.fromJSON(response);
		}
		runAutoFill(offers, error);
	}
	
	public static class Builder extends ListRequest.Builder<List<Store>>{
		
		public Builder(Listener<List<Store>> l) {
			super(new StoreListRequest(l));
		}
		
		public void setParameters(Parameter params) {
			super.setParameters(params);
		}
		
		public void setAutoFill(StoreAutoFill filler) {
			super.setAutoFiller(filler);
		}
		
		@Override
		public ListRequest<List<Store>> build() {
			
			if (getParameters() == null) {
				setParameters(new Parameter());
			}
			
			if (getAutofill() == null) {
				setAutoFiller(new StoreAutoFill());
			}
			
			return super.build();
		}
		
	}

	public static class Parameter extends ListRequest.ListParameterBuilder {
		
		public void addOfferFilter(Set<String> offerIds) {
			addFilter(Api.Param.OFFER_IDS, offerIds);
		}

		public void addCatalogFilter(Set<String> catalogIds) {
			addFilter(Api.Param.CATALOG_IDS, catalogIds);
		}
		
		public void addDealerFilter(Set<String> dealerIds) {
			addFilter(Api.Param.DEALER_IDS, dealerIds);
		}
		
		public void addStoreFilter(Set<String> storeIds) {
			addFilter(Api.Param.STORE_IDS, storeIds);
		}
		
		public void addOfferFilter(String offerId) {
			addFilter(Api.Param.OFFER_IDS, offerId);
		}
		
		public void addCatalogFilter(String catalogId) {
			addFilter(Api.Param.CATALOG_IDS, catalogId);
		}
		
		public void addDealerFilter(String dealerId) {
			addFilter(Api.Param.DEALER_IDS, dealerId);
		}
		
		public void addStoreFilter(String storeId) {
			addFilter(Api.Param.STORE_IDS, storeId);
		}
		
	}
	
	public static class StoreAutoFill extends ListRequest.ListAutoFill<List<Store>> {
		
		private boolean mDealer;
		
		public StoreAutoFill() {
			this(false);
		}
		
		public StoreAutoFill(boolean dealer) {
			mDealer = dealer;
		}
		
		@Override
		public List<Request<?>> createRequests(List<Store> data) {
			
			List<Request<?>> reqs = new ArrayList<Request<?>>();
			
			if (!data.isEmpty()) {
				
				if (mDealer) {
					reqs.add(getDealerRequest(data));
				}
				
			}
			
			return reqs;
		}
		
	}

}
