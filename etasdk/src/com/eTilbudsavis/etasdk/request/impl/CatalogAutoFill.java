package com.eTilbudsavis.etasdk.request.impl;

import com.eTilbudsavis.etasdk.EtaObjects.Dealer;
import com.eTilbudsavis.etasdk.EtaObjects.Pages;
import com.eTilbudsavis.etasdk.EtaObjects.Store;
import com.eTilbudsavis.etasdk.request.RequestAutoFill;

public class CatalogAutoFill extends RequestAutoFill {
	
	private boolean mPages;
	private boolean mDealer;
	private boolean mStore;
	
	public void fillPages(boolean getPages) {
		mPages = getPages;
	}

	public void fillDealer(boolean getDealer) {
		mDealer = getDealer;
	}

	public void fillStore(boolean getStore) {
		mStore = getStore;
	}
	
}
