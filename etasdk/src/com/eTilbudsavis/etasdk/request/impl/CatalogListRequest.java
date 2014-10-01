package com.eTilbudsavis.etasdk.request.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.Api;
import com.eTilbudsavis.etasdk.Utils.Api.Endpoint;
import com.eTilbudsavis.etasdk.request.RequestAutoFill;
import com.eTilbudsavis.etasdk.request.RequestParameter;

public class CatalogListRequest extends ListRequest<List<Catalog>> {
	
	private CatalogListRequest(Listener<List<Catalog>> l) {
		super(Endpoint.CATALOG_LIST, l);
	}
	
	@Override
	public void deliverResponse(JSONArray response, EtaError error) {
		List<Catalog> mCatalogs = null;
		if (response != null) {
			mCatalogs = Catalog.fromJSON(response);
		}
		runAutoFill(mCatalogs, error);
	}
	
	public static class Builder extends ListRequest.Builder<List<Catalog>>{
		
		public Builder(Listener<List<Catalog>> l) {
			super(new CatalogListRequest(l));
		}
		
		public Builder(CatalogListParameterBuilder parameter, Listener<List<Catalog>> l) {
			super(new CatalogListRequest(l));
			setParameters(parameter);
		}
		
		public void setParameters(RequestParameter params) {
			super.setParameters(params);
		}
		
		public void setAutoFill(CatalogAutoFill filler) {
			super.setAutoFiller(filler);
		}
		
		@Override
		public ListRequest<List<Catalog>> build() {
			
			if (getParameters() == null) {
				setParameters(new CatalogListParameterBuilder());
			}
			
			if (getAutofill() == null) {
				setAutoFiller(new CatalogAutoFill());
			}
			
			return super.build();
		}
		
	}

	public static class CatalogListParameterBuilder extends ListParameterBuilder {
		
		public CatalogListParameterBuilder() {
			setDefaultOrder("-" + Api.Sort.POPULARITY);
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
		
		public void addCatalogFilter(String catalogId) {
			addFilter(Api.Param.CATALOG_IDS, catalogId);
		}
		
		public void addDealerFilter(String dealerId) {
			addFilter(Api.Param.DEALER_IDS, dealerId);
		}
		
		public void addStoreFilter(String storeId) {
			addFilter(Api.Param.STORE_IDS, storeId);
		}
		
		public void orderByPopularity(boolean descending) {
			addOrder(Api.Sort.POPULARITY, descending);
		}
		
		public void removeOrderPopularity() {
			removeOrder(Api.Sort.POPULARITY);
		}
		
		public void orderByDealer(boolean enable, boolean descending) {
			addOrder(Api.Sort.DEALER, descending);
		}

		public void removeOrderDealer() {
			removeOrder(Api.Sort.DEALER);
		}
		
		public void orderByCreated(boolean enable, boolean descending) {
			addOrder(Api.Sort.CREATED, descending);
		}

		public void removeOrderCreated() {
			removeOrder(Api.Sort.CREATED);
		}
		
		public void orderByExpirationDate(boolean enable, boolean descending) {
			addOrder(Api.Sort.EXPIRATION_DATE, descending);
		}

		public void removeOrderExpirationDate() {
			removeOrder(Api.Sort.EXPIRATION_DATE);
		}
		
		public void orderByPublicationDate(boolean enable, boolean descending) {
			addOrder(Api.Sort.PUBLICATION_DATE, descending);
		}

		public void removeOrderPublicationDate() {
			removeOrder(Api.Sort.PUBLICATION_DATE);
		}
		
		public void orderByDistance(boolean enable, boolean descending) {
			addOrder(Api.Sort.DISTANCE, descending);
		}

		public void removeOrderDistance() {
			removeOrder(Api.Sort.DISTANCE);
		}
		
	}
	
	public static class CatalogAutoFill extends RequestAutoFill<List<Catalog>> {
		
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
		public List<Request<?>> createRequests(List<Catalog> data) {
			
			List<Request<?>> reqs = new ArrayList<Request<?>>();
			
			if (!data.isEmpty()) {
				
				if (mStore) {
					reqs.add(getStoreRequest(data));
				}
				
				if (mDealer) {
					reqs.add(getDealerRequest(data));
				}
				
				if (mPages) {
					
					for (Catalog c : data) {
						reqs.add(getPagesRequest(c));
					}
					
				}
				
			}
			
			return reqs;
		}
		
	}

}
