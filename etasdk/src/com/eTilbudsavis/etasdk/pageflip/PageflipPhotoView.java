package com.eTilbudsavis.etasdk.pageflip;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.AttributeSet;

import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.photoview.PhotoView;

public class PageflipPhotoView extends PhotoView {
	
	private static final String STATE_DISPLAY_MATRIX = "state_display_matrix";

	private boolean mZoomed = false;
	
	private OnZoomChangeListener mZoomChangeListener;
	private OnMatrixChangedListener mMatrixChangedListener;
	private OnMatrixChangedListener mMyMatrixChangedListener = new OnMatrixChangedListener() {
		
		public void onMatrixChanged(RectF rect) {
//			EtaLog.d(TAG, "onMatrixChanged");
			boolean isMinScale = PageflipUtils.almost(getScale(), getMinimumScale(), 0.001f);
			setAllowParentInterceptOnEdge(isMinScale);
			if (!isAnimating()) {
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
		mZoomed = true;
		EtaLog.d(TAG, "isZoom:"+isZoomed);
		if (mZoomChangeListener!=null) {
			mZoomChangeListener.onZoomChange(isZoomed);
		}
	}
	
	public PageflipPhotoView(Context context) {
		super(context);
		init();
	}

	public PageflipPhotoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PageflipPhotoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		super.setOnMatrixChangeListener(mMyMatrixChangedListener);
	}
	
	@Override
	public void setOnMatrixChangeListener(OnMatrixChangedListener listener) {
		super.setOnMatrixChangeListener(listener);
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
	
	@Override
	public void setImageBitmap(Bitmap bm) {
		Bundle state = null;
		BitmapDrawable d = (BitmapDrawable)getDrawable();
		if (d!=null) {
			Bitmap b = d.getBitmap();
			if (b!=null) {
				state = new Bundle();
				saveState(state);
			}
		}
		// Remove the MatrixChangeListener, as it shouldn't be perceived as a matrix change
		super.setOnMatrixChangeListener(null);
		super.setImageBitmap(bm);
		// Set the MatrixChangeListener again
		super.setOnMatrixChangeListener(mMyMatrixChangedListener);
		restoreState(state);
	}
	
	public interface OnZoomChangeListener {
		public void onZoomChange(boolean isZoomed);
	}
}
