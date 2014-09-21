package com.eTilbudsavis.etasdk.request.impl;

import org.json.JSONArray;

import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Network.Impl.JsonArrayRequest;
import com.eTilbudsavis.etasdk.Utils.Param;
import com.eTilbudsavis.etasdk.request.IRequestParameter;
import com.eTilbudsavis.etasdk.request.RequestFilter;
import com.eTilbudsavis.etasdk.request.RequestOrder;
import com.eTilbudsavis.etasdk.request.RequestParameter;

public class ListRequest<T> extends JsonArrayRequest {

	private static final String ERROR_NO_REQUESTQUEUE = 
			"Request must initially be added to RequestQueue, subsequent pagination requests can be performed with next() method";

	private IRequestParameter mFilters;
	private IRequestParameter mOrder;
	private IRequestParameter mParam;
	
	public ListRequest(String url, Listener<JSONArray> listener) {
		super(url, listener);
	}
	
	public ListRequest(Method method, String url, Listener<JSONArray> listener) {
		super(method, url, listener);
	}
	
	public ListRequest(Method method, String url, JSONArray requestBody, Listener<JSONArray> listener) {
		super(method, url, requestBody, listener);
	}
	
	public void setFilter(RequestFilter<?> filter) {
		mFilters = filter;
	}
	
	public void setOrder(RequestOrder order) {
		mOrder = order;
	}
	
	public void setParameters(RequestParameter params) {
		mParam = params;
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
	
}
