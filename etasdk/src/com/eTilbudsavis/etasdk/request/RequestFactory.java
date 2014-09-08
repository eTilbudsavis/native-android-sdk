package com.eTilbudsavis.etasdk.request;

import java.util.List;

import org.json.JSONArray;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.Network.EtaError;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.Response.Listener;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.request.impl.CatalogAutoFill;
import com.eTilbudsavis.etasdk.request.impl.CatalogFilter;
import com.eTilbudsavis.etasdk.request.impl.CatalogOrder;
import com.eTilbudsavis.etasdk.request.impl.CatalogParameter;
import com.eTilbudsavis.etasdk.request.impl.ListRequest;

@SuppressWarnings("rawtypes")
public class RequestFactory {
	
	public static Request getCatalogList(Listener<Catalog> l) {
		return getCatalogList(l,null, null, new CatalogOrder(), new CatalogAutoFill());
	}
	
	public static Request getCatalogList(Listener<Catalog> l, final CatalogParameter parameter, CatalogFilter filter, CatalogOrder order, final CatalogAutoFill filler) {
		
		final ListRequest r = new ListRequest(Endpoint.CATALOG_LIST, new Listener<JSONArray>() {
			
			public void onComplete(JSONArray response, EtaError error) {
				
				if (response != null) {
					List<Catalog> items = Catalog.fromJSON(response);
					parameter.setOffset(items.size());
					// TODO: Use the AutoFiller to get needed objects
				}
				
			}
			
		});
		
		r.setFilter(filter);
		r.setOrder(order);
		r.updateParameters();
		
		return r;
	}
	
}
