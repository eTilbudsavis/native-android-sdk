package com.eTilbudsavis.etasdk.request.impl;

import com.eTilbudsavis.etasdk.request.RequestOrder;

public class CatalogOrder extends RequestOrder {
	
	public CatalogOrder() {
		setDefault("-" + POPULARITY);
	}
	
	private boolean setOrder(boolean orderBy, boolean descending, String order) {
		if (orderBy) {
			return set(order);
		} else {
			return remove(order);
		}
	}
	
	public void byPopularity(boolean enableOrder, boolean descending) {
		setOrder(enableOrder, descending, POPULARITY);
	}
	
	/*
popularity
dealer
created
expiration_date
publication_date
distance
	 */
	
}
