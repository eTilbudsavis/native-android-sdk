package com.eTilbudsavis.etasdk.pageflip;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;

public class PageflipUtils {

	private PageflipUtils() {
		// TODO Auto-generated constructor stub
	}

	public static boolean isLandscape(Context c) {
		return isLandscape(c.getResources().getConfiguration());
	}
	
	public static boolean isLandscape(Configuration c) {
		return c.orientation == Configuration.ORIENTATION_LANDSCAPE;
	}
	
	public static int pageToPosition(int page, boolean landscape) {
		if (page<0) {
			throw new IllegalArgumentException("Page cannot be negative");
		}
		int pos = -1;
		if (landscape) {
			
			if (page==0) {
				pos = 0;
			} else {
				pos = ( (page-(page%2)) /2)+1;
			}
			
		} else {
			pos = page;
		}
		return pos;
	}
	
	public static int positionToPage(int position, boolean landscape) {
		if (position<0) {
			throw new IllegalArgumentException("Position cannot be negative");
		}
		int page = -1;
		if (landscape) {
			
			if (position == 0) {
				page = 0;
			} else {
				page = (position*2)-1;
			}
			
		} else {
			page = position;
		}
		return page;
	}

	public static Bitmap mergeImage(Bitmap leftBitmap, Bitmap rightBitmap) {
		int width = leftBitmap.getWidth()*2;
		int height = leftBitmap.getHeight();
		Bitmap mBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(mBitmap);
		canvas.drawBitmap(leftBitmap, 0, 0, null);
		canvas.drawBitmap(rightBitmap, (width/2), 0, null);
		return mBitmap;
	}
}
