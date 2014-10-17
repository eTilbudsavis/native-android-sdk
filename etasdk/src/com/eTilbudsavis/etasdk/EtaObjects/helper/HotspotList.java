package com.eTilbudsavis.etasdk.EtaObjects.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.Interface.EtaObject;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Api;
import com.eTilbudsavis.etasdk.Utils.Api.JsonKey;
import com.eTilbudsavis.etasdk.Utils.Json;

public class HotspotList implements EtaObject<JSONArray>, Serializable {
	
	private static final long serialVersionUID = -4654824845675092954L;

	public static final String TAG = HotspotList.class.getSimpleName();
	
	private static final String TYPE_OFFER = "offer";
	
	Map<Integer, List<Hotspot>> mHotspots = new HashMap<Integer, List<Hotspot>>();
	
	public static HotspotList fromJSON(Dimension d, JSONArray jHotspots) {
		
		HotspotList list = new HotspotList();
		if (jHotspots==null) {
			return list;
		}
		
		for (int i = 0 ; i < jHotspots.length() ; i++ ) {
			
			try {
				
				JSONObject jHotspot = jHotspots.getJSONObject(i);
				
				String type = Json.valueOf(jHotspot, JsonKey.TYPE, null);
				// We all know that someone is going to introduce a new type at some point, so might as well check now
				if (TYPE_OFFER.equals(type)) {
					
					JSONObject offer = jHotspot.getJSONObject(Api.JsonKey.OFFER);
					Offer o = Offer.fromJSON(offer);
					
					JSONObject rectangleList = jHotspot.getJSONObject(Api.JsonKey.LOCATIONS);
					
					Iterator<String> keys = rectangleList.keys();
					while (keys.hasNext()) {
						
						String key = (String) keys.next();
						Integer page = Integer.valueOf(key);
						// Hotspot page are offset by one! (crappy real world numbers)
						page = page - 1;
						JSONArray rect = rectangleList.getJSONArray(key);
						
						if (!list.mHotspots.containsKey(page)) {
							list.mHotspots.put(page, new ArrayList<Hotspot>());
						}
						
						Hotspot h = Hotspot.fromJSON(rect);
						h.normalize(d);
						h.setPage(page);
						h.setOffer(o);
						list.mHotspots.get(page).add(h);
						
					}
				}
				
			} catch (JSONException e) {
				EtaLog.e(TAG, e.getMessage(), e);
			}
		}
		
		return list;
	}
	
	public Set<Hotspot> getHotspots(int page, double xPercent, double yPercent) {
		Set<Hotspot> list = new HashSet<Hotspot>();
		List<Hotspot> lh = mHotspots.get(page);
		if (lh == null) {
			return list;
		}
		for (Hotspot h : lh) {
			if (h.contains(xPercent, yPercent)) {
				list.add(h);
			}
		}
		return list;
	}
	
	public JSONArray toJSON() {
		return null;
	}

	public Map<Integer, List<Hotspot>> getHotspots() {
		return mHotspots;
	}
	
}
