package com.eTilbudsavis.etasdk.request;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;

import android.os.Handler;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.Dealer;
import com.eTilbudsavis.etasdk.EtaObjects.Store;
import com.eTilbudsavis.etasdk.EtaObjects.Interface.ICatalog;
import com.eTilbudsavis.etasdk.EtaObjects.Interface.IDealer;
import com.eTilbudsavis.etasdk.EtaObjects.Interface.IStore;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.RequestDebugger;
import com.eTilbudsavis.etasdk.Network.RequestQueue;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Network.Impl.JsonArrayRequest;
import com.eTilbudsavis.etasdk.Utils.Api.Endpoint;
import com.eTilbudsavis.etasdk.Utils.Api.Param;

public abstract class RequestAutoFill<T> {
	
	public static final String TAG = RequestAutoFill.class.getSimpleName();
	
	private OnAutoFillCompleteListener mListener;
	private T mData;
	private AutoFillParams mParams;
	private List<Request<?>> mRequests = new ArrayList<Request<?>>();
	
	public abstract List<Request<?>> createRequests(T data);
	
	public void setAutoFillParams(AutoFillParams params) {
		mParams = params;
	}
	
	public void setOnAutoFillCompleteListener(OnAutoFillCompleteListener l) {
		mListener = l;
	}
	
	public void execute(T data, RequestQueue rq) {
		mRequests.clear();
		mData = data;
		mRequests.addAll(createRequests(mData));
		runRequests(rq);
		done();
	}
	
	private void runRequests(RequestQueue rq) {
		
		for (Request<?> r : mRequests) {
			r.addEvent("executed-by-autofiller");
			mParams.applyParams(r);
			r.setDeliverOnThread(true);
			rq.add(r);
		}
		
	}

	protected void done() {
		if (isFinished()) {
			mListener.onComplete();
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
	
	public interface OnAutoFillCompleteListener {
		public void onComplete();
	}
	
	public static class AutoFillParams {
		
		private Object tag = null;
		private RequestDebugger debugger = null;
		private Handler handler = null;
		private boolean useLocation = true;
		private boolean ignoreCache = false;
//		private boolean isCachable = true;
		
		public void applyParams(Request<?> r) {
			r.setTag(tag);
			r.setDebugger(debugger);
			r.setUseLocation(useLocation);
			r.setHandler(handler);
			r.setIgnoreCache(ignoreCache);
		}
		
		public AutoFillParams() {
			this(new Object(), null, null, true, false);
		}
		
		public AutoFillParams(Request<?> parent) {
			this(parent.getTag(), parent.getDebugger(), parent.getHandler(), parent.useLocation(), parent.ignoreCache());
		}
		
		public AutoFillParams(Object tag, RequestDebugger debugger, Handler h, boolean useLocation, boolean ignoreCache) {
			this.tag = (tag == null ? new Object() : tag);
			this.debugger = debugger;
			this.handler = h;
			this.useLocation = useLocation;
			this.ignoreCache = ignoreCache;
		}
		
	}
}
