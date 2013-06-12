package com.eTilbudsavis.etasdk;

import java.io.Serializable;
import java.util.HashMap;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.Dealer;
import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.Store;

import Utils.Utilities;

public class EtaCache implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "EtaCache";
	
	private static final long ITEM_CACHE_TIME = 5*60*1000;
	private static final long HTML_CACHE_TIME = 15*60*1000;
	
	private String mHtmlCached = "";
	private long mHtmlTime = 0L;
	
	HashMap<String, CacheItem> items = new HashMap<String, EtaCache.CacheItem>();

	public EtaCache() {
		
	}

	/**
	 * Returns HTML cache.
	 *
	 * @return Cached HTML as String
	 */
	public String getHtmlCached() {
		return mHtmlCached;
	}

	public void setHtmlCache(String html) {
		// Validate input.
		if (html.matches(".*\\<[^>]+>.*")) {
			mHtmlCached = html;
			mHtmlTime = System.currentTimeMillis();
		}
	}

	public String getHtmlCache() {
		return mHtmlTime < System.currentTimeMillis() - HTML_CACHE_TIME ? mHtmlCached : null ;
	}

	public void put(String key, Object value, int statusCode) {
		items.put(key, new CacheItem(value, statusCode));
	}
	
	public CacheItem get(String key) {
		CacheItem c = items.get(key);
		if (c != null) {
			c = (c.time + ITEM_CACHE_TIME) > System.currentTimeMillis() ? c : null;
		} else {
			c = null;
		}
		return c;
	}
	
	public class CacheItem {
		
		public long time;
		public int statuscode;
		public Object item;
		
		public CacheItem(Object o, int statusCode) {
			time = System.currentTimeMillis();
			this.statuscode = statusCode;
			item = o;
		}
	}
	
}
