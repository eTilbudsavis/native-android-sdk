package com.eTilbudsavis.etasdk.request.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;

import com.eTilbudsavis.etasdk.EtaObjects.Dealer;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.Api;
import com.eTilbudsavis.etasdk.Utils.Api.Endpoint;

public class DealerListRequest extends ListRequest<List<Dealer>> {
	
	private DealerListRequest(Listener<List<Dealer>> l) {
		super(Endpoint.DEALER_LIST, l);
	}
	
	@Override
	public void deliverResponse(JSONArray response, EtaError error) {
		List<Dealer> dealers = null;
		if (response != null) {
			dealers = Dealer.fromJSON(response);
		}
		runAutoFill(dealers, error);
	}
	
	public static class Builder extends ListRequest.Builder<List<Dealer>>{
		
		public Builder(Listener<List<Dealer>> l) {
			super(new DealerListRequest(l));
		}
		
		public void setAutoFill(DealerAutoFill filler) {
			super.setAutoFiller(filler);
		}
		
		@Override
		public ListRequest<List<Dealer>> build() {
			
			if (getParameters() == null) {
				setParameters(new Parameter());
			}
			
			if (getAutofill() == null) {
				setAutoFiller(new DealerAutoFill());
			}
			
			return super.build();
		}
		
	}
	
	public static class Parameter extends ListRequest.ListParameterBuilder {
		
		public void addDealerFilter(Set<String> dealerIds) {
			addFilter(Api.Param.DEALER_IDS, dealerIds);
		}
		
		public void addDealerFilter(String dealerId) {
			addFilter(Api.Param.DEALER_IDS, dealerId);
		}
		
	}
	
	public static class DealerAutoFill extends ListRequest.ListAutoFill<List<Dealer>> {
		
		@Override
		public List<Request<?>> createRequests(List<Dealer> data) {
			
			List<Request<?>> reqs = new ArrayList<Request<?>>();
			
			return reqs;
		}
		
	}

}
