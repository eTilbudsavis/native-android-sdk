package com.eTilbudsavis.etasdk.pageflip;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;

public interface PageCallback {
	
	public Catalog getCatalog();
	
	public boolean isLandscape();

	public void zoomStart();
	
	public void zoomStop();
	
}