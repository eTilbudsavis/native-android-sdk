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
	
	private Map<String, Item> mItems = Collections.synchronizedMap(new HashMap<String, Cache.Item>());
	
	public Cache() { }
	
	public void clear() {
		mItems = new HashMap<String, Cache.Item>();
	}
	
	@SuppressLint("DefaultLocale")
	public void put(Map<String, Cache.Item> cacheItems) {
		mItems.putAll(cacheItems);
	}
	
	@SuppressWarnings("rawtypes")
	public Cache.Item get(String key) {
		
		Cache.Item c = mItems.get(key);
		if (c.isExpired()) {
			mItems.remove(key);
			return null;
		}
		return c;
		
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
