package com.eTilbudsavis.etasdk.request;

import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.request.impl.ListRequest;

public abstract class ListRequestBuilder<T> {
	
	ListRequest mRequest;
	private Listener<T> mListener;
	private IRequestParameter mFilters;
	private IRequestParameter mOrder;
	private IRequestParameter mParam;
	private RequestAutoFill mAutofill;
	
	public ListRequest build() {
		mRequest.putParameters(mFilters.getParameter());
		mRequest.putParameters(mOrder.getParameter());
		mRequest.putParameters(mParam.getParameter());
		
		return mRequest;
	}
	
	public ListRequestBuilder(ListRequest<?> request) {
		mRequest = request;
	}
	
	protected IRequestParameter getFilter() {
		return mFilters;
	}
	
	protected void setFilter(RequestFilter<?> filter) {
		mFilters = filter;
	}
	
	protected IRequestParameter getOrder() {
		return mOrder;
	}
	
	protected void setOrder(RequestOrder order) {
		mOrder = order;
	}
	
	protected IRequestParameter getParameters() {
		return mParam;
	}
	
	protected void setParameters(RequestParameter params) {
		mParam = params;
	}
	
	protected RequestAutoFill getAutofill() {
		return mAutofill;
	}
	
	protected void setAutoFiller(RequestAutoFill filler) {
		mAutofill = filler;
	}
	
}
