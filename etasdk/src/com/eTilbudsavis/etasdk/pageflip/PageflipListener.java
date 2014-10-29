package com.eTilbudsavis.etasdk.pageflip;

import java.util.Set;

import android.view.View;

import com.eTilbudsavis.etasdk.EtaObjects.helper.Hotspot;
import com.eTilbudsavis.etasdk.Network.EtaError;

public interface PageflipListener {
	
	public void onPageChange(int[] pages);
	
	public void onOutOfBounds(boolean left);
	
	public void onHotspotClick(Set<Hotspot> hotspots);
	
	public void onClick(View v, int page);
	
	public void onDoubleClick(View v, int page);
	
	public void onDragStateChanged(int state);
	
	public void onZoom(int[] pages, boolean zoonIn);
	
	public void onError(EtaError error);
	
}
