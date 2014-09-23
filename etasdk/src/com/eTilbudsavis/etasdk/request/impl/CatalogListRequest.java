package com.eTilbudsavis.etasdk.request.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.Dealer;
import com.eTilbudsavis.etasdk.EtaObjects.Store;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Network.Impl.JsonArrayRequest;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.Param;
import com.eTilbudsavis.etasdk.request.ListRequestBuilder;
import com.eTilbudsavis.etasdk.request.RequestAutoFill;
import com.eTilbudsavis.etasdk.request.RequestOrder;

public class CatalogListRequest extends ListRequest<List<Catalog>> {
	
	private CatalogListRequest(Listener<JSONArray> listener, Listener<List<Catalog>> objListener) {
		super(Endpoint.CATALOG_LIST, listener, objListener);
	}
	
	public static class Builder extends ListRequestBuilder<List<Catalog>>{
		
		public Builder(Listener<List<Catalog>> l) {
			super(l);
			setRequest(new CatalogListRequest(new Listener<JSONArray>() {
				
				public void onComplete(JSONArray response, EtaError error) {
					List<Catalog> catalogs = null;
					if (response != null) {
						catalogs = Catalog.fromJSON(response);
					}
					getRequest().runAutoFiller(catalogs, error);
				}
			}, l));
			
		}
		
		public void setFilter(Filter filter) {
			super.setFilter(filter);
		}
		
		public void setOrder(Order order) {
			super.setOrder(order);
		}
		
		public void setParameters(Parameter params) {
			super.setParameters(params);
		}
		
		public void setAutoFill(CatalogAutoFill filler) {
			super.setAutoFiller(filler);
		}
		
		@Override
		public ListRequest<List<Catalog>> build() {
			
			if (getFilter() == null) {
				setFilter(new Filter());
			}
			
			if (getOrder() == null) {
				setOrder(new Order());
			}
			
			if (getParameters() == null) {
				setParameters(new Parameter());
			}
			
			if (getAutofill() == null) {
				setAutoFiller(new CatalogAutoFill());
			}
			
			return super.build();
		}
		
	}
	
	public static class Filter extends ListFilter {
		
		public void addCatalogFilter(Set<String> catalogIds) {
			add(CATALOG_IDS, catalogIds);
		}
		
		public void addDealerFilter(Set<String> dealerIds) {
			add(DEALER_IDS, dealerIds);
		}
		
		public void addStoreFilter(Set<String> storeIds) {
			add(STORE_IDS, storeIds);
		}
		
		public void addCatalogFilter(String catalogId) {
			add(CATALOG_IDS, catalogId);
		}
		
		public void addDealerFilter(String dealerId) {
			add(DEALER_IDS, dealerId);
		}
		
		public void addStoreFilter(String storeId) {
			add(STORE_IDS, storeId);
		}
		
	}
	
	public static class Order extends RequestOrder {
		
		public Order() {
			super("-" + POPULARITY);
		}
		
		public void byPopularity(boolean enable, boolean descending) {
			setOrder(enable, descending, POPULARITY);
		}
		
		public void byDealer(boolean enable, boolean descending) {
			setOrder(enable, descending, DEALER);
		}
		
		public void byCreated(boolean enable, boolean descending) {
			setOrder(enable, descending, CREATED);
		}

		public void byExpirationDate(boolean enable, boolean descending) {
			setOrder(enable, descending, EXPIRATION_DATE);
		}
		
		public void byPublicationDate(boolean enable, boolean descending) {
			setOrder(enable, descending, PUBLICATION_DATE);
		}
		
		public void byDistance(boolean enable, boolean descending) {
			setOrder(enable, descending, DISTANCE);
		}
		
	}
	
	public static class Parameter extends ListParameter {
		// Intentionally left empty to create a new type, but with all parent properties
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
		
		private JsonArrayRequest getDealerRequest(final List<Catalog> catalogs) {
			
			Set<String> ids = new HashSet<String>(catalogs.size());
			for (Catalog c : catalogs) {
				ids.add(c.getDealerId());
			}
			
			JsonArrayRequest req = new JsonArrayRequest(Endpoint.DEALER_LIST, new Listener<JSONArray>() {
				
				public void onComplete(JSONArray response, EtaError error) {
					
					if (response != null) {
						List<Dealer> dealers = Dealer.fromJSON(response);
						for(Catalog c : catalogs) {
							for(Dealer d: dealers) {
								if (c.getDealerId().equals(d.getId())) {
									c.setDealer(d);
									break;
								}
							}
						}
						
					}
					
					done();
					
				}
			});
			
			req.setIds(Param.FILTER_DEALER_IDS, ids);
			return req;
		}

		private JsonArrayRequest getPagesRequest(Catalog c) {
			
			return null;
		}
		
		private JsonArrayRequest getStoreRequest(final List<Catalog> catalogs) {
			
			Set<String> ids = new HashSet<String>(catalogs.size());
			for (Catalog c : catalogs) {
				ids.add(c.getStoreId());
			}
			
			JsonArrayRequest req = new JsonArrayRequest(Endpoint.STORE_LIST, new Listener<JSONArray>() {
				
				public void onComplete(JSONArray response, EtaError error) {
					
					if (response != null) {
						List<Store> stores = Store.fromJSON(response);
						for(Catalog c : catalogs) {
							for(Store s: stores) {
								if (c.getStoreId().equals(s.getId())) {
									c.setStore(s);
									break;
								}
							}
						}
						
					}
					done();
					
				}
			});
			
			req.setIds(Param.FILTER_STORE_IDS, ids);
			return req;
		}

	}

}
