package com.eTilbudsavis.etasdk.request.impl;

import org.json.JSONArray;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Network.Impl.JsonArrayRequest;
import com.eTilbudsavis.etasdk.request.RequestFilter;
import com.eTilbudsavis.etasdk.request.RequestOrder;
import com.eTilbudsavis.etasdk.request.RequestParameter;

public class ListRequest extends JsonArrayRequest {
	
	private static final String ERROR_NO_REQUESTQUEUE = "The request must have been handed to RequestQueue, before the "
			+ "next() method is available";
	
	private RequestFilter<?> mFilters;
	private RequestOrder mOrder;
	private RequestParameter mParam;
	
	public ListRequest(String url, Listener<JSONArray> listener) {
		super(url, listener);
	}

	public ListRequest(Method method, String url, Listener<JSONArray> listener) {
		super(method, url, listener);
	}
	
	public ListRequest(Method method, String url, JSONArray requestBody, Listener<JSONArray> listener) {
		super(method, url, requestBody, listener);
	}
	
	public ListRequest(Method method, String url, JSONObject requestBody, Listener<JSONArray> listener) {
		super(method, url, requestBody, listener);
	}
	
	public void setFilter(ListFilter filter) {
		mFilters = filter;
	}
	
	public void setOrder(RequestOrder order) {
		mOrder = order;
	}
	
	public void updateParameters() {
		if (mFilters != null) {
			putParameters(mFilters.getParameter());
		}
		if (mOrder != null) {
			putParameters(mOrder.getParameter());
		}
		if (mParam != null) {
			putParameters(mParam.getParameter());
		}
	}
	
	public void next() {
		if (getRequestQueue() == null) {
			throw new IllegalStateException(ERROR_NO_REQUESTQUEUE);
		}
		// Making sure that the increment to offset is propagated
		updateParameters();
		// TODO: Reset state variables, or figure out how to apply cheats
		getRequestQueue().add(this);
	}
	
}
