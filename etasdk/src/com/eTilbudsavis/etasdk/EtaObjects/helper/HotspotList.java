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
	
	public static HotspotList fromJSON(JSONArray jHotspots) {
		
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
					
					JSONObject points = jHotspot.getJSONObject(Api.JsonKey.LOCATIONS);
					
					Iterator<String> keys = points.keys();
					while (keys.hasNext()) {
						
						String key = (String) keys.next();
						
						// TODO: If it throws an exception?
						Integer p = Integer.valueOf(key);
						// Hotspots are offset by one!
						p = p-1;
						if (!list.mHotspots.containsKey(p)) {
							list.mHotspots.put(p, new ArrayList<Hotspot>());
						}
						
						JSONArray jCoords = points.getJSONArray(key);
						Hotspot h = Hotspot.fromJSON(jCoords);
						h.setOffer(o);
						list.mHotspots.get(p).add(h);
						
					}
				}
				
			} catch (JSONException e) {
				EtaLog.e(TAG, e.getMessage(), e);
			}
		}
		
		return list;
	}
	
	public Set<Offer> getOfferFromHotspot(int page, Dimension dimen, double x, double y) {
		Set<Offer> list = new HashSet<Offer>();
		double absX = dimen.getHeight()*x;
		double absY = dimen.getWidth()*y;
		List<Hotspot> lh = mHotspots.get(Integer.valueOf(page));
		if (lh != null && !lh.isEmpty()) {
			for (Hotspot h : lh) {
				if (h.contains(absX, absY)) {
					list.add(h.getOffer());
				}
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
