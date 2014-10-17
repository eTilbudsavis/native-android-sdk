package com.eTilbudsavis.etasdk.pageflip;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.ImageLoader.BitmapProcessor;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.photoview.PhotoView.OnPhotoTapListener;

public class PageflipDoublePage extends PageflipPage {

	public static PageflipPage newInstance(Catalog c, int page) {
		Bundle b = new Bundle();
		b.putSerializable(ARG_CATALOG, c);
		b.putInt(ARG_PAGE, page);
		PageflipPage f = new PageflipDoublePage();
		f.setArguments(b);
		return f;
	}
	
	Object LOCK = new Object();
	private int mCount = 0;
	private Bitmap mPage;
	private Canvas mCanvas;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);

		getPhotoView().setOnPhotoTapListener(new OnPhotoTapListener() {

			public void onPhotoTap(View view, float x, float y) {
				
				EtaLog.d(TAG, "view:" + view.getClass().getSimpleName() + ", x:" + x + ", y:" + y);
				
			}
		});

		return v;
	}
	
	private void merge(Bitmap l, Bitmap r) {
		
		boolean isLeft = l!=null;
		Bitmap b = (isLeft?l:r);
		
		synchronized (LOCK) {
			// Lock to only allow one thread to create the new bitmap
			if(mPage==null) {
				int w = b.getWidth();
				int h = b.getHeight();
				mPage = Bitmap.createBitmap(w*2, h, Config.ARGB_8888);
				mCanvas = new Canvas(mPage);
			}
		}
		// Allow both threads to draw at the same time
		int left = ( isLeft ? 0 : b.getWidth() );
		mCanvas.drawBitmap(b, left, 0, null);
		mCount++;
		
	}
	
	PageflipBitmapDisplayer mDisplayer = new PageflipBitmapDisplayer() {
		
		public void display(ImageRequest ir) {
			if (mCount==2) {
				ir.setBitmap(mPage);
				super.display(ir);
			}
		};
	};
	
	public BitmapProcessor getProcessor(final boolean left) {
		return new PageflipBitmapProcessor(getCatalog(), getPage(), mDrawHotSpotRects) {
			
			public Bitmap process(Bitmap b) {
				b = super.process(b);
				if (left) {
					merge(b, null);
				} else {
					merge(null, b);
				}
				b.recycle();
				// Bitmap have been recycled by now, don't use it
				return null;
			}
		};
	}
	@Override
	public void loadPages() {
		
		if (getPhotoView().getDrawable() == null) {
			
			runImageloader(getPageLeft().getView(), true);
			
			runImageloader(getPageRight().getView(), false);
			
		}
		
	}
	
	private void runImageloader(String url, boolean left) {
		ImageRequest r = new ImageRequest(url, new ImageView(getActivity()));
		r.setBitmapDisplayer(mDisplayer);
		r.setBitmapProcessor(getProcessor(left));
		addRequest(r);
	}
	
}
