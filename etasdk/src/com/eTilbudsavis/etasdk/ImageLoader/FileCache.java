package com.eTilbudsavis.etasdk.ImageLoader;

import android.graphics.Bitmap;

/**
 * Interface for caching bitmaps to disk.
 * @author Danny Hvam - danny@etilbudsavis.dk
 */
public interface FileCache {
	
	/**
	 * Method being called to save a {@link ImageRequest} to disk (the bitmap actually)
	 * @param id An identifier for the bitmap
	 * @param b A bitmap to cache
	 */
	public void save(String id, Bitmap b);
	
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