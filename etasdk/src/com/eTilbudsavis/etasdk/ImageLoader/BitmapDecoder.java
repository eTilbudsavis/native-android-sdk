package com.eTilbudsavis.etasdk.ImageLoader;

import android.graphics.Bitmap;

public interface BitmapDecoder {
	
	/**
	 * Interface for decoding a byte-array into it's Bitmap representation.
	 * 
	 * @param request The ImageRequest
	 * @param image The image data
	 * @return A bitmap, or null
	 */
	public Bitmap decode(ImageRequest request, byte[] image);
	
}
