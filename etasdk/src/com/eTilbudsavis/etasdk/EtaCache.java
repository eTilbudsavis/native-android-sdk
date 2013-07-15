package com.eTilbudsavis.etasdk;

import java.io.Serializable;
import java.util.HashMap;

public class EtaCache implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "EtaCache";
	
	private static final long ITEM_CACHE_TIME = 5*60*1000;
	private static final long HTML_CACHE_TIME = 15*60*1000;
	
	private HashMap<String, CachePageflip> mPageflip = new HashMap<String, CachePageflip>();
	private HashMap<String, CacheItem> mItems = new HashMap<String, EtaCache.CacheItem>();
	
	public EtaCache() {
	}

	public void setPageflipHtml(String html, String uuid) {
		// Validate input.
		if (html.matches(".*\\<[^>]+>.*")) {
			mPageflip.put(uuid, new CachePageflip(html));
		}
	}

	public String getPageflipHtml(String uuid) {
		CachePageflip cp = mPageflip.get(uuid);
		return cp == null ? null : cp.time < System.currentTimeMillis() - HTML_CACHE_TIME ? cp.html : null;
	}

	public void put(String key, Object value, int statusCode) {
		mItems.put(key, new CacheItem(value, statusCode));
	}
	
	public CacheItem get(String key) {
		CacheItem c = mItems.get(key);
		if (c != null) {
			c = (c.time + ITEM_CACHE_TIME) > System.currentTimeMillis() ? c : null;
		} else {
			c = null;
		}
		return c;
	}
	
	public void clear() {
		mPageflip = new HashMap<String, EtaCache.CachePageflip>();
		mItems = new HashMap<String, EtaCache.CacheItem>();
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
	
	private class CachePageflip {
		 
		public String html;
		public long time;
		
		public CachePageflip(String html) {
			this.html = html;
			this.time = System.currentTimeMillis();
		}
		
	}
	
}
