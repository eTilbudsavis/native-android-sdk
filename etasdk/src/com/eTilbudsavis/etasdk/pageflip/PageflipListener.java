package com.eTilbudsavis.etasdk.pageflip;

public interface PageflipListener {

	public void onPageChange(PageflipFragment f, int newPage);
	
	public void onOutOfBounds();
	
	public void onHotspotClick(String offerId);
	
	public void onSingleClick(String offerId);
	
	public void onDoubleClick(String offerId);
	
	public void onDragStartClick(String offerId);
	
}
