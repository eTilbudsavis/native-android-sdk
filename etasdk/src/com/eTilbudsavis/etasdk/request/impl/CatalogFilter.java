package com.eTilbudsavis.etasdk.request.impl;

import java.util.Set;

public class CatalogFilter extends ListFilter {
	
	public void addCatalogFilter(Set<String> catalogIds) {
		add(CATALOG_IDS, catalogIds);
	}
	
	public void addDealerFilter(Set<String> dealerIds) {
		add(DEALER_IDS, dealerIds);
	}
	
	public void addStoreFilter(Set<String> storeIds) {
		add(STORE_IDS, storeIds);
	}
	
	public void addCatalogFilter(String catalogId) {
		add(CATALOG_IDS, catalogId);
	}
	
	public void addDealerFilter(String dealerId) {
		add(DEALER_IDS, dealerId);
	}
	
	public void addStoreFilter(String storeId) {
		add(STORE_IDS, storeId);
	}
	
}
