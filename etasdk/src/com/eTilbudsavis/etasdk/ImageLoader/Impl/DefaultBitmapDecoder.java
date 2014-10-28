package com.eTilbudsavis.etasdk.ImageLoader.Impl;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.util.ByteArrayBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;

import com.eTilbudsavis.etasdk.ImageLoader.BitmapDecoder;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.Log.EtaLog;

public class DefaultBitmapDecoder implements BitmapDecoder {
	
	public static final String TAG = DefaultBitmapDecoder.class.getSimpleName();

	static final int init_buf = 0x10000;
	
	public Bitmap decode(ImageRequest ir, InputStream is) {
		
		try {
			
			byte[] image = entityToBytes(is);
			
			BitmapFactory.Options o = new BitmapFactory.Options();
//		    o.inJustDecodeBounds = true;
		    
//		    BitmapFactory.decodeByteArray(image, 0, image.length, o);
//		    o.inSampleSize = calculateInSampleSize(o, ir.getImageView().getMeasuredWidth(), ir.getImageView().getMeasuredHeight());
			
//		    o.inJustDecodeBounds = false;
		    
		    if (VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB) {
				o.inMutable = true;
			}
			return BitmapFactory.decodeByteArray(image, 0, image.length, o);
			
		} catch (Throwable t) {
			
		}
		return null;
	}
	
	public static void examineBitmap(String properties, Bitmap b) {
		EtaLog.d(TAG, "Bitmap[prop:" + properties + ", " + b.getWidth() + "x" + b.getHeight() + ", " + b.getByteCount()/1024 + "kb]");
	}
	
	private static byte[] entityToBytes(InputStream is) throws IOException {
		
		ByteArrayBuffer bytes = new ByteArrayBuffer(init_buf);
		if (is != null) {
			
			byte[] buf = new byte[init_buf];
			int c = -1;
			while ( (c=is.read(buf)) != -1) {
				bytes.append(buf, 0, c);
			}
			
		}
		
		return bytes.toByteArray();
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		int inSampleSize = 1;
		if (reqWidth == 0 || reqHeight == 0) {
			return inSampleSize;
		}
		
		final int height = options.outHeight;
		final int width = options.outWidth;

		
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
		
		EtaLog.d(TAG, "ImageView[w:" + reqWidth + ", h:" + reqHeight + "]");
		EtaLog.d(TAG, "Bitmap[w:" + height + ", h:" + width + ", scale:" + inSampleSize + "]");
		
		return inSampleSize;
	}
	
	static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }
        
        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }
}
