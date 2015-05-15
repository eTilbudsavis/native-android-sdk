package com.eTilbudsavis.etasdk.request;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.Catalog;
import com.eTilbudsavis.etasdk.model.Dealer;
import com.eTilbudsavis.etasdk.model.HotspotMap;
import com.eTilbudsavis.etasdk.model.Images;
import com.eTilbudsavis.etasdk.model.Store;
import com.eTilbudsavis.etasdk.model.interfaces.ICatalog;
import com.eTilbudsavis.etasdk.model.interfaces.IDealer;
import com.eTilbudsavis.etasdk.model.interfaces.IStore;
import com.eTilbudsavis.etasdk.network.Delivery;
import com.eTilbudsavis.etasdk.network.EtaError;
import com.eTilbudsavis.etasdk.network.Request;
import com.eTilbudsavis.etasdk.network.RequestDebugger;
import com.eTilbudsavis.etasdk.network.RequestQueue;
import com.eTilbudsavis.etasdk.network.Response.Listener;
import com.eTilbudsavis.etasdk.network.impl.JsonArrayRequest;
import com.eTilbudsavis.etasdk.network.impl.JsonObjectRequest;
import com.eTilbudsavis.etasdk.network.impl.ThreadDelivery;
import com.eTilbudsavis.etasdk.utils.Api;
import com.eTilbudsavis.etasdk.utils.Api.Endpoint;
import com.eTilbudsavis.etasdk.utils.Api.Param;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class RequestAutoFill<T> {
	
	public static final String TAG = Constants.getTag(RequestAutoFill.class);
	
	private Listener<T> mListener;
	private T mData;
	private EtaError mError;
	private AutoFillParams mParams;
	private List<Request<?>> mRequests = new ArrayList<Request<?>>();
	private Eta mEta;
	
	public abstract List<Request<?>> createRequests(T data);
	
	public RequestAutoFill() {
		mEta = Eta.getInstance();
	}
	
	public void prepare(AutoFillParams params, T data, Listener<T> l) {
		prepare(params, data, null, l);
	}
	
	public void prepare(AutoFillParams params, T data, EtaError e, Listener<T> l) {
		mParams = params;
		mListener = l;
		mData = data;
		mError = e;
	}
	
	public void execute(RequestQueue rq) {
		
		mRequests.clear();
		if (mData != null) {
			mRequests = createRequests(mData);
			for (Request<?> r : mRequests) {
				r.addEvent("executed-by-autofiller");
				mParams.applyParams(r);
				r.setDelivery(new ThreadDelivery(mEta.getExecutor()));
				rq.add(r);
			}
		} else if (mError==null) {
			mError = new AutoLoadError(new Exception("The data provided is null"));
		}
		done();
		
	}
	
	protected void done() {
		if (isFinished()) {
			if (mData==null && mError==null) {
				mError = new AutoLoadError(new Exception("Daiam"));
			}
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
		req.setDelivery(new ThreadDelivery(mEta.getExecutor()));
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
		r.setDelivery(new ThreadDelivery(mEta.getExecutor()));
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
		req.setDelivery(new ThreadDelivery(mEta.getExecutor()));
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
		r.setDelivery(new ThreadDelivery(mEta.getExecutor()));
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
		req.setDelivery(new ThreadDelivery(mEta.getExecutor()));
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
		r.setDelivery(new ThreadDelivery(mEta.getExecutor()));
		return r;
	}

	protected JsonArrayRequest getPagesRequest(final Catalog c) {
		
		JsonArrayRequest req = new JsonArrayRequest(Endpoint.catalogPages(c.getId()), new Listener<JSONArray>() {
			
			public void onComplete(JSONArray response, EtaError error) {
				if (response != null) {
					c.setPages(Images.fromJSON(response));
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
					c.setHotspots(HotspotMap.fromJSON(c.getDimension(), response));
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

		public AutoFillParams setTag(Object tag) {
			this.tag = tag;
			return this;
		}
		
		public Object getTag() {
			return tag;
		}

		public AutoFillParams setDebugger(RequestDebugger debugger) {
			this.debugger = debugger;
			return this;
		}
		
		public RequestDebugger getDebugger() {
			return debugger;
		}

		public AutoFillParams setDelivery(Delivery delivery) {
			this.delivery = delivery;
			return this;
		}
		
		public Delivery getDelivery() {
			return delivery;
		}

		public AutoFillParams setUseLocation(boolean useLocation) {
			this.useLocation = useLocation;
			return this;
		}
		
		public boolean useLocation() {
			return useLocation;
		}

		public AutoFillParams setIgnoreCache(boolean ignoreCache) {
			this.ignoreCache = ignoreCache;
			return this;
		}
		
		public boolean getIgnoreCache() {
			return ignoreCache;
		}
		
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
