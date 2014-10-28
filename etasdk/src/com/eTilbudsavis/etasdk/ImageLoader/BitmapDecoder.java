package com.eTilbudsavis.etasdk.ImageLoader;

import java.io.InputStream;

import android.graphics.Bitmap;

public interface BitmapDecoder {
	
	/**
	 * Interface for decoding a byte-array into it's Bitmap representation.
	 * 
	 * @param request The ImageRequest
	 * @param image The image data
	 * @return A bitmap, or null
	 */
	public Bitmap decode(ImageRequest ir, InputStream is);
	
}
