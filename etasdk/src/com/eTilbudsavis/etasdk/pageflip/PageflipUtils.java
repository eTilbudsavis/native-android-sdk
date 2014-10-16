package com.eTilbudsavis.etasdk.pageflip;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.Log.EtaLog;

public class PageflipUtils {
	
	public static final String TAG = PageflipUtils.class.getSimpleName();
	
	private PageflipUtils() {
		// Empty constructor
	}

	public static boolean isLandscape(Context c) {
		return isLandscape(c.getResources().getConfiguration());
	}
	
	public static boolean isLandscape(Configuration c) {
		return c.orientation == Configuration.ORIENTATION_LANDSCAPE;
	}
	
	public static void test() {
		
		EtaLog.d(TAG, "Running page/position tests");
		testPageToPosition(0, true, 0);
		testPageToPosition(1, true, 1);
		testPageToPosition(2, true, 1);
		testPageToPosition(3, true, 2);
		testPageToPosition(4, true, 2);
		
		testPageToPosition(0, false, 0);
		testPageToPosition(1, false, 1);
		testPageToPosition(2, false, 2);
		testPageToPosition(3, false, 3);
		testPageToPosition(4, false, 4);

		testPositionToPage(0, true, 0);
		testPositionToPage(1, true, 1);
		testPositionToPage(2, true, 3);
		testPositionToPage(3, true, 5);
		testPositionToPage(4, true, 7);

		testPositionToPage(0, false, 0);
		testPositionToPage(1, false, 1);
		testPositionToPage(2, false, 2);
		testPositionToPage(3, false, 3);
		testPositionToPage(4, false, 4);

		EtaLog.d(TAG, "Done page/position tests");
	}
	
	private static void testPageToPosition(int page, boolean land, int expectedPos) {
		if ( pageToPosition(page, land) != expectedPos ) {
			EtaLog.e(TAG, "pageToPosition[page:" + page + ", land:" + land + ", expected:" + expectedPos);
		}
	}
	
	private static void testPositionToPage(int pos, boolean land, int expectedPage) {
		if ( positionToPage(pos, land) != expectedPage ) {
			EtaLog.e(TAG, "positionToPage[pos:" + pos + ", land:" + land + ", expected:" + expectedPage);
		}
	}
	
	public static int pageToPosition(int page, boolean landscape) {
		int pos = page;
		if (landscape) {
			if (page%2!=0) {
				page++;
			}
			pos = page/2;
		}
//		EtaLog.d(TAG, "pageToPosition[page:" + page + ", pos:" + pos + "]");
		return pos;
	}
	
	public static int positionToPage(int position, boolean landscape) {
		int page = position;
		if (landscape) {
			if (position == 0) {
				page = 0;
			} else {
				page = (position*2)-1;
			}
		}
		
//		EtaLog.d(TAG, "positionToPage[page:" + page + ", pos:" + position + "]");
		return page;
	}

	public static boolean withInPageRange(Catalog c, int page) {
		return ( 0 <= page && page < c.getPageCount());
	}
	
	public static Bitmap mergeImage(Bitmap leftBitmap, Bitmap rightBitmap) {
		int w = leftBitmap.getWidth()*2;
		int h = leftBitmap.getHeight();
		Bitmap b = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		Canvas canvas = new Canvas(b);
		canvas.drawBitmap(leftBitmap, 0, 0, null);
		canvas.drawBitmap(rightBitmap, (w/2), 0, null);
		return b;
	}
	
}
