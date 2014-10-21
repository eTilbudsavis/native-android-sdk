package com.eTilbudsavis.etasdk.pageflip;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.view.View;
import android.widget.ImageView;

import com.eTilbudsavis.etasdk.ImageLoader.BitmapProcessor;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.photoview.PhotoView.OnPhotoTapListener;

public class PageflipDoublePage extends PageflipPage {
	
	private Object LOCK = new Object();
	private int mCount = 0;
	private Bitmap mPage;
	private Canvas mCanvas;
	private OnPhotoTapListener mTapListener = new OnPhotoTapListener() {
		
		public void onPhotoTap(View view, float x, float y) {
			
			if (x>0.5) {
				click(getRightPage(), ((float)(x-0.5)*2) , y);
			} else {
				click(getLeftPage(), x*2, y);
			}
			
		}
	};
	
	public void onResume() {
		super.onResume();
		getPhotoView().setOnPhotoTapListener(mTapListener);
	};
	
	private int getLeftPage() {
		return getPages()[0];
	}

	private int getRightPage() {
		return getPages()[1];
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
	
	@Override
	public void loadPages() {
		
		if (getPhotoView().getDrawable() == null) {
			
			runImageloader(getPage(getLeftPage()).getView(), true);
			
			runImageloader(getPage(getRightPage()).getView(), false);
			
		}
		
	}
	
	private void runImageloader(String url, boolean left) {
		ImageRequest r = new ImageRequest(url, new ImageView(getActivity()));
		r.setBitmapDisplayer(mDisplayer);
		r.setBitmapProcessor(getProcessor(left));
		addRequest(r);
	}
	
	public BitmapProcessor getProcessor(final boolean left) {
		int page = left ? getLeftPage() : getRightPage();
		return new PageflipBitmapProcessor(getCatalog(), page, isLandscape(), debug) {
			
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
	
}
