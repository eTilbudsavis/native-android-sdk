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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;

import android.text.TextUtils;

import com.eTilbudsavis.etasdk.network.Delivery;
import com.eTilbudsavis.etasdk.network.EtaError;
import com.eTilbudsavis.etasdk.network.Request;
import com.eTilbudsavis.etasdk.network.Response.Listener;
import com.eTilbudsavis.etasdk.network.impl.JsonArrayRequest;
import com.eTilbudsavis.etasdk.request.ParameterBuilder;
import com.eTilbudsavis.etasdk.request.RequestAutoFill;
import com.eTilbudsavis.etasdk.request.RequestAutoFill.AutoFillParams;
import com.eTilbudsavis.etasdk.utils.Api;

public abstract class ListRequest<T> extends JsonArrayRequest {
	
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
		super.setDelivery(new DeliveryHelper<T>(this, listener));
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
		getAutoFill().prepare(new AutoFillParams(this), response, error, new Listener<T>() {
			
			public void onComplete(T response, EtaError error) {
				((DeliveryHelper<T>)getDelivery()).deliver(response, error);
			}
		});
		getAutoFill().execute(getRequestQueue());
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
	
	@Override
	public Request<?> setDelivery(Delivery delivery) {
		String msg = "ListRequest does not support setting Delivery. All requests are returned to UI Thread";
		throw new UnsupportedOperationException(msg);
	}
	
	public static abstract class Builder<T> extends com.eTilbudsavis.etasdk.request.Builder<ListRequest<T>> {
		
		private RequestAutoFill<T> mAutofill;
		private Delivery mDelivery;
		
		public ListRequest<T> build() {
			ListRequest<T> r = super.build();
			r.setAutoFill(mAutofill);
			return r;
		}
		
		public Builder(ListRequest<T> r) {
			super(r);
		}
		
		protected RequestAutoFill<T> getAutofill() {
			return mAutofill;
		}
		
		protected void setAutoFiller(RequestAutoFill<T> filler) {
			mAutofill = filler;
		}
		
		public void setDelivery(Delivery d) {
			mDelivery = d;
		}

		public Delivery getDelivery() {
			return mDelivery;
		}
		
	}
	
	public static class ListParameterBuilder extends ParameterBuilder {
		
		private String mOrderDefault = null;
		private LinkedHashSet<String> mOrder = new LinkedHashSet<String>();
		Map<String, Set<String>> mFilters = new HashMap<String, Set<String>>();
		
		protected void setDefaultOrder(String defaultOrder) {
			mOrderDefault = defaultOrder;
		}
		
		protected boolean setOrder(LinkedHashSet<String> orders) {
			return mOrder.addAll(orders);
		}
		
		/**
		 * Adds the order to the set. Use with caution, as this method does
		 * not remove negated values in the set.
		 * @param order the order to add
		 * @return true when this {@link RequestOrder} did not already contain the order, false otherwise
		 */
		protected boolean addOrder(String order) {
			return mOrder.add(order);
		}
		
		/**
		 * Removed the given order (checks for both ascending, and descending order).
		 * @param order the order to remove
		 * @return true if the order was removed, otherwise false
		 */
		protected boolean removeOrder(String order) {
			// removing the negated value of the given order
			if (order.startsWith("-")) {
				mOrder.remove(order.replaceFirst("-", ""));
			} else {
				mOrder.remove("-" + order);
			}
			return mOrder.remove(order);
		}
		
		/**
		 * Adds the given order to the set of orders, removing any order
		 * currently in the set, that may be in conflict with the new order.
		 * 
		 * @param order the string to add
		 * @param descending true if the string "-" should be prepended, indicating descending order. else false
		 * @return true when this {@link RequestOrder} did not already contain the order, false otherwise
		 */
		protected boolean addOrder(String order, boolean descending) {
			String tmp = (descending ? "-" : "") + order;
			// Performing cleanup, ensuring only ONE of each 'order' is added to the set
			if (mOrder.contains(order)) {
				mOrder.remove(order);
			}
			return addOrder(tmp);
		}
		
		/*
		 * The filters
		 */
		protected boolean addFilter(String filter, String id) {
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
		
		protected boolean addFilter(String filter, Set<String> ids) {
			Set<String> set = mFilters.get(filter);
			if (set != null) {
				set.addAll(ids);
			} else {
				mFilters.put(filter, ids);
			}
			return true;
		}

		protected boolean removeFilter(String filter, String id) {
			if (mFilters.get(filter) != null) {
				return mFilters.get(filter).remove(id);
			}
			return false;
		}
		
		protected boolean removeFilter(String filter) {
			mFilters.remove(filter);
			return true;
		}
		
		protected Map<String, Set<String>> getFilters() {
			return mFilters;
		}
		
		/**
		 * The API relies on pagination for retrieving data. Therefore you need to
		 * define the offset to the first item in the requested list, when querying for data.
		 * If no offset is set it will default to 0.
		 * @param offset to first item in list
		 * @return this object
		 */
		public ListParameterBuilder setOffset(int offset) {
			put(Api.Param.OFFSET, String.valueOf(offset));
			return this;
		}
		
		/**
		 * Get the offset parameter used for the query.
		 * @return offset
		 */
		public int getOffset() {
			String offset = getParameters().get(Api.Param.OFFSET);
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
		public ListParameterBuilder setLimit(int limit) {
			put(Api.Param.LIMIT, String.valueOf(limit));
			return this;
		}
		
		/**
		 * Get the upper limit on how many items the API should return.
		 * @return max number of items API should return
		 */
		public int getLimit() {
			String offset = getParameters().get(Api.Param.LIMIT);
			if (offset != null) {
				return Integer.valueOf(offset);
			}
			return 0;
		}
		
		@Override
		public Map<String, String> getParameters() {
			
			Map<String, String> map = super.getParameters();
			
			if (!mOrder.isEmpty()) {
				map.put(Api.Param.ORDER_BY, TextUtils.join(Api.DELIMITER, mOrder));
			} else if (mOrderDefault != null) {
				map.put(Api.Param.ORDER_BY, mOrderDefault);
			}
			
			for (String key : mFilters.keySet()) {
				map.put(key, TextUtils.join(Api.DELIMITER, mFilters.get(key)));
			}
			
			return map;
		}
		
	}
	
	public static abstract class ListAutoFill<T extends List<?>> extends RequestAutoFill<T> {
		
	}
	
}
