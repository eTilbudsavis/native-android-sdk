package com.eTilbudsavis.etasdk.photoview;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.View;
import android.widget.ImageView;

public interface IPhotoView {
	
    public static final float DEFAULT_MAX_SCALE = 3.0f;
    public static final float DEFAULT_MIN_SCALE = 1.0f;
    public static final int DEFAULT_ZOOM_DURATION = 200;
    
    /**
     * Returns true if the PhotoView is set to allow zooming of Photos.
     *
     * @return true if the PhotoView allows zooming.
     */
    boolean canZoom();

    /**
     * Gets the Display Rectangle of the currently displayed Drawable. The Rectangle is relative to
     * this View and includes all scaling and translations.
     *
     * @return - RectF of Displayed Drawable
     */
    RectF getDisplayRect();

    /**
     * Sets the Display Matrix of the currently displayed Drawable. The Rectangle is considered
     * relative to this View and includes all scaling and translations.
     *
     * @param finalMatrix target matrix to set PhotoView to
     * @return - true if rectangle was applied successfully
     */
    boolean setDisplayMatrix(Matrix finalMatrix);

    /**
     * Gets the Display Matrix of the currently displayed Drawable. The Rectangle is considered
     * relative to this View and includes all scaling and translations.
     *
     * @return - true if rectangle was applied successfully
     */
    Matrix getDisplayMatrix();

    /**
     * @return The current minimum scale level. What this value represents depends on the current
     * {@link android.widget.ImageView.ScaleType}.
     */
    float getMinimumScale();
    
    /**
     * @return The current maximum scale level. What this value represents depends on the current
     * {@link android.widget.ImageView.ScaleType}.
     */
    float getMaximumScale();

    /**
     * Returns the current scale value
     *
     * @return float - current scale value
     */
    float getScale();

    /**
     * Return the current scale type in use by the ImageView.
     *
     * @return current ImageView.ScaleType
     */
    ImageView.ScaleType getScaleType();

    /**
     * Whether to allow the ImageView's parent to intercept the touch event when the photo is scroll
     * to it's horizontal edge.
     *
     * @param allow whether to allow intercepting by parent element or not
     */
    void setAllowParentInterceptOnEdge(boolean allow);

    /**
     * Sets the minimum scale level. What this value represents depends on the current {@link
     * android.widget.ImageView.ScaleType}.
     *
     * @param minimumScale minimum allowed scale
     */
    void setMinimumScale(float minimumScale);
    
    /**
     * Sets the maximum scale level. What this value represents depends on the current {@link
     * android.widget.ImageView.ScaleType}.
     *
     * @param maximumScale maximum allowed scale preset
     */
    void setMaximumScale(float maximumScale);

    /**
     * Register a callback to be invoked when the Photo displayed by this view is long-pressed.
     *
     * @param listener - Listener to be registered.
     */
    void setOnLongClickListener(View.OnLongClickListener listener);

    /**
     * Register a callback to be invoked when the Matrix has changed for this View. An example would
     * be the user panning or scaling the Photo.
     *
     * @param listener - Listener to be registered.
     */
    void setOnMatrixChangeListener(OnMatrixChangedListener listener);

    /**
     * Register a callback to be invoked when the Photo displayed by this View is tapped with a
     * single tap.
     *
     * @param listener - Listener to be registered.
     */
    void setOnPhotoTapListener(OnPhotoTapListener listener);

    /**
     * Returns a listener to be invoked when the Photo displayed by this View is tapped with a
     * single tap.
     *
     * @return OnPhotoTapListener currently set, may be null
     */
    OnPhotoTapListener getOnPhotoTapListener();

    /**
     * Register a callback to be invoked when the View is tapped with a single tap.
     *
     * @param listener - Listener to be registered.
     */
    void setOnViewTapListener(OnViewTapListener listener);

    /**
     * Enables rotation via PhotoView internal functions.
     *
     * @param rotationDegree - Degree to rotate PhotoView to, should be in range 0 to 360
     */
    void setRotationTo(float rotationDegree);

    /**
     * Enables rotation via PhotoView internal functions.
     *
     * @param rotationDegree - Degree to rotate PhotoView by, should be in range 0 to 360
     */
    void setRotationBy(float rotationDegree);

    /**
     * Returns a callback listener to be invoked when the View is tapped with a single tap.
     *
     * @return OnViewTapListener currently set, may be null
     */
    OnViewTapListener getOnViewTapListener();

    /**
     * Changes the current scale to the specified value.
     *
     * @param scale - Value to scale to
     */
    void setScale(float scale);

    /**
     * Changes the current scale to the specified value.
     *
     * @param scale   - Value to scale to
     * @param animate - Whether to animate the scale
     */
    void setScale(float scale, boolean animate);

    /**
     * Changes the current scale to the specified value, around the given focal point.
     *
     * @param scale   - Value to scale to
     * @param focalX  - X Focus Point
     * @param focalY  - Y Focus Point
     * @param animate - Whether to animate the scale
     */
    void setScale(float scale, float focalX, float focalY, boolean animate);

    /**
     * Controls how the image should be resized or moved to match the size of the ImageView. Any
     * scaling or panning will happen within the confines of this {@link
     * android.widget.ImageView.ScaleType}.
     *
     * @param scaleType - The desired scaling mode.
     */
    void setScaleType(ImageView.ScaleType scaleType);

    /**
     * Allows you to enable/disable the zoom functionality on the ImageView. When disable the
     * ImageView reverts to using the FIT_CENTER matrix.
     *
     * @param zoomable - Whether the zoom functionality is enabled.
     */
    void setZoomable(boolean zoomable);

    /**
     * Extracts currently visible area to Bitmap object, if there is no image loaded yet or the
     * ImageView is already destroyed, returns {@code null}
     *
     * @return currently visible area as bitmap or null
     */
    Bitmap getVisibleRectangleBitmap();

    /**
     * Allows to change zoom transition speed, default value is 200 (DEFAULT_ZOOM_DURATION).
     * Will default to 200 if provided negative value
     *
     * @param milliseconds duration of zoom interpolation
     */
    void setZoomTransitionDuration(int milliseconds);

    /**
     * Sets custom double tap listener, to intercept default given functions. To reset behavior to
     * default, you can just pass in "null" or public field of defaultOnDoubleTapListener
     *
     * @param newOnDoubleTapListener custom OnDoubleTapListener to be set on ImageView
     */
    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener newOnDoubleTapListener);
    

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
    
    
}
