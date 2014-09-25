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
	protected void deliverResponse(JSONArray response, EtaError error) {
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
		
		public void setFilter(Filter filter) {
			super.setFilter(filter);
		}
		
		public void setOrder(Order order) {
			super.setOrder(order);
		}
		
		public void setParameters(Parameter params) {
			super.setParameters(params);
		}
		
		public void setAutoFill(DealerAutoFill filler) {
			super.setAutoFiller(filler);
		}
		
		@Override
		public ListRequest<List<Dealer>> build() {
			
			if (getFilter() == null) {
				setFilter(new Filter());
			}
			
			if (getOrder() == null) {
				setOrder(new Order());
			}
			
			if (getParameters() == null) {
				setParameters(new Parameter());
			}
			
			if (getAutofill() == null) {
				setAutoFiller(new DealerAutoFill());
			}
			
			return super.build();
		}
		
	}
	
	public static class Filter extends ListRequest.Filter {
		
		public void addDealerFilter(Set<String> dealerIds) {
			add(Api.Param.DEALER_IDS, dealerIds);
		}
		
		public void addDealerFilter(String dealerId) {
			add(Api.Param.DEALER_IDS, dealerId);
		}
		
	}
	
	public static class Order extends ListRequest.Order {
		
		public Order() {
			super(null);
		}
		
	}
	
	public static class Parameter extends ListRequest.Parameter {
		// TODO lookup API doc to find relevant filters
	}
	
	public static class DealerAutoFill extends ListRequest.ListAutoFill<List<Dealer>> {
		
		@Override
		public List<Request<?>> createRequests(List<Dealer> data) {
			
			List<Request<?>> reqs = new ArrayList<Request<?>>();
			
			return reqs;
		}
		
	}

}
