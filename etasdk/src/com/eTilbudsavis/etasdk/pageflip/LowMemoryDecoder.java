package com.eTilbudsavis.etasdk.pageflip;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import com.eTilbudsavis.etasdk.ImageLoader.BitmapDecoder;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;

public class LowMemoryDecoder implements BitmapDecoder {
	
	public static final String TAG = LowMemoryDecoder.class.getSimpleName();
	
	private int mSampleSize = 0;
	
	public LowMemoryDecoder(int sampleSize) {
		mSampleSize = sampleSize;
	}
	
	public Bitmap decode(ImageRequest ir, byte[] image) {
		
		BitmapFactory.Options o = new BitmapFactory.Options();
	    if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
			o.inMutable = true;
		}
	    o.inSampleSize = mSampleSize;
		return BitmapFactory.decodeByteArray(image, 0, image.length, o);
		
	}
	
}
