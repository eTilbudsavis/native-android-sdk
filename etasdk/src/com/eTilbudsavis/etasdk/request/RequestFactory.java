package com.eTilbudsavis.etasdk.request;

import java.util.List;

import org.json.JSONArray;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Network.Impl.JsonArrayRequest;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.request.impl.CatalogFiller;
import com.eTilbudsavis.etasdk.request.impl.CatalogFilter;

public class RequestFactory {
	
	public Request getCatalogList(Listener<Catalog> l, CatalogFilter filter, CatalogFiller filler) {
		
		JsonArrayRequest r = new JsonArrayRequest(Endpoint.CATALOG_LIST, new Listener<JSONArray>() {

			public void onComplete(JSONArray response, EtaError error) {
				
				if (response != null) {
					List<Catalog> items = Catalog.fromJSON(response);
					
				}
				
			}
			
		});
		r.putParameters(filter.getParameter());
		return r;
	}
	
}
