package com.eTilbudsavis.etasdk.ImageLoader.Impl;

import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.eTilbudsavis.etasdk.ImageLoader.BitmapDecoder;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;

public class ScaleBitmapDecoder implements BitmapDecoder {
	
	public static final String TAG = ScaleBitmapDecoder.class.getSimpleName();
		
	public Bitmap decode(ImageRequest ir, InputStream is) {
		
//		BitmapFactory.Options options = new BitmapFactory.Options();
//		options.inJustDecodeBounds = true;
//		BitmapFactory.decodeByteArray(image, 0, image.length, options);
//		
//		int imageHeight = options.outHeight;
//		int imageWidth = options.outWidth;
//		String imageType = options.outMimeType;
////		options.inSampleSize = calculateInSampleSize(options, imageWidth, imageHeight);
//		options.inSampleSize = 2;
//		options.inJustDecodeBounds = false;
//		
//		Bitmap b = BitmapFactory.decodeByteArray(image, 0, image.length);
//		EtaLog.d(TAG, "bitmap[" +b.getHeight()+ "," + b.getWidth() + "]");
//		b = BitmapFactory.decodeByteArray(image, 0, image.length, options);
//		EtaLog.d(TAG, "bitmap-scaled[" +b.getHeight()+ "," + b.getWidth() + "]");
		
//		return b;
		return null;
	}
	
}
