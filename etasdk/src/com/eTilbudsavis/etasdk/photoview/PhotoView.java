/*******************************************************************************
 * Most of the code in this package (com.eTilbudsavis.etasdk.photoview) is the
 * creation of Chris Banes. Orig source: https://github.com/chrisbanes/PhotoView.
 * 
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.eTilbudsavis.etasdk.photoview;

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import com.eTilbudsavis.etasdk.Log.EtaLog;
import com.eTilbudsavis.etasdk.photoview.gestures.OnGestureListener;
import com.eTilbudsavis.etasdk.photoview.gestures.VersionedGestureDetector;
import com.eTilbudsavis.etasdk.photoview.scrollerproxy.ScrollerProxy;

public class PhotoView extends ImageView implements View.OnTouchListener, OnGestureListener, ViewTreeObserver.OnGlobalLayoutListener {

	public static final String TAG = PhotoView.class.getSimpleName();
	
	private static final boolean DEBUG = false;
	
	public static final float DEFAULT_MAX_SCALE = 3.0f;
	public static final float DEFAULT_MIN_SCALE = 1.0f;
	public static final int DEFAULT_ZOOM_DURATION = 200;

	static final Interpolator sInterpolator = new AccelerateDecelerateInterpolator();
	private int mZoomDuration = DEFAULT_ZOOM_DURATION;

	static final int EDGE_NONE = -1;
	static final int EDGE_LEFT = 0;
	static final int EDGE_RIGHT = 1;
	static final int EDGE_BOTH = 2;

	private float mMinScale = DEFAULT_MIN_SCALE;
	private float mMaxScale = DEFAULT_MAX_SCALE;

	private boolean mAllowParentInterceptOnEdge = true;

	// Gesture Detectors
	private GestureDetector mGestureDetector;
	private com.eTilbudsavis.etasdk.photoview.gestures.GestureDetector mScaleDragDetector;

	// These are set so we don't keep allocating them on the heap
	private final Matrix mBaseMatrix = new Matrix();
	private final Matrix mDrawMatrix = new Matrix();
	private final Matrix mSuppMatrix = new Matrix();
	private final RectF mDisplayRect = new RectF();
	private final float[] mMatrixValues = new float[9];

	// Listeners
	private OnMatrixChangedListener mMatrixChangeListener;
	private OnPhotoTapListener mPhotoTapListener;
	private OnViewTapListener mViewTapListener;
	private OnLongClickListener mLongClickListener;

	private int mIvTop, mIvRight, mIvBottom, mIvLeft;
	private FlingRunnable mCurrentFlingRunnable;
	private int mScrollEdge = EDGE_BOTH;

	private boolean mZoomEnabled = true;
	private ScaleType mScaleType = ScaleType.FIT_CENTER;

	public PhotoView(Context context) {
		super(context);
		init();
	}

	public PhotoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PhotoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		setDrawingCacheEnabled(true);
		setOnTouchListener(this);

		ViewTreeObserver observer = getViewTreeObserver();
		if (null != observer)
			observer.addOnGlobalLayoutListener(this);

		// Make sure we using MATRIX Scale Type
		setImageViewScaleTypeMatrix();

		if (isInEditMode()) {
			return;
		}

		// Create Gesture Detectors...
		mScaleDragDetector = VersionedGestureDetector.newInstance(getContext(), this);

		mGestureDetector = new GestureDetector(getContext(),
				new GestureDetector.SimpleOnGestureListener() {

			// forward long click listener
			@Override
			public void onLongPress(MotionEvent e) {
				if (null != mLongClickListener) {
					mLongClickListener.onLongClick(PhotoView.this);
				}
			}
		});

		mGestureDetector.setOnDoubleTapListener(new DefaultOnDoubleTapListener(this));
		
		setOnDoubleTapListener(new DefaultOnDoubleTapListener(this));
		
		// Finally, update the UI so that we're zoomable
		setZoomable(true);
	}

	/**
	 * Returns true if the PhotoView is set to allow zooming of Photos.
	 *
	 * @return true if the PhotoView allows zooming.
	 */
	public boolean canZoom() {
		return mZoomEnabled;
	}

	/**
	 * Gets the Display Rectangle of the currently displayed Drawable. The Rectangle is relative to
	 * this View and includes all scaling and translations.
	 *
	 * @return - RectF of Displayed Drawable
	 */
	public RectF getDisplayRect() {
		checkMatrixBounds();
		return getDisplayRect(getDrawMatrix());
	}

	/**
	 * Sets the Display Matrix of the currently displayed Drawable. The Rectangle is considered
	 * relative to this View and includes all scaling and translations.
	 *
	 * @param finalMatrix target matrix to set PhotoView to
	 * @return - true if rectangle was applied successfully
	 */
	public boolean setDisplayMatrix(Matrix finalMatrix) {
		if (finalMatrix == null)
			throw new IllegalArgumentException("Matrix cannot be null");

		if (null == getDrawable())
			return false;

		mSuppMatrix.set(finalMatrix);
		setImageViewMatrix(getDrawMatrix());
		checkMatrixBounds();

		return true;
	}

	/**
	 * Gets the Display Matrix of the currently displayed Drawable. The Rectangle is considered
	 * relative to this View and includes all scaling and translations.
	 *
	 * @return - true if rectangle was applied successfully
	 */
	public Matrix getDisplayMatrix() {
		return new Matrix(getDrawMatrix());
	}

	/**
	 * @return The current minimum scale level. What this value represents depends on the current
	 * {@link android.widget.ImageView.ScaleType}.
	 */
	public float getMinimumScale() {
		return mMinScale;
	}

	/**
	 * @return The current maximum scale level. What this value represents depends on the current
	 * {@link android.widget.ImageView.ScaleType}.
	 */
	public float getMaximumScale() {
		return mMaxScale;
	}

	/**
	 * Returns the current scale value
	 *
	 * @return float - current scale value
	 */
	public float getScale() {
		return FloatMath.sqrt((float) Math.pow(getValue(mSuppMatrix, Matrix.MSCALE_X), 2) + (float) Math.pow(getValue(mSuppMatrix, Matrix.MSKEW_Y), 2));
	}

	/**
	 * Return the current scale type in use by the ImageView.
     *
	 * @return current ImageView.ScaleType
	 */
	public ScaleType getScaleType() {
		return mScaleType;
	}

	/**
	 * Whether to allow the ImageView's parent to intercept the touch event when the photo is scroll
	 * to it's horizontal edge.
	 *
	 * @param allow whether to allow intercepting by parent element or not
	 */
	public void setAllowParentInterceptOnEdge(boolean allow) {
		mAllowParentInterceptOnEdge = allow;
	}

	/**
	 * Sets the minimum scale level. What this value represents depends on the current {@link
	 * android.widget.ImageView.ScaleType}.
	 *
	 * @param minimumScale minimum allowed scale
	 */
	public void setMinimumScale(float minimumScale) {
		checkZoomLevels(minimumScale, mMaxScale);
		mMinScale = minimumScale;
	}

	/**
	 * Sets the maximum scale level. What this value represents depends on the current {@link
	 * android.widget.ImageView.ScaleType}.
	 *
	 * @param maximumScale maximum allowed scale preset
	 */
	public void setMaximumScale(float maximumScale) {
		checkZoomLevels(mMinScale, maximumScale);
		mMaxScale = maximumScale;
	}

	/**
	 * Register a callback to be invoked when the Photo displayed by this view is long-pressed.
	 *
	 * @param listener - Listener to be registered.
	 */
	public void setOnLongClickListener(OnLongClickListener listener) {
		mLongClickListener = listener;
	}

	/**
	 * Register a callback to be invoked when the Matrix has changed for this View. An example would
	 * be the user panning or scaling the Photo.
	 *
	 * @param listener - Listener to be registered.
	 */
	public void setOnMatrixChangeListener(OnMatrixChangedListener listener) {
		mMatrixChangeListener = listener;
	}

	/**
	 * Register a callback to be invoked when the Photo displayed by this View is tapped with a
	 * single tap.
	 *
	 * @param listener - Listener to be registered.
	 */
	public void setOnPhotoTapListener(OnPhotoTapListener listener) {
		mPhotoTapListener = listener;
	}

	/**
	 * Returns a listener to be invoked when the Photo displayed by this View is tapped with a
	 * single tap.
	 *
	 * @return OnPhotoTapListener currently set, may be null
	 */
	public OnPhotoTapListener getOnPhotoTapListener() {
		return mPhotoTapListener;
	}

	/**
	 * Register a callback to be invoked when the View is tapped with a single tap.
	 *
	 * @param listener - Listener to be registered.
	 */
	public void setOnViewTapListener(OnViewTapListener listener) {
		mViewTapListener = listener;
	}

	/**
	 * Returns a callback listener to be invoked when the View is tapped with a single tap.
	 *
	 * @return OnViewTapListener currently set, may be null
	 */
	public OnViewTapListener getOnViewTapListener() {
		return mViewTapListener;
	}

	/**
	 * Enables rotation via PhotoView internal functions.
	 *
	 * @param rotationDegree - Degree to rotate PhotoView to, should be in range 0 to 360
	 */
	public void setRotationTo(float rotationDegree) {
		mSuppMatrix.setRotate(rotationDegree % 360);
		checkAndDisplayMatrix();
	}

	/**
	 * Enables rotation via PhotoView internal functions.
	 *
	 * @param rotationDegree - Degree to rotate PhotoView by, should be in range 0 to 360
	 */
	public void setRotationBy(float rotationDegree) {
		mSuppMatrix.postRotate(rotationDegree % 360);
		checkAndDisplayMatrix();
	}

	/**
	 * Changes the current scale to the specified value.
	 *
	 * @param scale - Value to scale to
	 */
	public void setScale(float scale) {
		setScale(scale, false);
	}

	/**
	 * Changes the current scale to the specified value.
	 *
	 * @param scale   - Value to scale to
	 * @param animate - Whether to animate the scale
	 */
	public void setScale(float scale, boolean animate) {
		setScale(scale, (getRight() / 2), (getBottom() / 2), animate);
	}

	/**
	 * Changes the current scale to the specified value, around the given focal point.
	 *
	 * @param scale   - Value to scale to
	 * @param focalX  - X Focus Point
	 * @param focalY  - Y Focus Point
	 * @param animate - Whether to animate the scale
	 */
	public void setScale(float scale, float focalX, float focalY, boolean animate) {
		// Check to see if the scale is within bounds
		if (scale < mMinScale || scale > mMaxScale) {
			EtaLog.i(TAG, "Scale must be within the range of minScale and maxScale");
			return;
		}

		if (animate) {
			post(new AnimatedZoomRunnable(getScale(), scale, focalX, focalY));
		} else {
			mSuppMatrix.setScale(scale, scale, focalX, focalY);
			checkAndDisplayMatrix();
		}
	}

	/**
	 * Controls how the image should be resized or moved to match the size of the ImageView. Any
	 * scaling or panning will happen within the confines of this {@link
	 * android.widget.ImageView.ScaleType}.
	 *
	 * @param scaleType - The desired scaling mode.
	 */
	public void setScaleType(ScaleType scaleType) {
		if (isSupportedScaleType(scaleType) && scaleType != mScaleType) {
			mScaleType = scaleType;

			// Finally update
			update();
		}
	}

	/**
	 * Allows you to enable/disable the zoom functionality on the ImageView. When disable the
	 * ImageView reverts to using the FIT_CENTER matrix.
	 *
	 * @param zoomable - Whether the zoom functionality is enabled.
	 */
	public void setZoomable(boolean zoomable) {
		mZoomEnabled = zoomable;
		update();
	}

	/**
	 * Extracts currently visible area to Bitmap object, if there is no image loaded yet or the
	 * ImageView is already destroyed, returns {@code null}
	 *
	 * @return currently visible area as bitmap or null
	 */
	public Bitmap getVisibleRectangleBitmap() {
		return getDrawingCache();
	}

	/**
	 * Allows to change zoom transition speed, default value is 200 (DEFAULT_ZOOM_DURATION).
	 * Will default to 200 if provided negative value
	 *
	 * @param milliseconds duration of zoom interpolation
	 */
	public void setZoomTransitionDuration(int milliseconds) {
		mZoomDuration = (milliseconds < 0 ? DEFAULT_ZOOM_DURATION : milliseconds);
	}

	/**
	 * Sets custom double tap listener, to intercept default given functions. To reset behavior to
	 * default, you can just pass in "null" or public field of defaultOnDoubleTapListener
	 *
	 * @param newOnDoubleTapListener custom OnDoubleTapListener to be set on ImageView
	 */
	public void setOnDoubleTapListener(OnDoubleTapListener newOnDoubleTapListener) {
		if (newOnDoubleTapListener != null) {
			mGestureDetector.setOnDoubleTapListener(newOnDoubleTapListener);
		}
	}

	//TODO: HELPERS

	private void cleanup() {

		// Remove this as a global layout listener
		ViewTreeObserver observer = getViewTreeObserver();
		if (null != observer && observer.isAlive()) {
			observer.removeGlobalOnLayoutListener(this);
		}

		// Remove the ImageView's reference to this
		setOnTouchListener(null);

		// make sure a pending fling runnable won't be run
		cancelFling();

		if (null != mGestureDetector) {
			mGestureDetector.setOnDoubleTapListener(null);
		}

		// Clear listeners too
		mMatrixChangeListener = null;
		mPhotoTapListener = null;
		mViewTapListener = null;

	}

	private void update() {

		if (mZoomEnabled) {
			// Make sure we using MATRIX Scale Type
			setImageViewScaleTypeMatrix();

			// Update the base matrix using the current drawable
			updateBaseMatrix(getDrawable());
		} else {
			// Reset the Matrix...
			resetMatrix();
		}

	}

	public Matrix getDrawMatrix() {
		mDrawMatrix.set(mBaseMatrix);
		mDrawMatrix.postConcat(mSuppMatrix);
		return mDrawMatrix;
	}

	private void cancelFling() {
		if (null != mCurrentFlingRunnable) {
			mCurrentFlingRunnable.cancelFling();
			mCurrentFlingRunnable = null;
		}
	}

	/**
	 * Helper method that 'unpacks' a Matrix and returns the required value
	 *
	 * @param matrix     - Matrix to unpack
	 * @param whichValue - Which value from Matrix.M* to return
	 * @return float - returned value
	 */
	private float getValue(Matrix matrix, int whichValue) {
		matrix.getValues(mMatrixValues);
		return mMatrixValues[whichValue];
	}

	/**
	 * Resets the Matrix back to FIT_CENTER, and then displays it.s
	 */
	private void resetMatrix() {
		mSuppMatrix.reset();
		setImageViewMatrix(getDrawMatrix());
		checkMatrixBounds();
	}

	private void setImageViewMatrix(Matrix matrix) {
		setImageMatrix(matrix);
		
		// Call MatrixChangedListener if needed
		if (null != mMatrixChangeListener) {
			RectF displayRect = getDisplayRect(matrix);
			if (null != displayRect) {
				mMatrixChangeListener.onMatrixChanged(displayRect);
			}
		}
	}
	
	/**
	 * Calculate Matrix for FIT_CENTER
	 *
	 * @param d - Drawable being displayed
	 */
	private void updateBaseMatrix(Drawable d) {
		
		if (d==null) {
			return;
		}
		
		final float viewWidth = getImageViewWidth();
		final float viewHeight = getImageViewHeight();
		final int drawableWidth = d.getIntrinsicWidth();
		final int drawableHeight = d.getIntrinsicHeight();

		mBaseMatrix.reset();

		final float widthScale = viewWidth / drawableWidth;
		final float heightScale = viewHeight / drawableHeight;

		if (mScaleType == ScaleType.CENTER) {
			mBaseMatrix.postTranslate((viewWidth - drawableWidth) / 2F,
					(viewHeight - drawableHeight) / 2F);

		} else if (mScaleType == ScaleType.CENTER_CROP) {
			float scale = Math.max(widthScale, heightScale);
			mBaseMatrix.postScale(scale, scale);
			mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F,
					(viewHeight - drawableHeight * scale) / 2F);

		} else if (mScaleType == ScaleType.CENTER_INSIDE) {
			float scale = Math.min(1.0f, Math.min(widthScale, heightScale));
			mBaseMatrix.postScale(scale, scale);
			mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F,
					(viewHeight - drawableHeight * scale) / 2F);

		} else {
			RectF mTempSrc = new RectF(0, 0, drawableWidth, drawableHeight);
			RectF mTempDst = new RectF(0, 0, viewWidth, viewHeight);

			switch (mScaleType) {
			case FIT_CENTER:
				mBaseMatrix
				.setRectToRect(mTempSrc, mTempDst, ScaleToFit.CENTER);
				break;

			case FIT_START:
				mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.START);
				break;

			case FIT_END:
				mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.END);
				break;

			case FIT_XY:
				mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.FILL);
				break;

			default:
				break;
			}
		}

		resetMatrix();
	}

	/**
	 * Helper method that simply checks the Matrix, and then displays the result
	 */
	private void checkAndDisplayMatrix() {
		if (checkMatrixBounds()) {
			setImageViewMatrix(getDrawMatrix());
		}
	}

	private boolean checkMatrixBounds() {

		final RectF rect = getDisplayRect(getDrawMatrix());
		if (null == rect) {
			return false;
		}

		final float height = rect.height(), width = rect.width();
		float deltaX = 0, deltaY = 0;

		final int viewHeight = getImageViewHeight();
		if (height <= viewHeight) {
			switch (mScaleType) {
			case FIT_START:
				deltaY = -rect.top;
				break;
			case FIT_END:
				deltaY = viewHeight - height - rect.top;
				break;
			default:
				deltaY = (viewHeight - height) / 2 - rect.top;
				break;
			}
		} else if (rect.top > 0) {
			deltaY = -rect.top;
		} else if (rect.bottom < viewHeight) {
			deltaY = viewHeight - rect.bottom;
		}

		final int viewWidth = getImageViewWidth();
		if (width <= viewWidth) {
			switch (mScaleType) {
			case FIT_START:
				deltaX = -rect.left;
				break;
			case FIT_END:
				deltaX = viewWidth - width - rect.left;
				break;
			default:
				deltaX = (viewWidth - width) / 2 - rect.left;
				break;
			}
			mScrollEdge = EDGE_BOTH;
		} else if (rect.left > 0) {
			mScrollEdge = EDGE_LEFT;
			deltaX = -rect.left;
		} else if (rect.right < viewWidth) {
			deltaX = viewWidth - rect.right;
			mScrollEdge = EDGE_RIGHT;
		} else {
			mScrollEdge = EDGE_NONE;
		}

		// Finally actually translate the matrix
		mSuppMatrix.postTranslate(deltaX, deltaY);
		return true;
	}

	/**
	 * Helper method that maps the supplied Matrix to the current Drawable
	 *
	 * @param matrix - Matrix to map Drawable against
	 * @return RectF - Displayed Rectangle
	 */
	private RectF getDisplayRect(Matrix matrix) {

		Drawable d = getDrawable();
		if (null != d) {
			mDisplayRect.set(0, 0, d.getIntrinsicWidth(),
					d.getIntrinsicHeight());
			matrix.mapRect(mDisplayRect);
			return mDisplayRect;
		}

		return null;
	}

	//TODO: STATIC HELPERS

	private void checkZoomLevels(float minZoom, float maxZoom) {
		if (minZoom >= maxZoom) {
			throw new IllegalArgumentException("minZoom has to be less than MaxZoom");
		}
	}

	/**
	 * @return true if the ScaleType is supported.
	 */
	private boolean isSupportedScaleType(final ScaleType scaleType) {
		if (null == scaleType) {
			return false;
		}
		
		switch (scaleType) {
		case MATRIX:
			throw new IllegalArgumentException(scaleType.name()
					+ " is not supported in PhotoView");

		default:
			return true;
		}
	}

	private int getImageViewWidth() {
		return getWidth() - getPaddingLeft() - getPaddingRight();
	}

	private int getImageViewHeight() {
		return getHeight() - getPaddingTop() - getPaddingBottom();
	}

	/**
	 * Set's the ImageView's ScaleType to Matrix.
	 */
	private void setImageViewScaleTypeMatrix() {/**
	 * PhotoView sets it's own ScaleType to Matrix, then diverts all calls
	 * setScaleType to this.setScaleType automatically.
	 */
		if (!ScaleType.MATRIX.equals(getScaleType())) {
			super.setScaleType(ScaleType.MATRIX);
		}
	}

	private class AnimatedZoomRunnable implements Runnable {

		private final float mFocalX, mFocalY;
		private final long mStartTime;
		private final float mZoomStart, mZoomEnd;

		public AnimatedZoomRunnable(final float currentZoom, final float targetZoom,
				final float focalX, final float focalY) {
			mFocalX = focalX;
			mFocalY = focalY;
			mStartTime = System.currentTimeMillis();
			mZoomStart = currentZoom;
			mZoomEnd = targetZoom;
		}

		public void run() {

			float t = interpolate();
			float scale = mZoomStart + t * (mZoomEnd - mZoomStart);
			float deltaScale = scale / getScale();

			mSuppMatrix.postScale(deltaScale, deltaScale, mFocalX, mFocalY);
			checkAndDisplayMatrix();

			// We haven't hit our target scale yet, so post ourselves again
			if (t < 1f) {
				Compat.postOnAnimation(PhotoView.this, this);
			}

		}

		private float interpolate() {
			float t = 1f * (System.currentTimeMillis() - mStartTime) / mZoomDuration;
			t = Math.min(1f, t);
			t = sInterpolator.getInterpolation(t);
			return t;
		}
	}

	private class FlingRunnable implements Runnable {

		private final ScrollerProxy mScroller;
		private int mCurrentX, mCurrentY;

		public FlingRunnable(Context context) {
			mScroller = ScrollerProxy.getScroller(context);
		}

		public void cancelFling() {
			log(TAG, "Cancel Fling");
			mScroller.forceFinished(true);
		}
		
		public void fling(int viewWidth, int viewHeight, int velocityX,
				int velocityY) {
			final RectF rect = getDisplayRect();
			if (null == rect) {
				return;
			}

			final int startX = Math.round(-rect.left);
			final int minX, maxX, minY, maxY;

			if (viewWidth < rect.width()) {
				minX = 0;
				maxX = Math.round(rect.width() - viewWidth);
			} else {
				minX = maxX = startX;
			}
			
			final int startY = Math.round(-rect.top);
			if (viewHeight < rect.height()) {
				minY = 0;
				maxY = Math.round(rect.height() - viewHeight);
			} else {
				minY = maxY = startY;
			}
			
			mCurrentX = startX;
			mCurrentY = startY;
			
			log(TAG, "fling. StartX:" + startX + " StartY:" + startY + " MaxX:" + maxX + " MaxY:" + maxY);
			
			// If we actually can move, fling the scroller
			if (startX != maxX || startY != maxY) {
				mScroller.fling(startX, startY, velocityX, velocityY, minX,
						maxX, minY, maxY, 0, 0);
			}
		}

		public void run() {
			if (mScroller.isFinished()) {
				return; // remaining post that should not be handled
			}

			if (mScroller.computeScrollOffset()) {

				final int newX = mScroller.getCurrX();
				final int newY = mScroller.getCurrY();

				log(TAG, "fling run(). CurrentX:" + mCurrentX + " CurrentY:"
						+ mCurrentY + " NewX:" + newX + " NewY:"
						+ newY);


				mSuppMatrix.postTranslate(mCurrentX - newX, mCurrentY - newY);
				setImageViewMatrix(getDrawMatrix());

				mCurrentX = newX;
				mCurrentY = newY;

				// Post On animation
				Compat.postOnAnimation(PhotoView.this, this);
			}
		}
	}
	//TODO: Override methods

	@Override
	protected void onDetachedFromWindow() {
		cleanup();
		super.onDetachedFromWindow();
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
		update();
	}

	@Override
	public void setImageResource(int resId) {
		super.setImageResource(resId);
		update();
	}

	@Override
	public void setImageURI(Uri uri) {
		super.setImageURI(uri);
		update();
	}

	// TODO interface implementations

	public void onGlobalLayout() {
		if (mZoomEnabled) {
			final int top = getTop();
			final int right = getRight();
			final int bottom = getBottom();
			final int left = getLeft();

			/**
			 * We need to check whether the ImageView's bounds have changed.
			 * This would be easier if we targeted API 11+ as we could just use
			 * View.OnLayoutChangeListener. Instead we have to replicate the
			 * work, keeping track of the ImageView's bounds and then checking
			 * if the values change.
			 */
			if (top != mIvTop || bottom != mIvBottom || left != mIvLeft
					|| right != mIvRight) {
				// Update our base matrix, as the bounds have changed
				updateBaseMatrix(getDrawable());

				// Update values as something has changed
				mIvTop = top;
				mIvRight = right;
				mIvBottom = bottom;
				mIvLeft = left;
			}
		} else {
			updateBaseMatrix(getDrawable());
		}
	}

	public void onDrag(float dx, float dy) {
		if (mScaleDragDetector.isScaling()) {
			return; // Do not drag if we are already scaling
		}

		log(TAG, String.format("onDrag: dx: %.2f. dy: %.2f", dx, dy));

		mSuppMatrix.postTranslate(dx, dy);
		checkAndDisplayMatrix();

		/**
		 * Here we decide whether to let the ImageView's parent to start taking
		 * over the touch event.
		 *
		 * First we check whether this function is enabled. We never want the
		 * parent to take over if we're scaling. We then check the edge we're
		 * on, and the direction of the scroll (i.e. if we're pulling against
		 * the edge, aka 'overscrolling', let the parent take over).
		 */
		ViewParent parent = getParent();
		if (mAllowParentInterceptOnEdge && !mScaleDragDetector.isScaling()) {
			if (mScrollEdge == EDGE_BOTH
					|| (mScrollEdge == EDGE_LEFT && dx >= 1f)
					|| (mScrollEdge == EDGE_RIGHT && dx <= -1f)) {
				if (null != parent)
					parent.requestDisallowInterceptTouchEvent(false);
			}
		} else {
			if (null != parent) {
				parent.requestDisallowInterceptTouchEvent(true);
			}
		}
	}

	public void onFling(float startX, float startY, float velocityX,
			float velocityY) {
		log(TAG, "onFling. sX: " + startX + " sY: " + startY + " Vx: " + velocityX + " Vy: " + velocityY);

		mCurrentFlingRunnable = new FlingRunnable(getContext());
		mCurrentFlingRunnable.fling(getImageViewWidth(),
				getImageViewHeight(), (int) velocityX, (int) velocityY);
		post(mCurrentFlingRunnable);
	}

	public void onScale(float scaleFactor, float focusX, float focusY) {
		log(TAG, String.format("onScale: scale: %.2f. fX: %.2f. fY: %.2f", scaleFactor, focusX, focusY));

		if (getScale() < mMaxScale || scaleFactor < 1f) {
			mSuppMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
			checkAndDisplayMatrix();
		}
	}

	/**
	 * @return true if the ImageView exists, and it's Drawable existss
	 */
	public static boolean hasDrawable(ImageView imageView) {
		return null != imageView && null != imageView.getDrawable();
	}

	public boolean onTouch(View v, MotionEvent event) {
		boolean handled = false;
		
		if (mZoomEnabled && hasDrawable((ImageView)v)) {
			ViewParent parent = v.getParent();
			switch (event.getAction()) {
			case ACTION_DOWN:
				
				// First, disable the Parent from intercepting the touch
				// event
				if (null != parent)
					parent.requestDisallowInterceptTouchEvent(true);
				else
					EtaLog.i(TAG, "onTouch getParent() returned null");

				// If we're flinging, and the user presses down, cancel
				// fling
				cancelFling();
				break;

			case ACTION_CANCEL:
			case ACTION_UP:
				
				// If the user has zoomed less than min scale, zoom back
				// to min scale
				if (getScale() < mMinScale) {
					RectF rect = getDisplayRect();
					if (null != rect) {
						v.post(new AnimatedZoomRunnable(getScale(), mMinScale,
								rect.centerX(), rect.centerY()));
						handled = true;
					}
				}
				break;
			}
			
			// Try the Scale/Drag detector
			if (null != mScaleDragDetector && mScaleDragDetector.onTouchEvent(event)) {
				handled = true;
			}
			
			// Check to see if the user double tapped
			if (null != mGestureDetector && mGestureDetector.onTouchEvent(event)) {
				handled = true;
			}
		}
		
		return handled;
	}

	// Interfaces

	/**
	 * Interface definition for a callback to be invoked when the internal Matrix has changed for
	 * this View.
	 *
	 * @author Chris Banes
	 */
	public static interface OnMatrixChangedListener {
		/**
		 * Callback for when the Matrix displaying the Drawable has changed. This could be because
		 * the View's bounds have changed, or the user has zoomed.
		 *
		 * @param rect - Rectangle displaying the Drawable's new bounds.
		 */
		void onMatrixChanged(RectF rect);
	}

	/**
	 * Interface definition for a callback to be invoked when the Photo is tapped with a single
	 * tap.
	 *
	 * @author Chris Banes
	 */
	public static interface OnPhotoTapListener {

		/**
		 * A callback to receive where the user taps on a photo. You will only receive a callback if
		 * the user taps on the actual photo, tapping on 'whitespace' will be ignored.
		 *
		 * @param view - View the user tapped.
		 * @param x    - where the user tapped from the of the Drawable, as percentage of the
		 *             Drawable width.
		 * @param y    - where the user tapped from the top of the Drawable, as percentage of the
		 *             Drawable height.
		 */
		void onPhotoTap(View view, float x, float y);
	}

	/**
	 * Interface definition for a callback to be invoked when the ImageView is tapped with a single
	 * tap.
	 *
	 * @author Chris Banes
	 */
	public static interface OnViewTapListener {

		/**
		 * A callback to receive where the user taps on a ImageView. You will receive a callback if
		 * the user taps anywhere on the view, tapping on 'whitespace' will not be ignored.
		 *
		 * @param view - View the user tapped.
		 * @param x    - where the user tapped from the left of the View.
		 * @param y    - where the user tapped from the top of the View.
		 */
		void onViewTap(View view, float x, float y);
	}
	
	private void log(String tag, String msg) {
		if (DEBUG)
			EtaLog.d(tag, msg);
	}
}
