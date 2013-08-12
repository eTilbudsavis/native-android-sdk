package com.eTilbudsavis.etasdk;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.EtaObject;
import com.eTilbudsavis.etasdk.EtaObjects.ResponseWrapper;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class EtaCache implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String TAG = "EtaCache";
	
	private static final long ITEM_CACHE_TIME = 15 * Utils.MINUTE_IN_MILLIS;
	private static final long HTML_CACHE_TIME = 15 * Utils.MINUTE_IN_MILLIS;
	private static final String HTML_REGEX = ".*\\<[^>]+>.*";
	
	private Map<String, CacheItem> mItems = Collections.synchronizedMap(new HashMap<String, EtaCache.CacheItem>());

	public EtaCache() {
	}

	public void putHtml(String uuid, String html, int statusCode) {
		// Validate input.
		if (html.matches(HTML_REGEX)) {
			mItems.put(uuid, new CacheItem(html, statusCode));
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

	public void put(ResponseWrapper response) {
		if (Utils.isSuccess(response.getStatusCode())) {
			if (response.isJSONArray()) {
				put(response.getStatusCode(), response.getJSONArray());
			} else if (response.isJSONObject()) {
				put(response.getStatusCode(), response.getJSONObject());
			}
		}
	}
	
	public void put(int statusCode, JSONArray objects) {

		try {
		if (objects != null && objects.length()>0) {
				JSONObject o = objects.getJSONObject(0);
				if (o.has("ern")) {
					for (int i = 0; i < objects.length() ; i++) {
						put(statusCode, objects.getJSONObject(i));
					}
				}
			} 
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	public void put(int statucCode, JSONObject object) {
		if (object != null && object.has("ern")) {
			try {
				mItems.put(object.getString("ern"), new CacheItem(object, statucCode));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	public CacheItem get(String key) {
		CacheItem c = mItems.get(key);
		if (c != null) {
			if ( ! ((c.time + ITEM_CACHE_TIME) > System.currentTimeMillis()) ) {
				mItems.remove(c);
				c = null;
			}
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
