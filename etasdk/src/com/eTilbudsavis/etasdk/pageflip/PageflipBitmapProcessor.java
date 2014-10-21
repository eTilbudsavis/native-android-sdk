package com.eTilbudsavis.etasdk.pageflip;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.EtaObjects.helper.Hotspot;
import com.eTilbudsavis.etasdk.ImageLoader.BitmapProcessor;
import com.eTilbudsavis.etasdk.Log.EtaLog;

public class PageflipBitmapProcessor implements BitmapProcessor {
	
	public static final String TAG = PageflipBitmapProcessor.class.getSimpleName();
	
	private Catalog mCatalog;
	private int mPage = 0;
	private boolean mDrawHotspotRects = false;
	private boolean mLandscape = false;
	
	public PageflipBitmapProcessor(Catalog c, int page, boolean landscape, boolean drawHotSpotRects) {
		mCatalog = c;
		mPage = page;
		mLandscape = landscape;
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
	
	protected Bitmap drawDebugRects(Bitmap bitmap) {
		
		Bitmap b = null;
		List<Hotspot> hotspots = mCatalog.getHotspots().get(mPage);
		
		if (hotspots != null && !hotspots.isEmpty()) {
			
			// The given bitmap is immutable, so we'll copy it, and recycle the old one
			b = bitmap.copy(Bitmap.Config.ARGB_8888, true);
			bitmap.recycle();
			Canvas c = new Canvas(b);
			
			Paint p = new Paint();
			p.setStyle(Paint.Style.STROKE);
			p.setStrokeWidth(5);
			
			double bw = b.getWidth();
			double bh = b.getHeight();
			for (Hotspot h : hotspots) {
				if (h.isAreaSignificant(mLandscape)) {
					p.setColor(h.getColor());
					int left = (int)(h.left*bw);
					int top = (int)(h.top*bh);
					int right = (int)(h.right*bw);
					int bottom = (int)(h.bottom*bh);
					Rect r = new Rect(left, top, right, bottom);
					c.drawRect(r, p);
				}
			}
			
		}
		
		return b;
	}

}
