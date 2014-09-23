package com.eTilbudsavis.etasdk.request;

import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.request.impl.ListRequest;

public abstract class ListRequestBuilder<T> {
	
	private ListRequest<T> mRequest;
	private Listener<T> mListener;
	private RequestAutoFill<T> mAutofill;
	private IRequestParameter mFilters;
	private IRequestParameter mOrder;
	private IRequestParameter mParam;
	
	public ListRequest<T> build() {
		mRequest.putParameters(mFilters.getParameter());
		mRequest.putParameters(mOrder.getParameter());
		mRequest.putParameters(mParam.getParameter());
		mRequest.setAutoFill(mAutofill);
		return mRequest;
	}

	public ListRequestBuilder(Listener<T> l) {
		mListener = l;
	}

	public ListRequestBuilder(Listener<T> l, ListRequest<T> r) {
		mRequest = r;
		mListener = l;
	}
	
	protected void setRequest(ListRequest<T> request) {
		mRequest = request;
	}
	
	protected ListRequest<T> getRequest() {
		return mRequest;
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
	
	protected RequestAutoFill<T> getAutofill() {
		return mAutofill;
	}
	
	protected void setAutoFiller(RequestAutoFill<T> filler) {
		mAutofill = filler;
	}
	
}
