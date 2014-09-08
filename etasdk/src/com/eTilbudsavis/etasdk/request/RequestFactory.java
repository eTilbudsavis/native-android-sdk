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

//	public static Request getCatalogList(Listener<Catalog> l) {
//		return getCatalogList(l, new CatalogParameter(), new CatalogFilter(), new CatalogOrder(), new CatalogAutoFill());
//	}

	public static Request getCatalogList(Listener<List<Catalog>> l) {
		CatalogFilter cf = new CatalogFilter();
		CatalogParameter cp = new CatalogParameter();
		CatalogOrder co = new CatalogOrder();
		CatalogAutoFill caf = new CatalogAutoFill();
		return getCatalogList(l, cp, cf, co, caf);
	}
	
	public static Request getCatalogList(final Listener<List<Catalog>> l, final CatalogParameter parameter, CatalogFilter filter, CatalogOrder order, final CatalogAutoFill filler) {

		final ListRequest r = new ListRequest(Endpoint.CATALOG_LIST, new Listener<JSONArray>() {
			
			public void onComplete(JSONArray response, EtaError error) {
				List<Catalog> items = null;
				if (response != null) {
					parameter.setOffset(response.length());
					items = Catalog.fromJSON(response);
					
					// TODO: Use the AutoFiller to get needed objects
					
				}
				l.onComplete(items, error);
			}
			
		});
		r.setFilter(filter);
		r.setOrder(order);
		r.setParameters(parameter);
		r.updateParameters();
		return r;
	}
	
}
