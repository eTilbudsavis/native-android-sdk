/*******************************************************************************
 * Copyright 2015 ShopGun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.shopgun.android.sdk.pageflip.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

import com.shopgun.android.sdk.pageflip.utils.PageflipUtils;
import com.shopgun.android.sdk.photoview.PhotoView;
import com.shopgun.android.sdk.utils.Constants;

public class ZoomPhotoView extends PhotoView implements PhotoView.OnMatrixChangedListener {

    public static final String TAG = Constants.getTag(ZoomPhotoView.class);

    private static final String STATE_DISPLAY_MATRIX = "state_display_matrix";
    private static final float MIN_SCALE_EPSILON = 0.1f;

    private boolean mZoomed = false;
    private OnZoomChangeListener mZoomChangeListener;
    private OnMatrixChangedListener mMatrixChangedListener;

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
        super.setOnMatrixChangeListener(this);
    }

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

        if (mMatrixChangedListener != null) {
            mMatrixChangedListener.onMatrixChanged(rect);
        }
    }

    private void zoomChange(boolean isZoomed) {
        mZoomed = isZoomed;
        if (mZoomChangeListener != null) {
            mZoomChangeListener.onZoomChange(isZoomed);
        }
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

    public Bitmap getBitmap() {
        BitmapDrawable d = (BitmapDrawable) getDrawable();
        if (d != null) {
            return d.getBitmap();
        }
        return null;
    }

    public boolean isBitmapValid() {
        return isBitmapValid(getBitmap());
    }

    private boolean isBitmapValid(Bitmap b) {
        return b != null && !b.isRecycled();
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
        Bitmap b = getBitmap();
        boolean valid = isBitmapValid(b);
        float[] mMatrixValues = new float[9];
        if (valid) {
            getSupportMatrix().getValues(mMatrixValues);
        }
        // Remove the MatrixChangeListener, as it shouldn't be perceived as a matrix change
        super.setOnMatrixChangeListener(null);
        super.setImageBitmap(bm);
        // Set the MatrixChangeListener again
        super.setOnMatrixChangeListener(this);
        if (valid) {
            getSupportMatrix().setValues(mMatrixValues);
            setImageMatrix(getDisplayMatrix());
        }
    }

    public interface OnZoomChangeListener {
        public void onZoomChange(boolean isZoomed);
    }
}
