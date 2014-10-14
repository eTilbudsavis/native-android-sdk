package com.eTilbudsavis.etasdk.ImageLoader.Impl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.eTilbudsavis.etasdk.ImageLoader.BitmapDecoder;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;

public class DefaultBitmapDecoder implements BitmapDecoder {

	public Bitmap decode(ImageRequest request, byte[] image) {
		return BitmapFactory.decodeByteArray(image, 0, image.length);
	}
	
}
