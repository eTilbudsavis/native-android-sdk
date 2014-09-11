package com.eTilbudsavis.etasdk.request.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.Store;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Network.Impl.JsonArrayRequest;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.Param;
import com.eTilbudsavis.etasdk.request.RequestAutoFill;

public class CatalogAutoFill extends RequestAutoFill {
	
	private List<Catalog> mCatalogs;
	private boolean mPages;
	private boolean mDealer;
	private boolean mStore;
	
	public void fillPages(boolean getPages) {
		mPages = getPages;
	}

	public void fillDealer(boolean getDealer) {
		mDealer = getDealer;
	}

	public void fillStore(boolean getStore) {
		mStore = getStore;
	}

	public void fill(Request<?> r, Catalog c, OnAutoFillComplete listener) {
		mCatalogs = new ArrayList<Catalog>();
		mCatalogs.add(c);
		run(r, listener);
	}
	
	public void fill(Request<?> r, List<Catalog> list, OnAutoFillComplete listener) {
		mCatalogs = list;
		run(r, listener);
	}
	
	protected void run(Request<?> r, OnAutoFillComplete listener) {
		
		if (isRunning() || r.isCanceled()) {
			return;
		}
		
		super.run(r, listener);
		
		if (mStore) {
			appendStores();
		}

		if (mDealer) {
			appendStores();
		}

		if (mPages) {
			appendStores();
		}
		
		done();
		
	}
	
	private void appendStores() {
		
		Set<String> ids = new HashSet<String>(mCatalogs.size());
		for (Catalog c : mCatalogs) {
			ids.add(c.getStoreId());
		}
		
		JsonArrayRequest storeReq = new JsonArrayRequest(Endpoint.STORE_LIST, new Listener<JSONArray>() {
			
			public void onComplete(JSONArray response, EtaError error) {
				
				if (response != null) {
					List<Store> stores = Store.fromJSON(response);
					for(Catalog c : mCatalogs) {
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
		
		storeReq.setIds(Param.FILTER_STORE_IDS, ids);
		add(storeReq);
		
	}
	
}
