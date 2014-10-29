package com.eTilbudsavis.etasdk.pageflip;

public interface StatsCollect {
	
	public void collectView(boolean landscape, int[] pages);
	
	public void collectZoom(boolean zoomIn, boolean landscape, int[] pages);
	
}
