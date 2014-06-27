package com.eTilbudsavis.etasdk.ImageLoader.Impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.graphics.Bitmap;

import com.eTilbudsavis.etasdk.ImageLoader.MemoryCache;
import com.eTilbudsavis.etasdk.Log.EtaLog;

public class LruMemoryCache implements MemoryCache {
	
	public static final String TAG = MemoryCache.class.getSimpleName();
	
	//Last argument true for LRU ordering
	private Map<String, Bitmap> mCache = Collections.synchronizedMap(new LinkedHashMap<String, Bitmap>(10,1.5f,true));
	
	//current allocated size
	private long mSize = 0;
	
	//max memory in bytes
	private long mLimit = 0x100000;
	
	public LruMemoryCache() {
		mLimit = Runtime.getRuntime().maxMemory()/4;
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
		
		if(mSize>mLimit){
			EtaLog.d(TAG, "cache-clear");
			Iterator<Entry<String, Bitmap>> iter=mCache.entrySet().iterator();  
			while(iter.hasNext() && (mSize>mLimit)){
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
