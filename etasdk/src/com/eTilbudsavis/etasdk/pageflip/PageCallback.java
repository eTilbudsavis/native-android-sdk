package com.eTilbudsavis.etasdk.pageflip;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;

public interface PageCallback {
	
	public Catalog getCatalog();
	
	public boolean isLandscape();
	
	public boolean isPositionSet();
	
	public boolean isLowMemory();
	
	public PageflipListener getWrapperListener();
	
	public String getViewSession();
	
	public void onReady(int position);
	
}