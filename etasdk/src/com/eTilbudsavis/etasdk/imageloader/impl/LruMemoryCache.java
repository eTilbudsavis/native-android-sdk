package com.eTilbudsavis.etasdk.imageloader.impl;
import com.eTilbudsavis.etasdk.Constants;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.graphics.Bitmap;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.imageloader.MemoryCache;
import com.eTilbudsavis.etasdk.log.EtaLog;

public class LruMemoryCache implements MemoryCache {
	
	public static final String TAG = Constants.getTag(LruMemoryCache.class);
	
	/**
	 * The map containing the actual cache. Last argument true for LRU ordering
	 */
	private Map<String, Bitmap> mCache = Collections.synchronizedMap(new LinkedHashMap<String, Bitmap>(10,1.5f,true));
	
	/** The currently allocated size of cache */
	private long mSize = 0;
	
	/** Max memory in bytes - default to 1mb */
	private long mMemoryLimit = 0x100000;
	
	public LruMemoryCache() {
		// Set maxMem to 20% the size of max available memory in VM
		setMemoryLimit(Runtime.getRuntime().maxMemory()/8);
	}
	
	/**
	 * Set the max memory limit for this cache.
	 * @param limit A memory limit between 0 and max available memory in VM
	 */
	public void setMemoryLimit(long limit) {
		if (limit < 0 || limit > Runtime.getRuntime().maxMemory()) {
			throw new IllegalArgumentException("Invalid memory limit: " + limit);
		}
		mMemoryLimit = limit;
	}
	
	public void put(String id, Bitmap b){
		try{
			if(mCache.containsKey(id)) {
				mSize-=getSizeInBytes(mCache.get(id));
			}
			mCache.put(id, b);
			mSize+=getSizeInBytes(b);
			checkSize();
		}catch(Throwable th){
			EtaLog.d(TAG, th.getMessage(), th);
		}
		
	}

	public Bitmap get(String id){
		try{
			//NullPointerException sometimes happen here http://code.google.com/p/osmdroid/issues/detail?id=78
			return mCache.get(id);
		}catch(NullPointerException e){
			EtaLog.d(TAG, e.getMessage(), e);
		}
		return null;
	}
	
	private void checkSize() {
		
		if(mSize>mMemoryLimit){
			EtaLog.d(TAG, "cache-clear");
			Iterator<Entry<String, Bitmap>> iter=mCache.entrySet().iterator();  
			while(iter.hasNext() && (mSize>mMemoryLimit)){
				Entry<String, Bitmap> entry=iter.next();
				mSize-=getSizeInBytes(entry.getValue());
				iter.remove();
			}
			
		}
	}
	
	public void clear() {
		try{
			//NullPointerException sometimes happen here http://code.google.com/p/osmdroid/issues/detail?id=78 
			mCache.clear();
			mSize=0;
		}catch(NullPointerException ex){
			EtaLog.d(TAG, ex.getMessage(), ex);
		}
	}

	long getSizeInBytes(Bitmap b) {
		return (b==null) ? 0 : (b.getRowBytes() * b.getHeight());
	}
	
}
