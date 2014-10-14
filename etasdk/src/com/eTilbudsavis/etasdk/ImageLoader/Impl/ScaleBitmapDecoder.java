package com.eTilbudsavis.etasdk.ImageLoader.Impl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.eTilbudsavis.etasdk.ImageLoader.BitmapDecoder;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.Log.EtaLog;

public class ScaleBitmapDecoder implements BitmapDecoder {
	
	public static final String TAG = ScaleBitmapDecoder.class.getSimpleName();
		
	public Bitmap decode(ImageRequest request, byte[] image) {
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(image, 0, image.length, options);
		
		int imageHeight = options.outHeight;
		int imageWidth = options.outWidth;
		String imageType = options.outMimeType;
//		options.inSampleSize = calculateInSampleSize(options, imageWidth, imageHeight);
		options.inSampleSize = 2;
		options.inJustDecodeBounds = false;
		
		Bitmap b = BitmapFactory.decodeByteArray(image, 0, image.length);
		EtaLog.d(TAG, "bitmap[" +b.getHeight()+ "," + b.getWidth() + "]");
		b = BitmapFactory.decodeByteArray(image, 0, image.length, options);
		EtaLog.d(TAG, "bitmap-scaled[" +b.getHeight()+ "," + b.getWidth() + "]");
		
		return b;
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) > reqHeight
                && (halfWidth / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
    }

    return inSampleSize;
}
}
