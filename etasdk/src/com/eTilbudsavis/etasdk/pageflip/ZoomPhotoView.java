package com.eTilbudsavis.etasdk.pageflip;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.AttributeSet;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.photoview.PhotoView;

public class ZoomPhotoView extends PhotoView {
	
	public static final String TAG = Eta.TAG_PREFIX + ZoomPhotoView.class.getSimpleName();
	
	private static final String STATE_DISPLAY_MATRIX = "state_display_matrix";
	private static final float MIN_SCALE_EPSILON = 0.1f;
	
	private boolean mZoomed = false;
	private OnZoomChangeListener mZoomChangeListener;
	private OnMatrixChangedListener mMatrixChangedListener;
	private OnMatrixChangedListener mMyMatrixChangedListener = new OnMatrixChangedListener() {
		
		public void onMatrixChanged(RectF rect) {
			float scale = getScale();
			boolean isMinScale = PageflipUtils.almost(scale, getMinimumScale(), MIN_SCALE_EPSILON);
			setAllowParentInterceptOnEdge(isMinScale);
			if (scale > getMinimumScale()) {
				if (isMinScale && mZoomed) {
					zoomChange(false);
				} else if (!mZoomed && !isMinScale) {
					zoomChange(true);
				}
			}
			
			if (mMatrixChangedListener!=null) {
				mMatrixChangedListener.onMatrixChanged(rect);
			}
		}
		
	};
	
	private void zoomChange(boolean isZoomed) {
		mZoomed = isZoomed;
		if (mZoomChangeListener!=null) {
			mZoomChangeListener.onZoomChange(isZoomed);
		}
	}
	
	public ZoomPhotoView(Context context) {
		super(context);
		init();
	}

	public ZoomPhotoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ZoomPhotoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		super.setOnMatrixChangeListener(mMyMatrixChangedListener);
	}
	
	@Override
	public void setOnMatrixChangeListener(OnMatrixChangedListener listener) {
		mMatrixChangedListener = listener;
	}
	
	public void setOnZoomListener(OnZoomChangeListener l) {
		mZoomChangeListener = l;
	}
	
	public boolean isZoomed() {
		return mZoomed;
	}
	
	private void saveState(Bundle outState) {
		if (outState!=null) {
			float[] mMatrixValues = new float[9];
			getSupportMatrix().getValues(mMatrixValues);
			outState.putFloatArray(STATE_DISPLAY_MATRIX, mMatrixValues);
		}
	}
	
	private void restoreState(Bundle inState) {
		if (inState!=null) {
			float[] mMatrixValues = inState.getFloatArray(STATE_DISPLAY_MATRIX);
			getSupportMatrix().setValues(mMatrixValues);
			setImageMatrix(getDisplayMatrix());
		}
	}
	
	public Bitmap getBitmap() {
		BitmapDrawable d = (BitmapDrawable)getDrawable();
		if (d != null) {
			return d.getBitmap();
		}
		return null;
	}
	
	public boolean isBitmapValid() {
		return isBitmapValid(getBitmap());
	}
	
	private boolean isBitmapValid(Bitmap b) {
		return b!=null && !b.isRecycled();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if (isBitmapValid()) {
			super.onDraw(canvas);
		}
	}
	
	public void resetScale() {
		if (getScale() != getMinimumScale()) {
			setScale(getMinimumScale());
		}
	}
	
	public void recycle() {
		Bitmap b = getBitmap();
		if (isBitmapValid(b)) {
			resetScale();
			b.recycle();
			b = null;
			super.setImageBitmap(null);
		}
	}
	
	@Override
	public void setImageBitmap(Bitmap bm) {
		Bundle state = null;
		Bitmap b = getBitmap();
		boolean valid = isBitmapValid(b);
		if (valid) {
			state = new Bundle();
			saveState(state);
		}
		// Remove the MatrixChangeListener, as it shouldn't be perceived as a matrix change
		super.setOnMatrixChangeListener(null);
		super.setImageBitmap(bm);
		// Set the MatrixChangeListener again
		super.setOnMatrixChangeListener(mMyMatrixChangedListener);
		if (valid) {
			restoreState(state);
		}
	}
	
	public interface OnZoomChangeListener {
		public void onZoomChange(boolean isZoomed);
	}
}
