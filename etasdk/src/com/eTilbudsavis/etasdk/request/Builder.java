package com.eTilbudsavis.etasdk.request;

import com.eTilbudsavis.etasdk.Network.Request;

public abstract class Builder<T> {

	private Request<?> mRequest;
	private IRequestParameter mFilter;
	private IRequestParameter mOrder;
	private IRequestParameter mParam;
	private RequestAutoFill<T> mAutofill;
	
	public Builder(Request<?> r) {
		if (mFilter != null) {
			mRequest.putParameters(mFilter.getParameter());
		}
		if (mOrder != null) {
			mRequest.putParameters(mOrder.getParameter());
		}
		if (mParam != null) {
			mRequest.putParameters(mParam.getParameter());
		}
		mRequest = r;
	}
	
	public Request<?> build() {
		return mRequest;
	}
	
	protected IRequestParameter getFilter() {
		return mFilter;
	}
	
	protected void setFilter(RequestFilter<?> filter) {
		mFilter = filter;
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
