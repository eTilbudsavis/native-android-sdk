package com.eTilbudsavis.etasdk.ImageLoader;

import java.io.IOException;

import android.graphics.Bitmap;

/**
 * Interface for downloading images from web.
 * @author Danny Hvam - danny@etilbudsavis.dk
 */
public interface ImageDownloader {

	/**
	 * This method should return a bitmap from the requested url.
	 * @param url An url pointing at the imageresource to download
	 * @return A bitmap, or {@code null}
	 * @throws IllegalStateException
	 * @throws IOException
	 * @throws OutOfMemoryError
	 */
	public Bitmap getBitmap(ImageRequest ir) throws IllegalStateException, IOException, OutOfMemoryError;

	/**
	 * This method should return a bitmap from the requested url.
	 * @param url An url pointing at the imageresource to download
	 * @return A bitmap, or {@code null}
	 * @throws IllegalStateException
	 * @throws IOException
	 * @throws OutOfMemoryError
	 */
	public byte[] getByteArray(ImageRequest ir) throws IllegalStateException, IOException, OutOfMemoryError;
	
}
