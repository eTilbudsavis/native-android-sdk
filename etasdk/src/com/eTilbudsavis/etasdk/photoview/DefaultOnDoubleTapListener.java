package com.eTilbudsavis.etasdk.photoview;

import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Provided default implementation of GestureDetector.OnDoubleTapListener, to be overriden with custom behavior, if needed
 * <p>&nbsp;</p>
 * To be used via {@link uk.co.senab.photoview.PhotoViewAttacher#setOnDoubleTapListener(android.view.GestureDetector.OnDoubleTapListener)}
 */
public class DefaultOnDoubleTapListener implements GestureDetector.OnDoubleTapListener {
	
	public static final String TAG = DefaultOnDoubleTapListener.class.getSimpleName();
	private static final float EPSILON = 0.001f;
	
    private PhotoView mPhotoView;

    /**
     * Default constructor
     *
     * @param photoViewAttacher PhotoViewAttacher to bind to
     */
    public DefaultOnDoubleTapListener(PhotoView photoView) {
    	setPhotoView(photoView);
    }
    
    /**
     * Allows to change PhotoViewAttacher within range of single instance
     *
     * @param newPhotoViewAttacher PhotoViewAttacher to bind to
     */
    public void setPhotoView(PhotoView photoView) {
        mPhotoView = photoView;
    }
    
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (mPhotoView == null)
            return false;
        
        if (null != mPhotoView.getOnPhotoTapListener()) {
            final RectF displayRect = mPhotoView.getDisplayRect();

            if (null != displayRect) {
                final float x = e.getX(), y = e.getY();

                // Check to see if the user tapped on the photo
                if (displayRect.contains(x, y)) {

                    float xResult = (x - displayRect.left)
                            / displayRect.width();
                    float yResult = (y - displayRect.top)
                            / displayRect.height();

                    mPhotoView.getOnPhotoTapListener().onPhotoTap(mPhotoView, xResult, yResult);
                    return true;
                }
            }
        }
        if (null != mPhotoView.getOnViewTapListener()) {
        	mPhotoView.getOnViewTapListener().onViewTap(mPhotoView, e.getX(), e.getY());
        }
        
        return false;
    }
    
    public boolean onDoubleTap(MotionEvent ev) {
        if (mPhotoView == null) {
            return false;
        }
        
        try {
            float scale = mPhotoView.getScale();
            float min = mPhotoView.getMinimumScale();
            float max = mPhotoView.getMaximumScale();
            float x = ev.getX();
            float y = ev.getY();
            
            float midScale = min+((max-min)/2);
            if ( scale < midScale ) {
            	mPhotoView.setScale(max, x, y, true);
            } else {
            	mPhotoView.setScale(min, x, y, true);
            }

//            if ( almostEqual(min, scale) || ( scale > midScale && !almostEqual(max, scale)) ) {
//            	mPhotoView.setScale(max, x, y, true);
//            } else {
//            	mPhotoView.setScale(min, x, y, true);
//            }
            
        } catch (ArrayIndexOutOfBoundsException e) {
            // Can sometimes happen when getX() and getY() is called
        }

        return true;
    }

    public static boolean almostEqual(double a, double b){
    	return Math.abs(a-b)<EPSILON;
    }

    public static boolean almostEqual(double a, double b, double eps){
    	return Math.abs(a-b)<eps;
    }
    
    public boolean onDoubleTapEvent(MotionEvent e) {
        // Wait for the confirmed onDoubleTap() instead
        return false;
    }

}
