package com.eTilbudsavis.etasdk.request.impl;

import java.util.Set;

public class CatalogFilter extends ListFilter {

	public void setCatalogIds(Set<String> ids) {
		set(CATALOG_IDS, ids);
	}

	public void setDealerIds(Set<String> ids) {
		set(DEALER_IDS, ids);
	}

	public void setStoreIds(Set<String> ids) {
		set(STORE_IDS, ids);
	}

	public void setCatalogIds(String id) {
		set(CATALOG_IDS, id);
	}

	public void setDealerIds(String id) {
		set(DEALER_IDS, id);
	}

	public void setStoreIds(String id) {
		set(STORE_IDS, id);
	}
	
}
