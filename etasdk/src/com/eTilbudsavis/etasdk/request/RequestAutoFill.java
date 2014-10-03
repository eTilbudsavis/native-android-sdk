package com.eTilbudsavis.etasdk.request;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.Dealer;
import com.eTilbudsavis.etasdk.EtaObjects.Store;
import com.eTilbudsavis.etasdk.EtaObjects.Interface.ICatalog;
import com.eTilbudsavis.etasdk.EtaObjects.Interface.IDealer;
import com.eTilbudsavis.etasdk.EtaObjects.Interface.IStore;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Page;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Network.Delivery;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.RequestDebugger;
import com.eTilbudsavis.etasdk.Network.RequestQueue;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Network.Impl.JsonArrayRequest;
import com.eTilbudsavis.etasdk.Network.Impl.JsonObjectRequest;
import com.eTilbudsavis.etasdk.Network.Impl.ThreadDelivery;
import com.eTilbudsavis.etasdk.Utils.Api;
import com.eTilbudsavis.etasdk.Utils.Api.Endpoint;
import com.eTilbudsavis.etasdk.Utils.Api.Param;

public abstract class RequestAutoFill<T> {
	
	public static final String TAG = Eta.TAG_PREFIX + RequestAutoFill.class.getSimpleName();
	
	private Listener<T> mListener;
	private T mData;
	private EtaError mError;
	private List<Request<?>> mRequests = new ArrayList<Request<?>>();

	public abstract List<Request<?>> createRequests(T data);
	
	public void run(AutoFillParams params, T data, EtaError e, RequestQueue rq, Listener<T> l) {
		mListener = l;
		mRequests.clear();
		mData = data;
		mError = e;
		mRequests = createRequests(mData);
		if (mData != null) {
			
			for (Request<?> r : mRequests) {
				r.addEvent("executed-by-autofiller");
				params.applyParams(r);
				r.setDelivery(new ThreadDelivery(Eta.getInstance().getExecutor()));
				rq.add(r);
			}
			
		}
		done();
	}
	
	protected void done() {
		if (isFinished()) {
			mListener.onComplete(mData, mError);
		}
	}

	public List<Request<?>> getRequests() {
		return mRequests;
	}
	
	/**
	 * Returns true if ALL requests in this {@link RequestAutoFill} is finished
	 * @return true if all {@link RequestAutoFill} are finished, else false
	 */
	public boolean isFinished() {
		for (Request<?> r : mRequests) {
			if (!r.isFinished()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns true if ALL requests in this {@link RequestAutoFill} is cancelled
	 * @return true if all {@link RequestAutoFill} are cancelled, else false
	 */
	public boolean isCancled() {
		for (Request<?> r : mRequests) {
			if (!r.isCanceled()) {
				return false;
			}
		}
		return true;
	}
	
	public void cancel() {
		for (Request<?> r : mRequests) {
			r.cancel();
		}
	}

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
		req.setDelivery(new ThreadDelivery(Eta.getInstance().getExecutor()));
		req.setIds(Param.DEALER_IDS, ids);
		return req;
	}

	protected JsonObjectRequest getDealerRequest(final IDealer<?> item) {
		String url = Api.Endpoint.dealerId(item.getDealerId());
		JsonObjectRequest r = new JsonObjectRequest(url, new Listener<JSONObject>() {

			public void onComplete(JSONObject response, EtaError error) {
				
				if (response != null) {
					item.setDealer(Dealer.fromJSON(response));
				} else {
					EtaLog.d(TAG, error.toJSON().toString());
				}
				done();
			}
		});
		r.setDelivery(new ThreadDelivery(Eta.getInstance().getExecutor()));
		return r;
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
		req.setDelivery(new ThreadDelivery(Eta.getInstance().getExecutor()));
		req.setIds(Param.STORE_IDS, ids);
		return req;
	}

	protected JsonObjectRequest getStoreRequest(final IStore<?> item) {
		String url = Api.Endpoint.storeId(item.getStoreId());
		JsonObjectRequest r = new JsonObjectRequest(url, new Listener<JSONObject>() {

			public void onComplete(JSONObject response, EtaError error) {
				
				if (response != null) {
					item.setStore(Store.fromJSON(response));
				} else {
					EtaLog.d(TAG, error.toJSON().toString());
				}
				done();
			}
		});
		r.setDelivery(new ThreadDelivery(Eta.getInstance().getExecutor()));
		return r;
	}

	protected JsonArrayRequest getCatalogRequest(final List<? extends ICatalog<?>> list) {
		
		Set<String> ids = new HashSet<String>(list.size());
		for (ICatalog<?> item : list) {
			ids.add(item.getCatalogId());
		}
		
		Listener<JSONArray> l = new Listener<JSONArray>() {
			
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
		};
		
		JsonArrayRequest req = new JsonArrayRequest(Endpoint.CATALOG_LIST, l);
		req.setDelivery(new ThreadDelivery(Eta.getInstance().getExecutor()));
		req.setIds(Param.CATALOG_IDS, ids);
		return req;
	}

	protected JsonObjectRequest getCatalogRequest(final ICatalog<?> item) {
		String url = Api.Endpoint.dealerId(item.getCatalogId());
		JsonObjectRequest r = new JsonObjectRequest(url, new Listener<JSONObject>() {
			
			public void onComplete(JSONObject response, EtaError error) {
				
				if (response != null) {
					item.setCatalog(Catalog.fromJSON(response));
				} else {
					EtaLog.d(TAG, error.toJSON().toString());
				}
				done();
			}
		});
		r.setDelivery(new ThreadDelivery(Eta.getInstance().getExecutor()));
		return r;
	}

	protected JsonArrayRequest getPagesRequest(final Catalog c) {
		
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

	protected JsonArrayRequest getHotspotsRequest(final Catalog c) {
		
		JsonArrayRequest req = new JsonArrayRequest(Endpoint.catalogHotspots(c.getId()), new Listener<JSONArray>() {
			
			public void onComplete(JSONArray response, EtaError error) {
				if (response != null) {
					//TODO set hotspots
					EtaLog.d(TAG, response.toString());
				} else {
					EtaLog.d(TAG, error.toJSON().toString());
				}
				done();
			}
		});
		
		return req;
		
	}
	
	public static class AutoFillParams {
		
		private Object tag = null;
		private RequestDebugger debugger = null;
		private Delivery delivery = null;
		private boolean useLocation = true;
		private boolean ignoreCache = false;
//		private boolean isCachable = true;
		
		public void applyParams(Request<?> r) {
			r.setTag(tag);
			r.setDebugger(debugger);
			r.setUseLocation(useLocation);
			r.setDelivery(delivery);
			r.setIgnoreCache(ignoreCache);
		}
		
		public AutoFillParams() {
			this(new Object(), null, null, true, false);
		}
		
		public AutoFillParams(Request<?> parent) {
			this(parent.getTag(), parent.getDebugger(), parent.getDelivery(), parent.useLocation(), parent.ignoreCache());
		}
		
		public AutoFillParams(Object tag, RequestDebugger debugger, Delivery d, boolean useLocation, boolean ignoreCache) {
			this.tag = (tag == null ? new Object() : tag);
			this.debugger = debugger;
			this.delivery = d;
			this.useLocation = useLocation;
			this.ignoreCache = ignoreCache;
		}
		
	}
}
