package com.eTilbudsavis.etasdk.pageflip;

import java.util.concurrent.atomic.AtomicInteger;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.view.View;
import android.widget.ImageView;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.ImageLoader.ImageLoader;
import com.eTilbudsavis.etasdk.ImageLoader.ImageRequest;
import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.photoview.PhotoView.OnPhotoDoubleClickListener;
import com.eTilbudsavis.etasdk.photoview.PhotoView.OnPhotoLongClickListener;
import com.eTilbudsavis.etasdk.photoview.PhotoView.OnPhotoTapListener;

public class DoublePageFragment extends PageFragment {
	
	public static final String TAG = DoublePageFragment.class.getSimpleName();

	private Object LOCK = new Object();
	private AtomicInteger mCount = new AtomicInteger();
	private Bitmap mPage;
	private Canvas mCanvas;
	private boolean mClearBitmap = false;
	
	public void onResume() {
		super.onResume();
		getPhotoView().setOnPhotoTapListener(new OnPhotoTapListener() {
			
			public void onPhotoTap(View view, float x, float y) {
				Converter c = new Converter(x, y);
				onSingleClick(c.page, c.x , c.y);
			}
		});
		getPhotoView().setOnPhotoDoubleClickListener(new OnPhotoDoubleClickListener() {
			
			public void onPhotoTap(View view, float x, float y) {
				Converter c = new Converter(x, y);
				onDoubleClick(c.page, c.x , c.y);
			}
		});
		getPhotoView().setOnPhotoLongClickListener(new OnPhotoLongClickListener() {
			
			public void onPhotoTap(View view, float x, float y) {
				Converter c = new Converter(x, y);
				onLongClick(c.page, c.x , c.y);
			}
		});
	};
	
	private void reset(String tag) {
		mClearBitmap = true;
		mPage = null;
		mCount = new AtomicInteger();
		mCanvas = null;
	}
	
	@Override
	public void loadView() {
		reset(getFirst().getView());
		int sampleSize = lowMem() ? 2 : 1;
		load(getFirst().getView(), true, sampleSize, true);
		load(getSecond().getView(), false, sampleSize, true);
	}
	
	@Override
	public void loadZoom() {
		if (lowMem()) {
			return;
		}
		reset(getFirst().getZoom());
		int sampleSize = lowMem() ? 2 : 1;
		String firstUrl = getFirst().getZoom();
		load(firstUrl, true, sampleSize, false);
		String secondUrl = getSecond().getZoom();
		load(secondUrl, false, sampleSize, false);
	}
	
	private void load(String url, boolean left, int sampleSize, boolean autoScale) {
		ImageRequest r = new ImageRequest(url, new ImageView(getActivity()));
		r.setBitmapDisplayer(new DoublePageDisplayer());
		r.setBitmapProcessor(new DoublePageProcessor(left));
		r.setBitmapDecoder(new LowMemoryDecoder(getActivity(), sampleSize, autoScale));
		addRequest(r);
	}
	
	public class DoublePageProcessor extends PageBitmapProcessor {
		
		private boolean mLeft = true;
		
		public DoublePageProcessor(boolean leftSide) {
			super(leftSide?getFirstNum():getSecondNum());
			mLeft = leftSide;
		}
		
		public Bitmap process(Bitmap b) {
			
			synchronized (LOCK) {
				
				b = super.process(b);
				
				if (mLeft) {
					merge(b, null);
				} else {
					merge(null, b);
				}
				
				/* 
				 * Recycle the old bitmap, and try to garbage collect...
				 * (garbage collection can't be forced)
				 */
				b.recycle();
				System.gc();
				
			}
			return b;
		}

		private void merge(Bitmap l, Bitmap r) {
			
			boolean isLeft = l!=null;
			Bitmap b = (isLeft?l:r);
			synchronized (LowMemoryDecoder.LOCK) {
				createDoublePage(b);
			}
			if (mPage != null) {
				int left = ( isLeft ? 0 : b.getWidth() );
				mCanvas.drawBitmap(b, left, 0, null);
			}
			
		}
		
		private void createDoublePage(Bitmap b) {
			
			boolean allowRetry = true;
			while (mPage==null && allowRetry) {
				
				int w = b.getWidth();
				int h = b.getHeight();
				try {
					mPage = Bitmap.createBitmap(w*2, h, Config.ARGB_8888);
					mCanvas = new Canvas(mPage);
				} catch (OutOfMemoryError e) {
					
					if (allowRetry) {
						allowRetry = false;
						EtaLog.e(TAG, e.getMessage(), e);
						try {
							// Try to clear up some memory
							Eta.getInstance().getRequestQueue().clear();
							ImageLoader.getInstance().getMemoryCache().clear();
							// 'force' a GC
							Runtime.getRuntime().gc();
							// Wait, and hope for the best
							Thread.sleep(1000);
						} catch (InterruptedException e1) {
							EtaLog.e(TAG, "Sleep failed");
						}
					} else {
						throw e;
					}
					
				}
				
			}
			
		}
		
	}
	
	public class DoublePageDisplayer extends PageFadeBitmapDisplayer {

		@Override
		public void display(ImageRequest ir) {
			
			if (mCount.getAndIncrement()==1) {
				if (mClearBitmap) {
					mClearBitmap = false;
					Bitmap b = getPhotoView().getBitmap();
					if (b!=null && !b.isRecycled()) {
						b.recycle();
					}
				}
				ir.setBitmap(mPage);
				super.display(ir);
			}
			
		}

	}

	private class Converter {
		int page;
		float x;
		float y;
		public Converter(float x, float y) {

			if (x>0.5f) {
				this.page = getSecondNum();
				this.x = (x-0.5f)*2;
				this.y = y;
			} else {
				this.page = getFirstNum();
				this.x = x*2;
				this.y = y;
			}
			
		}
	}
	
}
