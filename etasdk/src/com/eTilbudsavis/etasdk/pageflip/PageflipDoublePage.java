package com.eTilbudsavis.etasdk.pageflip;

import java.util.concurrent.atomic.AtomicInteger;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.view.View;

import com.eTilbudsavis.etasdk.ImageLoader.BitmapProcessor;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.photoview.PhotoView.OnPhotoTapListener;

public class PageflipDoublePage extends PageflipPage {
	
	public static final String TAG = PageflipDoublePage.class.getSimpleName();
	
	private Object LOCK = new Object();
	private AtomicInteger mCount = new AtomicInteger();
	private Bitmap mPage;
	private Canvas mCanvas;
	String mTag;
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
		try {
			return getPages()[1];
		} catch (Exception mE) {
			EtaLog.d(TAG, "Pages are off... " + PageflipUtils.join(",", getPages()));
		}
		return getPages()[0]+1;
	}
	
	private void merge(Bitmap l, Bitmap r) {
		
		boolean isLeft = l!=null;
		Bitmap b = (isLeft?l:r);
		
		synchronized (LOCK) {
			// Lock to only allow one thread to create the new bitmap
			
			int count = 0;
			while (mPage==null && count < 3) {
				
				count++;

				EtaLog.d(TAG, "Count:"+count);
				
				int w = b.getWidth();
				int h = b.getHeight();
				try {
					printHeap();
					mPage = Bitmap.createBitmap(w*2, h, Config.ARGB_8888);
					mCanvas = new Canvas(mPage);
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					
					mPage = null;
					System.gc();
					// lets see if the GC is being caused now
					try {
						EtaLog.d(TAG, "sleep 500");
						Thread.sleep(1000);
						EtaLog.d(TAG, "done sleeping");
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				
			}
			
		}
		
		// Allow both threads to draw at the same time
		int left = ( isLeft ? 0 : b.getWidth() );
		mCanvas.drawBitmap(b, left, 0, null);
		
	}
	
	private void printHeap() {

		Runtime rt = Runtime.getRuntime();
		long kb = 1024;
		long free = rt.freeMemory()/kb;
		long available = rt.totalMemory()/kb;
		long max = rt.maxMemory()/kb;
		EtaLog.d(TAG, "Heap[free: " + free + ", available: " + available + ", max: " + max);
	}
	
	PageflipBitmapDisplayer mDisplayer = new PageflipBitmapDisplayer() {
		
		public void display(ImageRequest ir) {
			
			/*
			 *  BE AWARE: The bitmap in ImageRequest, have been recycled by the BitmapProcessor.
			 *  Use the dual-page bitmap instead
			 */
			
//			ir.isAlive("count:"+mCount.get());
			if (mCount.getAndIncrement()==1) {
				ir.setBitmap(mPage);
				super.display(ir);
			}
		};
	};

	@Override
	public void loadView() {
		mTag = getPage(getLeftPage()).getView();
		load(getPage(getLeftPage()).getView(), true);
		load(getPage(getRightPage()).getView(), false);
	}

	@Override
	public void loadZoom() {
		mTag = getPage(getLeftPage()).getZoom();
		load(getPage(getLeftPage()).getZoom(), true);
		load(getPage(getRightPage()).getZoom(), false);
	}
	
	private void load(String url, boolean left) {
		getPhotoView().setTag(mTag);
		ImageRequest r = new ImageRequest(url, getPhotoView());
		r.setBitmapDisplayer(mDisplayer);
		r.setBitmapProcessor(getProcessor(left));
		
		addRequest(r);
	}
	
	public BitmapProcessor getProcessor(final boolean left) {
		int page = left ? getLeftPage() : getRightPage();
		return new PageflipBitmapProcessor(getCatalog(), page, isLandscape(), debug) {
			
			public Bitmap process(Bitmap b) {
				try {
					b = super.process(b);
					if (left) {
						merge(b, null);
					} else {
						merge(null, b);
					}
					b.recycle();
					// Try to garbage collect... this might not work...
					System.gc();
					// Bitmap have been recycled by now, don't use it
				} catch (Exception e) {
					e.printStackTrace();
				}
				return b;
			}
		};
	}
	
}
