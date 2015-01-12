package com.eTilbudsavis.etasdk.imageloader.impl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import com.eTilbudsavis.etasdk.imageloader.BitmapDecoder;
import com.eTilbudsavis.etasdk.imageloader.ImageRequest;

public class DefaultBitmapDecoder implements BitmapDecoder {
	
	public static final String TAG = DefaultBitmapDecoder.class.getSimpleName();
	
	public Bitmap decode(ImageRequest ir, byte[] image) {
		
		BitmapFactory.Options o = new BitmapFactory.Options();
	    if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
			o.inMutable = true;
		}
		return BitmapFactory.decodeByteArray(image, 0, image.length, o);
		
	}
	
}
