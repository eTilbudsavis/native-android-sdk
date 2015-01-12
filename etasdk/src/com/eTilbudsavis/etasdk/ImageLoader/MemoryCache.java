package com.eTilbudsavis.etasdk.imageloader;

import android.graphics.Bitmap;

/**
 * Interface for caching bitmaps to memory.
 * @author Danny Hvam - danny@etilbudsavis.dk
 */
public interface MemoryCache {

	/**
	 * Method being called to save a {@link ImageRequest} to memory (the bitmap actually)
	 * @param id An identifier for the bitmap
	 * @param b A bitmap to cache
	 */
	public void put(String id, Bitmap b);
	
	/**
	 * Method for getting bitmaps from cache.
	 * @param id An identifier to search for in cache
	 * @return A bitmap if one exists, else {@code null}
	 */
	public Bitmap get(String id);

	/**
	 * Method for clearing the cache completely
	 */
	public void clear();
	
}