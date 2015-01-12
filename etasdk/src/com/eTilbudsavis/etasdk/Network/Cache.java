package com.eTilbudsavis.etasdk.network;

import java.io.Serializable;

public interface Cache {
	
	public void put(Request<?> request, Response<?> response);
	
	public Cache.Item get(String key);
	
	public void clear();
	

	public static class Item implements Serializable {
		
		private static final long serialVersionUID = 1L;
		
		// Time of insertion
		public final long expires;
		public final Object object;
		public long size;
		
		public Item(Object o, long timeToLive) {
			this.expires = System.currentTimeMillis() + timeToLive;
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
