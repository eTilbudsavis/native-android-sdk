package com.eTilbudsavis.etasdk.request.impl;

import com.eTilbudsavis.etasdk.request.RequestOrder;

public class CatalogOrder extends RequestOrder {
	
	public CatalogOrder() {
		setDefault("-" + POPULARITY);
	}
	
	public void byPopularity(boolean enableOrder, boolean descending) {
		setOrder(enableOrder, descending, POPULARITY);
	}

	public void byDealer(boolean enableOrder, boolean descending) {
		setOrder(enableOrder, descending, DEALER);
	}

	public void byCreated(boolean enableOrder, boolean descending) {
		setOrder(enableOrder, descending, CREATED);
	}

	public void byExpirationDate(boolean enableOrder, boolean descending) {
		setOrder(enableOrder, descending, EXPIRATION_DATE);
	}

	public void byPublicationDate(boolean enableOrder, boolean descending) {
		setOrder(enableOrder, descending, PUBLICATION_DATE);
	}

	public void byDistance(boolean enableOrder, boolean descending) {
		setOrder(enableOrder, descending, DISTANCE);
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
