package com.eTilbudsavis.etasdk.request.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.Dealer;
import com.eTilbudsavis.etasdk.EtaObjects.Store;
import com.eTilbudsavis.etasdk.EtaObjects.Interface.ICatalog;
import com.eTilbudsavis.etasdk.EtaObjects.Interface.IDealer;
import com.eTilbudsavis.etasdk.EtaObjects.Interface.IStore;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Network.Impl.JsonArrayRequest;
import com.eTilbudsavis.etasdk.Utils.Api;
import com.eTilbudsavis.etasdk.Utils.Api.Endpoint;
import com.eTilbudsavis.etasdk.Utils.Api.Param;
import com.eTilbudsavis.etasdk.request.RequestAutoFill;
import com.eTilbudsavis.etasdk.request.RequestAutoFill.AutoFillParams;
import com.eTilbudsavis.etasdk.request.RequestAutoFill.OnAutoFillCompleteListener;
import com.eTilbudsavis.etasdk.request.RequestFilter;
import com.eTilbudsavis.etasdk.request.RequestOrder;
import com.eTilbudsavis.etasdk.request.RequestParameter;

public abstract class ListRequest<T> extends JsonArrayRequest {
	
	private DeliveryHelper<T> mDelivery;
	private RequestAutoFill<T> mAutoFiller;
	
	private static final String ERROR_NO_REQUESTQUEUE = 
			"Request must initially be added to RequestQueue, subsequent pagination requests can be performed with next() method";
	
	public ListRequest(String url, Listener<T> listener) {
		super(url, null);
		init(listener);
	}
	
	public ListRequest(Method method, String url, Listener<T> listener) {
		super(method, url, null);
		init(listener);
	}
	
	public ListRequest(Method method, String url, JSONArray requestBody, Listener<T> listener) {
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
	
	public void nextPage() {
		getLog().add("request-next-page");
		pageChange(getOffset()+getLimit());
	}

	public void prevPage() {
		getLog().add("request-previous-page");
		pageChange(getOffset()-getLimit());
	}
	
	private void pageChange(int offset) {
		if (getRequestQueue() == null) {
			throw new IllegalStateException(ERROR_NO_REQUESTQUEUE);
		}
		setOffset(offset);
		resetstate();
		getRequestQueue().add(this);
	}
	
	@Override
	public void cancel() {
		super.cancel();
		if (mAutoFiller != null) {
			mAutoFiller.cancel();
		}
	}
	
	public static abstract class Builder<T> extends com.eTilbudsavis.etasdk.request.Builder<T> {
		
		private ListRequest<T> mRequest;
		private RequestAutoFill<T> mAutofill;
		
		public ListRequest<T> build() {
			if (mAutofill != null) {
				mRequest.setAutoFill(mAutofill);
			}
			return mRequest;
		}
		
		public Builder(ListRequest<T> r) {
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
	
	public static class Order extends RequestOrder {

		public Order(String defaultOrder) {
			super(defaultOrder);
		}
		
	}
	
	public static class Filter extends RequestFilter<Set<String>> {
		
		protected boolean add(String filter, String id) {
			Map<String, Set<String>> map = getFilters();
			if (map.containsKey(filter) && map.get(filter) != null) {
				map.get(filter).add(id);
			} else {
				Set<String> ids = new HashSet<String>();
				ids.add(id);
				map.put(filter, ids);
			}
			return true;
		}
		
	}
	
	public static class Parameter extends RequestParameter {
		
		/**
		 * The API relies on pagination for retrieving data. Therefore you need to
		 * define the offset to the first item in the requested list, when querying for data.
		 * If no offset is set it will default to 0.
		 * @param offset to first item in list
		 * @return this object
		 */
		public Parameter setOffset(int offset) {
			put(Api.Param.OFFSET, String.valueOf(offset));
			return this;
		}
		
		/**
		 * Get the offset parameter used for the query.
		 * @return offset
		 */
		public int getOffset() {
			String offset = getFilter().get(Api.Param.OFFSET);
			if (offset != null) {
				return Integer.valueOf(offset);
			}
			return 0;
		}
		
		/**
		 * The API relies on pagination for retrieving data. Therefore you need to
		 * define a limit for the data you want to retrieve. If no limit is set
		 * this will default to {@link #DEFAULT_LIMIT} if no limit is set.
		 * @param limit
		 * @return this object
		 */
		public Parameter setLimit(int limit) {
			put(Api.Param.LIMIT, String.valueOf(limit));
			return this;
		}
		
		/**
		 * Get the upper limit on how many items the API should return.
		 * @return max number of items API should return
		 */
		public int getLimit() {
			String offset = getFilter().get(Api.Param.LIMIT);
			if (offset != null) {
				return Integer.valueOf(offset);
			}
			return 0;
		}
		
	}
	
	public static abstract class ListAutoFill<T extends List<?>> extends RequestAutoFill<T> {
		
		protected JsonArrayRequest getDealerRequest(final List<? extends IDealer<?>> list) {
			
			Set<String> ids = new HashSet<String>(list.size());
			for (IDealer<?> item : list) {
				ids.add(item.getDealerId());
			}
			
			JsonArrayRequest req = new JsonArrayRequest(Endpoint.DEALER_LIST, new Listener<JSONArray>() {
				
				public void onComplete(JSONArray response, EtaError error) {
					
					if (response != null) {
						List<Dealer> dealers = Dealer.fromJSON(response);
						for(IDealer<?> item : list) {
							for(Dealer d: dealers) {
								if (item.getDealerId().equals(d.getId())) {
									item.setDealer(d);
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

		protected JsonArrayRequest getStoreRequest(final List<? extends IStore<?>> list) {
			
			Set<String> ids = new HashSet<String>(list.size());
			for (IStore<?> item : list) {
				ids.add(item.getStoreId());
			}
			
			JsonArrayRequest req = new JsonArrayRequest(Endpoint.STORE_LIST, new Listener<JSONArray>() {
				
				public void onComplete(JSONArray response, EtaError error) {
					
					if (response != null) {
						List<Store> stores = Store.fromJSON(response);
						for(IStore<?> item : list) {
							for(Store s: stores) {
								if (item.getStoreId().equals(s.getId())) {
									item.setStore(s);
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

		protected JsonArrayRequest getCatalogRequest(final List<? extends ICatalog<?>> list) {
			
			Set<String> ids = new HashSet<String>(list.size());
			for (ICatalog<?> item : list) {
				ids.add(item.getCatalogId());
			}
			
			JsonArrayRequest req = new JsonArrayRequest(Endpoint.CATALOG_LIST, new Listener<JSONArray>() {
				
				public void onComplete(JSONArray response, EtaError error) {
					
					if (response != null) {
						List<Catalog> catalogss = Catalog.fromJSON(response);
						for(ICatalog<?> item : list) {
							for(Catalog c: catalogss) {
								if (item.getCatalogId().equals(c.getId())) {
									item.setCatalog(c);
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
			
			req.setIds(Param.CATALOG_IDS, ids);
			return req;
		}
		
	}
	
}
