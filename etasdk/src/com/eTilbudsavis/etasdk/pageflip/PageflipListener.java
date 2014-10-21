package com.eTilbudsavis.etasdk.pageflip;

import java.util.List;

import android.view.View;

import com.eTilbudsavis.etasdk.EtaObjects.helper.Hotspot;

public interface PageflipListener {
	
	public void onPageChange(int[] pages);
	
	public void onOutOfBounds();
	
	public void onHotspotClick(List<Hotspot> hotspots);
	
	public void onClick(View v, int page);
	
	public void onDoubleClick(View v, int page);
	
	public void onDragStateChanged(int state);
	
	public void onZoom(int[] pages, boolean zoonIn);
	
	//event:eta-catalog-view-resume, {}
	//event:eta-catalog-view-pause, {}
	
	//event:eta-catalog-view-dragstart, {}
	//event:eta-catalog-view-pagechange, {"pageCount":20,"init":false,"viewSession":"b9b1d1cd-5565-404f-92b0-fbad8db77374","page":14,"pages":[14],"pageLabel":"14"}
	//event:eta-catalog-view-singletap, {"previousDeltaY":0,"previousDeltaX":0,"absDeltaY":0,"absDeltaX":0,"previousX":269,"deltaTime":76,"previousY":511,"deltaX":0,"pageX":269,"deltaY":0,"pageY":511,"previousDeltaTime":2,"startTime":1413810365463,"time":1413810365539,"startX":269,"startY":511,"previousTime":1413810365537}
	//event:eta-catalog-view-doubletap, {"previousDeltaY":0,"previousDeltaX":0,"absDeltaY":0,"absDeltaX":0,"previousX":231,"deltaTime":76,"previousY":506,"deltaX":0,"pageX":231,"deltaY":0,"pageY":506,"previousDeltaTime":3,"startTime":1413810380026,"time":1413810380102,"startX":231,"startY":506,"previousTime":1413810380099}
	//event:eta-catalog-view-outofbounds, {"page":0,"direction":"right"}
	
}
