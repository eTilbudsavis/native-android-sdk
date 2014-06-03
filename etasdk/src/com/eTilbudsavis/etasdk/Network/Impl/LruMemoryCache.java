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

import com.eTilbudsavis.etasdk.Network.Cache;
import com.eTilbudsavis.etasdk.Network.Request;
import com.eTilbudsavis.etasdk.Network.Request.Method;
import com.eTilbudsavis.etasdk.Network.Response;
import com.eTilbudsavis.etasdk.Utils.EtaLog;

public class LruMemoryCache implements Cache {
	
	public static final String TAG = "LruMemoryCache";
	
	/** On average we've measured a Cache.Item from the ETA API to be around 4kb */
	private static final int AVG_ITEM_SIZE = 4096;
	
	/** The default fraction of memory to use */
	private static final int DEFAULT_MEMORY_TO_USE = 8;
	
	/** Default percentage of cache to clean */
	private static final int DEFAULT_PERCENT_TO_CLEAN = 20;
	
	/** Max cache size - init to 1mb */
	private int mMaxSize = 256;
	
	/** Perceent of cache to remove on cleanup */
	private int mPercentToClean = DEFAULT_PERCENT_TO_CLEAN;
	
	private Map<String, Item> mItems;
	
	public LruMemoryCache() {
		
		long maxMem = Runtime.getRuntime().maxMemory();
		int maxItems = (int)(maxMem / AVG_ITEM_SIZE);
		setLimit( maxItems / DEFAULT_MEMORY_TO_USE );
		
		// Allocate only memory needed
		mItems = Collections.synchronizedMap(new HashMap<String, Cache.Item>(mMaxSize));
		
	}
	
	public void setCleanLimit(int percentToClean) {
		mPercentToClean = percentToClean;
	}
	
	public void setLimit(int maxLimit) {
		mMaxSize = maxLimit;
		Log.d(TAG, "New MaxMemory: " + mMaxSize + ", equivalent to: " + (mMaxSize*AVG_ITEM_SIZE)/1024 + "kb");
	}
	
	public void put(Request<?> request, Response<?> response) {
		
        // If the request is cachable
        if (request.getMethod() == Method.GET && request.isCachable() && !request.isCacheHit() && response.cache != null) {
        	
        	request.addEvent("add-response-to-cache");
        	synchronized (mItems) {
            	mItems.putAll(response.cache);
    			checkSize();
			}
			
        }
        
	}
	
	private void checkSize() {
		
		int size = mItems.size();
		
		if( size > mMaxSize){
			
			float percentToRemove = (float)mPercentToClean/(float)100;
			int itemsToRemove = (int)(size*percentToRemove);
			
        	//least recently accessed item will be the first one iterated
        	Iterator<Entry<String, Cache.Item>> it = mItems.entrySet().iterator();
			while(it.hasNext()){
				it.next();
				it.remove();
				if(itemsToRemove-- == 0) {
					break;
				}
			}
			
			EtaLog.d(TAG, "Cleaned " + TAG + " new size: " + mItems.size());
			
		}
		
	}
	
	public Cache.Item get(String key) {
		
		synchronized (mItems) {
			
			Cache.Item c = mItems.get(key);
			if (c == null) {
				return null;
			} else if (c.isExpired()) {
				mItems.remove(key);
				return null;
			}
			return c;
			
		}
		
	}
	
	public void clear() {
		synchronized (mItems) {
			mItems.clear();
		}
	}
	
}
