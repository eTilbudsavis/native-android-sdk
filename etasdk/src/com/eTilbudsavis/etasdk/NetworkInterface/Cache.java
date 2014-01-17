package com.eTilbudsavis.etasdk.NetworkInterface;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;

import com.eTilbudsavis.etasdk.EtaObjects.EtaObject;
import com.eTilbudsavis.etasdk.NetworkInterface.Request.Method;
import com.eTilbudsavis.etasdk.Utils.EtaLog;
import com.eTilbudsavis.etasdk.Utils.Header;
import com.eTilbudsavis.etasdk.Utils.Param;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class Cache implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "EtaCache";

	private static final long ITEM_CACHE_TIME = 15 * Utils.MINUTE_IN_MILLIS;
	
	private static final long LIST_CACHE_TIME = 3 * Utils.MINUTE_IN_MILLIS;
	
	// Define catchable types
	Map<String, String>	types = new HashMap<String, String>();
	
	private Set<String> ernTypes = Collections.synchronizedSet(new HashSet<String>());

	private Map<String, Item> mItems = Collections.synchronizedMap(new HashMap<String, Cache.Item>());

	private Map<String, Item> mLists = Collections.synchronizedMap(new HashMap<String, Cache.Item>());
	
	/*
	 * Lister:
	 * - Alle individuellt objekter kan caches, på nær kataloger, da det IKKE er det fulde objekt
	 * 
	 * -- Uspecificerede lister, med server bestemt orden (søgning, liste af kataloger e.t.c.)
	 *    - Den fulde listen, baseret på url og parametre, skal caches as is, da orden på objekter har relevans
	 * 
	 * -- specificerede lister (App'en bestiller en liste af specifikke id's)
	 *    - Den fulde listen skal ikke caches, da orden ikke er relevant og vi let kan stykke et svar sammen
	 * 
	 * 
	 * Objekter:
	 * - Vi cacher alt med et ern key
	 * 
	 * 
	 */
	
	public Cache() {
		ernTypes.add("catalogs");
		ernTypes.add("offers");
		ernTypes.add("dealers");
		ernTypes.add("stores");
		ernTypes.add("shoppinglists");
		ernTypes.add("items");
		
		types.put("catalogs", Request.Param.FILTER_CATALOG_IDS);
		types.put("offers", Request.Param.FILTER_OFFER_IDS);
		types.put("dealers", Request.Param.FILTER_DEALER_IDS);
		types.put("stores", Request.Param.FILTER_STORE_IDS);
	}
	
	private Cache.Item getItem(String key) {
		Item i = mItems.get(key);
		if (i == null) {
			return null;
		} else if (i.isExpired()) {
			mItems.remove(key);
			return null;
		}
		return i;
	}
	
	private Cache.Item getList(String key) {
		Item i = mLists.get(key);
		if (i == null) {
			return null;
		} else if (i.isExpired()) {
			mLists.remove(key);
			return null;
		}
		return i;
	}
	
	public void clear() {
		mItems = new HashMap<String, Cache.Item>();
	}
	
	@SuppressLint("DefaultLocale")
	public void put(Request<?> request, Response<?> response) {
		
		// No faulty data, and only GET requests
		if ( !response.isSuccess() || !(request.getMethod() == Method.GET) ) {
			return;
		}
		
		String ct = request.getBodyContentType().toLowerCase();

		if (ct.contains("application/json")) {

			// It's JSON, we should be able to get the objects
			if (response.result instanceof JSONObject) {

				// The simple case, just add it to cache
				putJsonObject((JSONObject)response.result);

			} else if (response.result instanceof JSONArray) {
				
				putJsonArray(request, (JSONArray)response.result);
				
			} else if (response.result instanceof String) {
				
				String json = (String)response.result;
				
				try {
					if (json.startsWith("{") && json.endsWith("}")) {
						putJsonObject(new JSONObject(json));
					} else if (json.startsWith("[") && json.endsWith("]")) {
						putJsonArray(request, new JSONArray(json));
					}
					
				} catch (JSONException e) {
					EtaLog.d(TAG, e);
				}
			}

		} else {
			EtaLog.d(TAG, "Request doesn't seem to be of any known cache-type object");
		}

	}
	
	private void putJsonArray(Request<?> request, JSONArray a) {
		
		LinkedList<String> ernlist = new LinkedList<String>();
		try {
			
			for (int i = 0; i < a.length() ; i++) {
				Object o = a.get(i);
				if (o instanceof JSONObject) {
					String ern = putJsonObject((JSONObject)o);
					if (ern != null) {
						ernlist.add(ern);
					}
				}
				
			}
			
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		
		if (ernlist.isEmpty()) {
			return;
		}
		
		mLists.put(Utils.buildQueryString(request), new Cache.Item(ernlist, LIST_CACHE_TIME));
		
	}
	
	private String putJsonObject(JSONObject o) {
		
		try {
			if (o.has(EtaObject.ServerKey.ERN)) {
				String ern = o.getString(EtaObject.ServerKey.ERN);
				Cache.Item i = new Item(o, ITEM_CACHE_TIME);
				mItems.put(ern, i);
				return ern;
			}
		} catch (JSONException e) {
			EtaLog.d(TAG, e);
		}
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	public Response<?> get(Request<?> r) {
		
		String url = r.getUrl();
		Bundle apiParams = r.getQueryParameters();
		
		Set<String> keys = r.getQueryParameters().keySet();
		
		boolean hasFilter = keys.contains(Param.FILTER_CATALOG_IDS) || 
				keys.contains(Param.FILTER_DEALER_IDS) || 
				keys.contains(Param.FILTER_OFFER_IDS) || 
				keys.contains(Param.FILTER_STORE_IDS);
		
		boolean hasOrder = keys.contains(Param.ORDER_BY);
		
		Response resp = null;
		
		String[] path = url.split("/");
		
		if (types.containsKey(path[path.length-1])) {
			
			// if last element is a type, then we'll expect a list
			String type = path[path.length-1];
			
			Set<String> ids = new HashSet<String>(0);
			String filter = types.get(type);
			if (apiParams.containsKey(filter)) {
				ids = getFilter(filter, apiParams);
			}
			
			// No ids? no catchable items...
			if (ids.size() == 0)
				return resp;
			
			// Get all possible items requested from cache
			JSONArray jArray = new JSONArray();
			for (String id : ids) {
				String ern = buildErn(type, id);
				Item c = getItem(ern);
				if (c != null) {
					jArray.put((JSONObject)c.object);
				}
			}
			
			// If cache had ALL items, then return the list.
			int size = jArray.length();
			if (size == ids.size()) {
				
//				resp.set(200, jArray.toString());
			}
			
		} else if (types.containsKey(path[path.length-2])) {
			// if second to last element is a valid type, then we'll expect an item id
			// (this isn't always true, but if it isn't then the item-id, shouldn't be in cache )
			
			String id = path[path.length-1];
			String type = path[path.length-2];
			
			Item c = getItem(buildErn(type, id));
			if (c != null) {
				return Response.fromSuccess(c.object, null, true);
			}
			
		}
		
		return resp;
		
	}

	private Set<String> getFilter(String filterName, Bundle apiParams) {
		String tmp = apiParams.getString(filterName);
		Set<String> list = new HashSet<String>();
		Collections.addAll(list, TextUtils.split(tmp, ","));
		return list;
	}
	
	private String buildErn(String type, String id) {
		if (ernTypes.contains(type)) {
			type = type.substring(0, type.length()-1);
			return String.format("ern:%s:%s", type, id);
		}
		return type;
	}
	
	public static class Item {
		
		// Time of insertion
		public final long expires;
		public final Object object;
		
		public Item(Object o, long ttl) {
			this.expires = System.currentTimeMillis() + ttl;
			this.object = o;
		}
		
		/**
		 * Returns true if the Item is still valid.
		 * 
		 * this is based on the time to live factor
		 * @return
		 */
		public boolean isExpired() {
			return expires < System.currentTimeMillis();
		}
		
	}
	
}
