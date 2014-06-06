/*******************************************************************************
* Copyright 2014 eTilbudsavis
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*******************************************************************************/
package com.eTilbudsavis.etasdk.Network.Impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.util.Log;

import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.Network.Cache;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.Request.Method;
import com.eTilbudsavis.etasdk.Network.Response;

public class MemoryCache implements Cache {
	
	public static final String TAG = "LruMemoryCache";
	
	/** On average we've measured a Cache.Item from the ETA API to be around 4kb */
	private static final int AVG_ITEM_SIZE = 4096;
	
	/** Max cache size - init to 1mb */
	private int mMaxItems = 256;
	
	/** Perceent of cache to remove on cleanup */
	private int mPercentToClean = 20;
	
	private Map<String, Item> mCache;
	
	public MemoryCache() {
		
		setLimit(Runtime.getRuntime().maxMemory() / 8);
		
		// Allocate only memory needed
		mCache = Collections.synchronizedMap(new HashMap<String, Cache.Item>(mMaxItems));
		
	}
	
	/**
	 * Set the percentage of cache to clean out when memory limit is hit
	 * @param percentToClean A percentage between 0 and 100 (default is 20)
	 */
	public void setCleanLimit(int percentToClean) {
		if (percentToClean <= 0 || 100 <= percentToClean) {
			new IllegalArgumentException("Percent a number between 0-100");
		}
		mPercentToClean = percentToClean;
	}
	
	/**
	 * Set the limit on memory this Cache may use.
	 * @param maxMemLimit The limit in bytes
	 */
	public void setLimit(long maxMemLimit) {
		if (maxMemLimit > Runtime.getRuntime().maxMemory()) {
			throw new IllegalArgumentException("maxMemLimit cannot be more than max heap size");
		}
		mMaxItems = (int)(maxMemLimit / AVG_ITEM_SIZE);
		Log.d(TAG, "New memory limit: " + maxMemLimit/1024 + "kb (" + mMaxItems + " items)");
	}
	
	public void put(Request<?> request, Response<?> response) {
		
        // If the request is cachable
        if (request.getMethod() == Method.GET && request.isCachable() && !request.isCacheHit() && response.cache != null) {
        	
        	request.addEvent("add-response-to-cache");
        	synchronized (mCache) {
            	mCache.putAll(response.cache);
    			checkSize();
			}
			
        }
        
	}
	
	private void checkSize() {
		
		int size = mCache.size();
		
		if( size > mMaxItems){
			
			float percentToRemove = (float)mPercentToClean/(float)100;
			int itemsToRemove = (int)(size*percentToRemove);
			
        	//least recently accessed item will be the first one iterated
        	Iterator<Entry<String, Cache.Item>> it = mCache.entrySet().iterator();
			while(it.hasNext()){
				it.next();
				it.remove();
				if(itemsToRemove-- == 0) {
					break;
				}
			}
			
			EtaLog.d(TAG, "Cleaned " + TAG + " new size: " + mCache.size());
			
		}
		
	}
	
	public Cache.Item get(String key) {
		
		synchronized (mCache) {
			
			Cache.Item c = mCache.get(key);
			if (c == null) {
				return null;
			} else if (c.isExpired()) {
				mCache.remove(key);
				return null;
			}
			return c;
			
		}
		
	}
	
	public void clear() {
		synchronized (mCache) {
			mCache.clear();
		}
	}
	
}
