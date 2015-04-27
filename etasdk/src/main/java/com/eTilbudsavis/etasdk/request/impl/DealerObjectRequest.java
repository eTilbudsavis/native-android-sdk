package com.eTilbudsavis.etasdk.request.impl;

import java.util.ArrayList;
import java.util.List;

import com.eTilbudsavis.etasdk.model.Dealer;
import com.eTilbudsavis.etasdk.network.Request;
import com.eTilbudsavis.etasdk.network.Response.Listener;
import com.eTilbudsavis.etasdk.request.RequestAutoFill;
import com.eTilbudsavis.etasdk.utils.Api;

public class DealerObjectRequest extends ObjectRequest<Dealer> {
	
	private DealerObjectRequest(String url, Listener<Dealer> l) {
		super(url, l);
	}
	
	public static abstract class Builder extends ObjectRequest.Builder<Dealer> {
		
		public Builder(String dealerId, Listener<Dealer> l) {
			super(new DealerObjectRequest(Api.Endpoint.dealerId(dealerId), l));
		}
		
		public ObjectRequest<Dealer> build() {
			ObjectRequest<Dealer> r = super.build();
			if (getAutofill()==null) {
				setAutoFiller(new DealerAutoFill());
			}
			return r;
		}

		public void setAutoFill(DealerAutoFill filler) {
			super.setAutoFiller(filler);
		}
		
	}
	
	public static class DealerAutoFill extends RequestAutoFill<Dealer> {
		
		@Override
		public List<Request<?>> createRequests(Dealer data) {
			
			List<Request<?>> reqs = new ArrayList<Request<?>>();
			
			return reqs;
		}
		
	}
	
}
