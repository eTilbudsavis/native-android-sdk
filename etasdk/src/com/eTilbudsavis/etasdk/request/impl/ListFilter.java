package com.eTilbudsavis.etasdk.request.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Utils.Param;
import com.eTilbudsavis.etasdk.request.RequestFilter;

public class ListFilter extends RequestFilter<Set<String>> {
	
	public static final String CATALOG_IDS = "catalog_ids";
	public static final String STORE_IDS = "store_ids";
	public static final String AREA_IDS = "area_ids";
	public static final String OFFER_IDS = "offer_ids";
	public static final String DEALER_IDS = "dealer_ids";

	protected boolean set(String filter, String id) {
		Map<String, Set<String>> map = getFilter();
		if (map.containsKey(filter) && map.get(filter) != null) {
			map.get(filter).add(id);
		} else {
			Set<String> ids = new HashSet<String>();
			ids.add(id);
			map.put(filter, ids);
		}
		return true;
	}
	
}
