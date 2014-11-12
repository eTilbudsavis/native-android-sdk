package com.eTilbudsavis.etasdk.pageflip;

import java.util.List;

import android.view.View;

import com.eTilbudsavis.etasdk.EtaObjects.helper.Hotspot;
import com.eTilbudsavis.etasdk.Network.EtaError;

public interface PageflipListener {
	
	public void onReady();
	
	public void onPageChange(int[] pages);
	
	public void onOutOfBounds(boolean left);

	public void onDragStateChanged(int state);
	
	public void onError(EtaError error);
	
	public void onSingleClick(View v, int page, float x, float y, List<Hotspot> hotspots);
	
	public void onDoubleClick(View v, int page, float x, float y, List<Hotspot> hotspots);

	public void onLongClick(View v, int page, float x, float y, List<Hotspot> hotspots);

	public void onZoom(View v, int[] pages, boolean zoonIn);
	
}
