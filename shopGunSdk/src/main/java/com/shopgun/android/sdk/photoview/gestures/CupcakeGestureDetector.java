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

package com.shopgun.android.sdk.photoview.gestures;

import android.content.Context;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.log.EtaLog;

public class CupcakeGestureDetector implements GestureDetector {

    public static final String TAG = Constants.getTag(CupcakeGestureDetector.class);
    final float mTouchSlop;
    final float mMinimumVelocity;
    final android.view.GestureDetector mGestureDetector;
    protected OnGestureListener mListener;
    final android.view.GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener =
            new android.view.GestureDetector.SimpleOnGestureListener() {

                public void onLongPress(MotionEvent e) {
                    if (mListener != null) {
                        mListener.onLongPress(e);
                    }
                }

                ;

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    if (mListener != null) {
                        return mListener.onDoubleTab(e);
                    }
                    return false;
                }

                public boolean onSingleTapConfirmed(MotionEvent e) {
                    if (mListener != null) {
                        return mListener.onSingleTab(e);
                    }
                    return false;
                }

                ;

            };
    float mLastTouchX;
    float mLastTouchY;
    private VelocityTracker mVelocityTracker;
    private boolean mIsDragging;

    public CupcakeGestureDetector(Context context) {
        mGestureDetector = new android.view.GestureDetector(context, mSimpleOnGestureListener);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    public void setOnGestureListener(OnGestureListener listener) {
        this.mListener = listener;
    }

    float getActiveX(MotionEvent ev) {
        return ev.getX();
    }

    float getActiveY(MotionEvent ev) {
        return ev.getY();
    }

    public boolean isScaling() {
        return false;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mVelocityTracker = VelocityTracker.obtain();
                if (null != mVelocityTracker) {
                    mVelocityTracker.addMovement(ev);
                } else {
                    EtaLog.i(TAG, "Velocity tracker is null");
                }

                mLastTouchX = getActiveX(ev);
                mLastTouchY = getActiveY(ev);
                mIsDragging = false;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final float x = getActiveX(ev);
                final float y = getActiveY(ev);
                final float dx = x - mLastTouchX, dy = y - mLastTouchY;

                if (!mIsDragging) {
                    // Use Pythagoras to see if drag length is larger than
                    // touch slop
                    mIsDragging = FloatMath.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
                }

                if (mIsDragging) {
                    if (mListener != null) {
                        mListener.onDrag(dx, dy);
                    }
                    mLastTouchX = x;
                    mLastTouchY = y;

                    if (null != mVelocityTracker) {
                        mVelocityTracker.addMovement(ev);
                    }
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                // Recycle Velocity Tracker
                if (null != mVelocityTracker) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (mIsDragging) {
                    if (null != mVelocityTracker) {
                        mLastTouchX = getActiveX(ev);
                        mLastTouchY = getActiveY(ev);

                        // Compute velocity within the last 1000ms
                        mVelocityTracker.addMovement(ev);
                        mVelocityTracker.computeCurrentVelocity(1000);

                        final float vX = mVelocityTracker.getXVelocity(), vY = mVelocityTracker
                                .getYVelocity();

                        // If the velocity is greater than minVelocity, call
                        // listener
                        if (Math.max(Math.abs(vX), Math.abs(vY)) >= mMinimumVelocity) {
                            if (mListener != null) {
                                mListener.onFling(mLastTouchX, mLastTouchY, -vX, -vY);
                            }
                        }
                    }
                }

                // Recycle Velocity Tracker
                if (null != mVelocityTracker) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            }
        }
        mGestureDetector.onTouchEvent(ev);
        return true;
    }

}
