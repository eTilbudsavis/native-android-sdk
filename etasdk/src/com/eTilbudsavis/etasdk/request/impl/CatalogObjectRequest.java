package com.eTilbudsavis.etasdk.request.impl;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.Network.Response.Listener;

public class CatalogObjectRequest extends ObjectRequest<Catalog> {
	
	public CatalogObjectRequest(String url, Listener<Catalog> l) {
		super(url, l);
	}
	
	
	
}
