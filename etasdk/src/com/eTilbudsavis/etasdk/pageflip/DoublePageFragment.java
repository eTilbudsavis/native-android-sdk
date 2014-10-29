package com.eTilbudsavis.etasdk.pageflip;

import java.util.concurrent.atomic.AtomicInteger;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.view.View;
import android.widget.ImageView;

import com.eTilbudsavis.etasdk.ImageLoader.BitmapProcessor;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.photoview.PhotoView.OnPhotoTapListener;

public class DoublePageFragment extends PageFragment {
	
	public static final String TAG = DoublePageFragment.class.getSimpleName();
	
	private AtomicInteger mCount = new AtomicInteger();
	private Bitmap mPage;
	private Canvas mCanvas;
	
	public void onResume() {
		super.onResume();
		getPhotoView().setOnPhotoTapListener(new OnPhotoTapListener() {
			
			public void onPhotoTap(View view, float x, float y) {
				
				if (x>0.5) {
					onClick(getSecondNum(), ((float)(x-0.5)*2) , y);
				} else {
					onClick(getFirstNum(), x*2, y);
				}
				
			}
		});
	};
	
	private void reset(String tag) {
		mPage = null;
		mCount = new AtomicInteger();
		mCanvas = null;
	}
	
	@Override
	public void loadView() {
		reset(getFirst().getView());
		int sampleSize = getCallback().isLowMemory() ? 2 : 0;
		load(getFirst().getView(), true, sampleSize);
		load(getSecond().getView(), false, sampleSize);
	}

	@Override
	public void loadZoom() {
		reset(getFirst().getZoom());
		boolean low = getCallback().isLowMemory();
		String firstUrl = low ? getFirst().getView() : getFirst().getZoom();
		load(firstUrl, true, 0);
		String secondUrl = low ? getSecond().getView() : getSecond().getZoom();
		load(secondUrl, false, 0);
	}
	
	private void load(String url, boolean left, int sampleSize) {
		ImageRequest r = new ImageRequest(url, new ImageView(getActivity()));
		r.setBitmapDisplayer(new DoublePageDisplayer());
		r.setBitmapProcessor(new DoublePageProcessor(left));
		r.setBitmapDecoder(new LowMemoryDecoder(sampleSize));
		addRequest(r);
	}
	
	public class DoublePageProcessor implements BitmapProcessor {
		
		private boolean mLeft = true;
		
		public DoublePageProcessor(boolean leftSide) {
			mLeft = leftSide;
		}
		
		public Bitmap process(Bitmap b) {
			if (mLeft) {
				merge(b, null);
			} else {
				merge(null, b);
			}
			// Recycle the old bitmap, and try to garbage collect... (garbage collection can't be forced)
			b.recycle();
			System.gc();
			return b;
		}

		private void merge(Bitmap l, Bitmap r) {
			
			boolean isLeft = l!=null;
			Bitmap b = (isLeft?l:r);
			createFullPage(b);
			int left = ( isLeft ? 0 : b.getWidth() );
			mCanvas.drawBitmap(b, left, 0, null);
			
		}
		
		private synchronized void createFullPage(Bitmap b) {
			// Lock to only allow one thread to create the new bitmap
			
			int count = 0;
			while (mPage==null && count < 3) {
				count++;
				int w = b.getWidth();
				int h = b.getHeight();
				try {
					mPage = Bitmap.createBitmap(w*2, h, Config.ARGB_8888);
					mCanvas = new Canvas(mPage);
				} catch (OutOfMemoryError e) {
					// TODO What to do about this?
					EtaLog.e(TAG, e.getMessage(), e);
					System.gc();
				}
				
			}

		}
		
	}
	
	public class DoublePageDisplayer extends PageFadeBitmapDisplayer {
		
		@Override
		public void display(ImageRequest ir) {
			
			if (mCount.getAndIncrement()==1) {
				Bitmap b = getPhotoView().getBitmap();
				if (b!=null && !b.isRecycled()) {
					b.recycle();
				}
				ir.setBitmap(mPage);
				super.display(ir);
			}
			
		}
		
	}
}
