/*******************************************************************************
 * Copyright 2015 eTilbudsavis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.eTilbudsavis.etasdk.request.impl;

import java.util.ArrayList;
import java.util.List;

import com.eTilbudsavis.etasdk.model.Offer;
import com.eTilbudsavis.etasdk.model.Store;
import com.eTilbudsavis.etasdk.network.Request;
import com.eTilbudsavis.etasdk.network.Response.Listener;
import com.eTilbudsavis.etasdk.request.RequestAutoFill;
import com.eTilbudsavis.etasdk.request.impl.StoreObjectRequest.StoreAutoFill;
import com.eTilbudsavis.etasdk.utils.Api;

public class OfferObjectRequest extends ObjectRequest<Store> {
	
	private OfferObjectRequest(String url, Listener<Store> l) {
		super(url, l);
	}
	
	public static abstract class Builder extends ObjectRequest.Builder<Store> {
		
		public Builder(String storeId, Listener<Store> l) {
			super(new OfferObjectRequest(Api.Endpoint.storeId(storeId), l));
		}
		
		public ObjectRequest<Store> build() {
			ObjectRequest<Store> r = super.build();
			if (getAutofill()==null) {
				setAutoFiller(new StoreAutoFill());
			}
			return r;
		}

		public void setAutoFill(StoreAutoFill filler) {
			super.setAutoFiller(filler);
		}
		
	}
	
	public static class OfferAutoFill extends RequestAutoFill<Offer> {

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
		public List<Request<?>> createRequests(Offer data) {
			
			List<Request<?>> reqs = new ArrayList<Request<?>>();
			
			if (data != null) {
				
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
		
	}

}
