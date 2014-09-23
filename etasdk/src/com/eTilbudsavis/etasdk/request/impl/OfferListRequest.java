package com.eTilbudsavis.etasdk.request.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;

import com.eTilbudsavis.etasdk.EtaObjects.Dealer;
import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.Store;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Network.Impl.JsonArrayRequest;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.Param;
import com.eTilbudsavis.etasdk.request.RequestAutoFill;

public class OfferListRequest extends ListRequest<List<Offer>> {
	
	private OfferListRequest(Listener<List<Offer>> l) {
		super(Endpoint.OFFER_LIST, l);
	}
	
	@Override
	protected void deliverResponse(JSONArray response, EtaError error) {
		List<Offer> offers = null;
		if (response != null) {
			offers = Offer.fromJSON(response);
		}
		runAutoFill(offers, error);
	}
	
	public static class Builder extends ListRequest.Builder<List<Offer>>{
		
		public Builder(Listener<List<Offer>> l) {
			super(new OfferListRequest(l));
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
		
		public void setAutoFill(OfferAutoFill filler) {
			super.setAutoFiller(filler);
		}
		
		@Override
		public ListRequest<List<Offer>> build() {
			
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
				setAutoFiller(new OfferAutoFill());
			}
			
			return super.build();
		}
		
	}
	
	public static class Filter extends ListRequest.Filter {
		// TODO lookup API doc to find relevant filters
	}
	
	public static class Order extends ListRequest.Order {
		
		public Order() {
			super("-" + POPULARITY);
		}
		// TODO lookup API doc to find relevant order
		
	}
	
	public static class Parameter extends ListRequest.Parameter {
		// TODO lookup API doc to find relevant filters
	}
	
	public static class OfferAutoFill extends RequestAutoFill<List<Offer>> {
		
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
		public List<Request<?>> createRequests(List<Offer> data) {
			
			List<Request<?>> reqs = new ArrayList<Request<?>>();
			
			if (!data.isEmpty()) {
				
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

		private JsonArrayRequest getDealerRequest(final List<Offer> offers) {
			
			Set<String> ids = new HashSet<String>(offers.size());
			for (Offer c : offers) {
				ids.add(c.getDealerId());
			}
			
			JsonArrayRequest req = new JsonArrayRequest(Endpoint.DEALER_LIST, new Listener<JSONArray>() {
				
				public void onComplete(JSONArray response, EtaError error) {
					
					if (response != null) {
						List<Dealer> dealers = Dealer.fromJSON(response);
						for(Offer o : offers) {
							for(Dealer d: dealers) {
								if (o.getDealerId().equals(d.getId())) {
									o.setDealer(d);
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

		private JsonArrayRequest getCatalogRequest(final List<Offer> offers) {
			
			Set<String> ids = new HashSet<String>(offers.size());
			for (Offer c : offers) {
				ids.add(c.getCatalogId());
			}
			
			JsonArrayRequest req = new JsonArrayRequest(Endpoint.CATALOG_LIST, new Listener<JSONArray>() {
				
				public void onComplete(JSONArray response, EtaError error) {
					
					if (response != null) {
						List<Dealer> dealers = Dealer.fromJSON(response);
						for(Offer o : offers) {
							for(Dealer d: dealers) {
								if (o.getDealerId().equals(d.getId())) {
									o.setDealer(d);
									break;
								}
							}
						}
						
					}
					
					done();
					
				}
			});
			
			req.setIds(Param.FILTER_CATALOG_IDS, ids);
			return req;
		}
		
		private JsonArrayRequest getStoreRequest(final List<Offer> offers) {
			
			Set<String> ids = new HashSet<String>(offers.size());
			for (Offer o : offers) {
				ids.add(o.getStoreId());
			}
			
			JsonArrayRequest req = new JsonArrayRequest(Endpoint.STORE_LIST, new Listener<JSONArray>() {
				
				public void onComplete(JSONArray response, EtaError error) {
					
					if (response != null) {
						List<Store> stores = Store.fromJSON(response);
						for(Offer o : offers) {
							for(Store s: stores) {
								if (o.getStoreId().equals(s.getId())) {
									o.setStore(s);
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
