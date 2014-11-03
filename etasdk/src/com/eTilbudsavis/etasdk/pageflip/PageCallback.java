package com.eTilbudsavis.etasdk.pageflip;

import android.view.View;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;

public interface PageCallback {
	
	public Catalog getCatalog();
	
	public boolean isLandscape();
	
	public void onZoom(View v, boolean start);
	
	public boolean isPositionSet();
	
	public boolean isLowMemory();
	
	public PageflipListener getWrapperListener();
	
}