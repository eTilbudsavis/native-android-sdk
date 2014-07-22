package com.eTilbudsavis.etasdk.ImageLoader;

import android.graphics.Bitmap;

import com.eTilbudsavis.etasdk.DefaultThreadFactory;

/**
 * Interface that allows pre/post processing of Bitmaps while the ImageLoader
 * is performing the request. This method will be called from a thread in the
 * ExecutorService pool, so by using {@link DefaultThreadFactory} the interface
 * will be running on a low-priority thread.
 * 
 * @author Danny Hvam - danny@etilbudsavis.dk
 */
public interface BitmapProcessor {
	
	/**
	 * This method will be called as soon as the {@link ImageLoader} has loaded
	 * the bitmap. And you can then perform any action on the Bitmap.
	 * @param b A Bitmap that have been loaded by the {@link ImageLoader}
	 * @return A new bitmap, that have been processed
	 */
	public Bitmap process(Bitmap b);
	
}
