package com.eTilbudsavis.etasdk.pageflip;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Hotspot;
import com.eTilbudsavis.etasdk.ImageLoader.BitmapProcessor;
import com.eTilbudsavis.etasdk.Log.EtaLog;

public class PageflipBitmapProcessor implements BitmapProcessor {
	
	public static final String TAG = PageflipBitmapProcessor.class.getSimpleName();
	
	private static final int[] mRectColors = { 
		Color.BLACK, 
		Color.BLUE, 
		Color.GREEN, 
		Color.RED, 
		Color.WHITE, 
		Color.YELLOW, 
		Color.MAGENTA 
	};
	
	private Catalog mCatalog;
	private int mPage = 0;
	private boolean mDrawHotspotRects = false;
	
	public PageflipBitmapProcessor(Catalog c, int page, boolean drawHotSpotRects) {
		mCatalog = c;
		mPage = page;
		mDrawHotspotRects = drawHotSpotRects;
	}
	
	public Bitmap process(Bitmap b) {
		
		try {
			if (mDrawHotspotRects) {
				return drawDebugRects(b);
			}
		} catch (Exception e) {
			EtaLog.e(TAG, e.getMessage(), e);
		}
		return b;

	}
	
	private Bitmap drawDebugRects(Bitmap immutableBitmap) {
		
		Bitmap b = null;
		List<Hotspot> hotspots = mCatalog.getHotspots().getHotspots().get(mPage);
		
		if (hotspots != null && !hotspots.isEmpty()) {
			
			b = immutableBitmap.copy(Bitmap.Config.ARGB_8888, true);
			immutableBitmap.recycle();
			Canvas c = new Canvas(b);
			
			Paint p = new Paint();
			
			p.setStyle(Paint.Style.STROKE);
			p.setStrokeWidth(5);
			
			int count = 0;
			int bw = b.getWidth();
			int bh = b.getHeight();
			for (Hotspot h : hotspots) {
				p.setColor(mRectColors[count%mRectColors.length]);
				int left = (int)(h.left*(double)bw);
				int top = (int)(h.top*(double)bh);
				int right = (int)(h.right*(double)bw);
				int bottom = (int)(h.bottom*(double)bh);
				Rect r = new Rect(left, top, right, bottom);
				c.drawRect(r, p);
				count++;
			}
			
		}
		
		return b;
	}

}
