package com.eTilbudsavis.etasdk;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import com.eTilbudsavis.etasdk.Utils.Utils;

public class EtaCache implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "EtaCache";
	
	private static final long ITEM_CACHE_TIME = 15 * Utils.MINUTE_IN_MILLIS;
	private static final long HTML_CACHE_TIME = 15 * Utils.MINUTE_IN_MILLIS;
	private static final String HTML_REGEX = ".*\\<[^>]+>.*";
	
	private Map<String, CacheItem> mItems = Collections.synchronizedMap(new WeakHashMap<String, EtaCache.CacheItem>());

	public EtaCache() {
	}

	public void putHtml(String uuid, String html, int statusCode) {
		// Validate input.
		if (html.matches(HTML_REGEX)) {
			put(uuid, html, statusCode);
		}
	}

	public String getHtml(String uuid) {
		CacheItem c = mItems.get(uuid);
		String html = null;
		if (c != null && c.time > (System.currentTimeMillis() - HTML_CACHE_TIME) ) {
			html =  c.object.toString();
			html = html.matches(HTML_REGEX) ? html : null;
		}
		return html;
	}

	public void put(String key, Object value, int statusCode) {
		if (Utils.isSuccess(statusCode)) {
			mItems.put(key, new CacheItem(value, statusCode));
		}
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
		mItems = new HashMap<String, EtaCache.CacheItem>();
	}
	
	public class CacheItem {
		
		public long time;
		public int statuscode;
		public Object object;
		
		public CacheItem(Object o, int statusCode) {
			time = System.currentTimeMillis();
			this.statuscode = statusCode;
			object = o;
		}
	}
	
}
