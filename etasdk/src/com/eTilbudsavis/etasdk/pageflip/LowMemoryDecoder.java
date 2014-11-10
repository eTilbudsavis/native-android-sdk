package com.eTilbudsavis.etasdk.pageflip;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import com.eTilbudsavis.etasdk.ImageLoader.BitmapDecoder;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;

public class LowMemoryDecoder implements BitmapDecoder {
	
	public static final String TAG = LowMemoryDecoder.class.getSimpleName();

	// Variable kept across all PageFragment, to save some work and mem
	private Point mDisplay;
	
	private static final float DISPLAY_SCALE_FACTOR = 0.8f;
	
	private int mSampleSize = 0;
	private boolean mAutoScale = false;
	private Context mContext;

	public LowMemoryDecoder(Context c, int sampleSize, boolean autoScale) {
		mContext = c;
		mAutoScale = autoScale;
		mSampleSize = sampleSize;
		mDisplay = PU.getDisplayDimen(c);
		if (PageflipUtils.hasLowMemory(c)) {
			// If it's a low memory device, lower the standards
			mDisplay.y = (int)((float)mDisplay.y*DISPLAY_SCALE_FACTOR);
			mDisplay.x = (int)((float)mDisplay.x*DISPLAY_SCALE_FACTOR);
		}
	}

	public LowMemoryDecoder(Context c) {
		this(c, 0, true);
	}
	
	public Bitmap decode(ImageRequest ir, byte[] image) {
		
		BitmapFactory.Options o = new BitmapFactory.Options();
		
		// try to make it mutable
	    if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
			o.inMutable = true;
		}
	    
	    if (mAutoScale) {
	    	
		    // Get the best possible size
		    o.inJustDecodeBounds = true;
		    BitmapFactory.decodeByteArray(image, 0, image.length, o);
		    
		    boolean land = PageflipUtils.isLandscape(mContext);
		    int w = (land?mDisplay.x:mDisplay.y);
		    int h = (land?mDisplay.y:mDisplay.x);
		    int displaySampleSize = calcDisplaySampleSize(o, h, w);
		    
		    // Find the largest, of either user provided or calculated sampleSize
		    o.inSampleSize = Math.max(mSampleSize, displaySampleSize);
		    o.inJustDecodeBounds = false;
		    
	    }
	    
	    // Perform actual decoding
//	    long s = System.currentTimeMillis();
		Bitmap b = BitmapFactory.decodeByteArray(image, 0, image.length, o);
//		EtaLog.d(TAG, "decode.time: " + (System.currentTimeMillis()-s));
//		PU.printBitmapInfo(TAG, b);
		return b;
	}
	
	private int calcDisplaySampleSize(BitmapFactory.Options o, int reqHeight, int reqWidth) {
		
		final int height = o.outHeight;
		final int width = o.outWidth;
		int inSampleSize = 1;
		
		if (height > reqHeight || width > reqWidth) {
			
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;
			
			while ((halfHeight / inSampleSize) > reqHeight || (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
			
		}
		return inSampleSize;
		
	}
	
}
