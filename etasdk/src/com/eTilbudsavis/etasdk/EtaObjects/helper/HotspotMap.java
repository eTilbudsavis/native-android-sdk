package com.eTilbudsavis.etasdk.EtaObjects.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;

import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.Interface.EtaObject;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Api;
import com.eTilbudsavis.etasdk.Utils.Api.JsonKey;
import com.eTilbudsavis.etasdk.Utils.Json;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class HotspotMap extends HashMap<Integer, List<Hotspot>> implements EtaObject<JSONArray>, Serializable {
	
	private static final long serialVersionUID = -4654824845675092954L;

	public static final String TAG = HotspotMap.class.getSimpleName();
	
	private static final String TYPE_OFFER = "offer";
	
	private boolean mIsNormalized = false;
	
	private static final int[] mRectColors = { 
		Color.BLACK, 
		Color.BLUE, 
		Color.GREEN, 
		Color.RED, 
		Color.YELLOW, 
		Color.MAGENTA 
	};
	
	public static HotspotMap fromJSON(Dimension d, JSONArray jHotspots) {
		
		HotspotMap map = new HotspotMap();
		if (jHotspots==null) {
			return map;
		}
		
		for (int i = 0 ; i < jHotspots.length() ; i++ ) {
			
			try {
				
				JSONObject jHotspot = jHotspots.getJSONObject(i);
				
				String type = Json.valueOf(jHotspot, JsonKey.TYPE, null);
				// We all know that someone is going to introduce a new type at some point, so might as well check now
				if (TYPE_OFFER.equals(type)) {
					
					JSONObject offer = jHotspot.getJSONObject(Api.JsonKey.OFFER);
					Offer o = Offer.fromJSON(offer);
					
					int color = mRectColors[i%mRectColors.length];
					
					JSONObject rectangleList = jHotspot.getJSONObject(Api.JsonKey.LOCATIONS);
					
					List<String> keys = Utils.copyIterator(rectangleList.keys());
					
					for (String key : keys) {
						
						Integer page = Integer.valueOf(key);
						JSONArray rect = rectangleList.getJSONArray(key);
						
						if (!map.containsKey(page)) {
							map.put(page, new ArrayList<Hotspot>());
						}
						
						Hotspot h = Hotspot.fromJSON(rect);
						
						h.setPage(page);
						h.setOffer(o);
						h.setColor(color);
						h.setDualPage(keys.size()>1);
						map.get(page).add(h);
						
					}
				}
				
			} catch (JSONException e) {
				EtaLog.e(TAG, e.getMessage(), e);
			}
		}
		
		map.normalize(d);
		
		return map;
	}
	
	public boolean isNormalized() {
		return mIsNormalized;
	}
	
	public void normalize(Dimension d) {
		
		if (!d.isSet()) {
			return;
		}
		
		Set<Integer> keys = keySet();
		if (keys==null||keys.isEmpty()) {
			return;
		}
		
		for (Integer i : keys) {
			
			List<Hotspot> hotspots = get(i);
			if (hotspots!=null && !hotspots.isEmpty()) {
				for (Hotspot h : hotspots) {
					h.normalize(d);
				}
			}
			
		}
		
		mIsNormalized = true;
	}
	
	public List<Hotspot> getHotspots(int page, double xPercent, double yPercent, boolean landscape) {
		return getHotspots(page, xPercent, yPercent, Hotspot.SIGNIFICANT_AREA, landscape);
	}
	
	public List<Hotspot> getHotspots(int page, double xPercent, double yPercent, double minArea, boolean landscape) {
		List<Hotspot> list = new ArrayList<Hotspot>();
		List<Hotspot> lh = get(page);
		if (lh == null) {
			return list;
		}
		for (Hotspot h : lh) {
			if (h.inBounds(xPercent, yPercent, minArea, landscape)) {
				list.add(h);
			}
		}
		return list;
	}
	
	public JSONArray toJSON() {
		return null;
	}
	
}
