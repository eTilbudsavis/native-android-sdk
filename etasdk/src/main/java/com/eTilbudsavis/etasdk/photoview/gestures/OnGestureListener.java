/**
 * ****************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */
package com.eTilbudsavis.etasdk.photoview.gestures;

import android.view.MotionEvent;

import com.eTilbudsavis.etasdk.photoview.PhotoView;

public interface OnGestureListener {

    public void onDrag(float dx, float dy);

    public void onFling(float startX, float startY, float velocityX, float velocityY);

    public void onScale(float scaleFactor, float focusX, float focusY);

    public boolean onSingleTab(MotionEvent e);

    public boolean onDoubleTab(MotionEvent e);

    public boolean onLongPress(MotionEvent e);

    /**
     * A convenience class to extend when you only want to listen for a subset of all the gestures.
     * This implements all methods in the {@link OnGestureListener}, and by default calls the method in the
     * {@link PhotoView} if these aren't overridden.
     *
     * @author Danny Hvam
     */
    public class SimpleOnGestureListener implements OnGestureListener {

        private PhotoView mPhotoview;

        public SimpleOnGestureListener(PhotoView photoView) {
            setPhotoView(photoView);
        }

        public PhotoView getPhotoView() {
            return mPhotoview;
        }

        public void setPhotoView(PhotoView photoView) {
            mPhotoview = photoView;
        }

        public void onDrag(float dx, float dy) {
            mPhotoview.onDrag(dx, dy);
        }

        public void onFling(float startX, float startY, float velocityX, float velocityY) {
            mPhotoview.onFling(startX, startY, velocityX, velocityY);
        }

        public void onScale(float scaleFactor, float focusX, float focusY) {
            mPhotoview.onScale(scaleFactor, focusX, focusY);
        }

        public boolean onSingleTab(MotionEvent e) {
            return mPhotoview.onSingleTab(e);
        }

        public boolean onDoubleTab(MotionEvent e) {
            return mPhotoview.onDoubleTab(e);
        }

        public boolean onLongPress(MotionEvent e) {
            return mPhotoview.onLongPress(e);
        }

    }

}