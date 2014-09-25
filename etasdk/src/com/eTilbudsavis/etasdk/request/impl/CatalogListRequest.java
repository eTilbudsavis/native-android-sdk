package com.eTilbudsavis.etasdk.request.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.Dealer;
import com.eTilbudsavis.etasdk.EtaObjects.Store;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Page;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Network.Impl.JsonArrayRequest;
import com.eTilbudsavis.etasdk.Utils.Api;
import com.eTilbudsavis.etasdk.Utils.Api.Endpoint;
import com.eTilbudsavis.etasdk.Utils.Api.Param;
import com.eTilbudsavis.etasdk.request.RequestAutoFill;

public class CatalogListRequest extends ListRequest<List<Catalog>> {
	
	private CatalogListRequest(Listener<List<Catalog>> l) {
		super(Endpoint.CATALOG_LIST, l);
	}
	
	@Override
	protected void deliverResponse(JSONArray response, EtaError error) {
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
	
	public static class Filter extends ListRequest.Filter {
		
		public void addCatalogFilter(Set<String> catalogIds) {
			add(Api.Param.CATALOG_IDS, catalogIds);
		}
		
		public void addDealerFilter(Set<String> dealerIds) {
			add(Api.Param.DEALER_IDS, dealerIds);
		}
		
		public void addStoreFilter(Set<String> storeIds) {
			add(Api.Param.STORE_IDS, storeIds);
		}
		
		public void addCatalogFilter(String catalogId) {
			add(Api.Param.CATALOG_IDS, catalogId);
		}
		
		public void addDealerFilter(String dealerId) {
			add(Api.Param.DEALER_IDS, dealerId);
		}
		
		public void addStoreFilter(String storeId) {
			add(Api.Param.STORE_IDS, storeId);
		}
		
	}
	
	public static class Order extends ListRequest.Order {
		
		public Order() {
			super("-" + Api.Sort.POPULARITY);
		}
		
		public void byPopularity(boolean enable, boolean descending) {
			setOrder(enable, descending, Api.Sort.POPULARITY);
		}
		
		public void byDealer(boolean enable, boolean descending) {
			setOrder(enable, descending, Api.Sort.DEALER);
		}
		
		public void byCreated(boolean enable, boolean descending) {
			setOrder(enable, descending, Api.Sort.CREATED);
		}

		public void byExpirationDate(boolean enable, boolean descending) {
			setOrder(enable, descending, Api.Sort.EXPIRATION_DATE);
		}
		
		public void byPublicationDate(boolean enable, boolean descending) {
			setOrder(enable, descending, Api.Sort.PUBLICATION_DATE);
		}
		
		public void byDistance(boolean enable, boolean descending) {
			setOrder(enable, descending, Api.Sort.DISTANCE);
		}
		
	}
	
	public static class Parameter extends ListRequest.Parameter {
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
						
					} else {
						EtaLog.d(TAG, error.toJSON().toString());
					}
					
					done();
					
				}
			});
			
			req.setIds(Param.DEALER_IDS, ids);
			return req;
		}
		
		private JsonArrayRequest getPagesRequest(final Catalog c) {
			
			JsonArrayRequest req = new JsonArrayRequest(Endpoint.catalogPages(c.getId()), new Listener<JSONArray>() {
				
				public void onComplete(JSONArray response, EtaError error) {
					if (response != null) {
						c.setPages(Page.fromJSON(response));
					} else {
						EtaLog.d(TAG, error.toJSON().toString());
					}
					done();
				}
			});
			return req;
			
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
						
					} else {
						EtaLog.d(TAG, error.toJSON().toString());
					}
					done();
					
				}
			});
			
			req.setIds(Param.STORE_IDS, ids);
			return req;
		}
		
	}

}
