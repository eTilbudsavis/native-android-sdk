package com.eTilbudsavis.etasdk.request.impl;

import com.eTilbudsavis.etasdk.request.RequestOrder;

public class CatalogOrder extends RequestOrder {
	
	public CatalogOrder() {
		setDefault("-" + POPULARITY);
	}
	
	public void byPopularity(boolean enable, boolean descending) {
		setOrder(enable, descending, POPULARITY);
	}
	
	public void byDealer(boolean enable, boolean descending) {
		setOrder(enable, descending, DEALER);
	}
	
	public void byCreated(boolean enable, boolean descending) {
		setOrder(enable, descending, CREATED);
	}

	public void byExpirationDate(boolean enable, boolean descending) {
		setOrder(enable, descending, EXPIRATION_DATE);
	}

	public void byPublicationDate(boolean enable, boolean descending) {
		setOrder(enable, descending, PUBLICATION_DATE);
	}

	public void byDistance(boolean enable, boolean descending) {
		setOrder(enable, descending, DISTANCE);
	}
	
}
