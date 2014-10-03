package com.eTilbudsavis.etasdk.photoview;

import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class PhotoViewHelper {
	
	private PhotoViewHelper() {
		// no constructor
	}

    public static void checkZoomLevels(float minZoom, float maxZoom) {
        if (minZoom >= maxZoom) {
            throw new IllegalArgumentException("minZoom has to be less than MaxZoom");
        }
    }

    /**
     * @return true if the ImageView exists, and it's Drawable existss
     */
    public static boolean hasDrawable(ImageView imageView) {
        return null != imageView && null != imageView.getDrawable();
    }

    /**
     * @return true if the ScaleType is supported.
     */
    public static boolean isSupportedScaleType(final ScaleType scaleType) {
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

    public static int getImageViewWidth(ImageView imageView) {
        if (null == imageView)
            return 0;
        return imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
    }
    
    public static int getImageViewHeight(ImageView imageView) {
        if (null == imageView)
            return 0;
        return imageView.getHeight() - imageView.getPaddingTop() - imageView.getPaddingBottom();
    }
    
    /**
     * Set's the ImageView's ScaleType to Matrix.
     */
    public static void setImageViewScaleTypeMatrix(ImageView imageView) {
        /**
         * PhotoView sets it's own ScaleType to Matrix, then diverts all calls
         * setScaleType to this.setScaleType automatically.
         */
        if (null != imageView && !(imageView instanceof IPhotoView)) {
            if (!ScaleType.MATRIX.equals(imageView.getScaleType())) {
                imageView.setScaleType(ScaleType.MATRIX);
            }
        }
    }
    
}
