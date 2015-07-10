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

package com.shopgun.android.sdk.pageflip;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Interpolator;
import android.widget.Scroller;

import com.shopgun.android.sdk.Constants;

import java.lang.reflect.Field;

public class PageflipViewPager extends ViewPager {

    public static final String TAG = Constants.getTag(PageflipViewPager.class);

    private PageflipListener mPageflipListener;
    private ScrollerCustomDuration mScroller = null;
    private boolean mIsBeingDragged = false;
    private float mLastMotionX;
    private boolean mOutOfBoundsCalled = false;
//	private HackPageChangeListener mOnPageChangeListener = new HackPageChangeListener();

//	@Override
//	public void setOnPageChangeListener(OnPageChangeListener listener) {
//		mOnPageChangeListener.mOnPageChangeListener = listener;
//	}

    public PageflipViewPager(Context context) {
        super(context);
        init();
    }

    public PageflipViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
//		super.setOnPageChangeListener(mOnPageChangeListener);
        setScroller();
    }

    public void setPageflipListener(PageflipListener l) {
        mPageflipListener = l;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        mIsBeingDragged = false;
        try {
            mIsBeingDragged = super.onInterceptTouchEvent(ev);
            mLastMotionX = ev.getX();
        } catch (IllegalArgumentException e) {
            // Bug in Eclair - ignore any exceptions
        } catch (ArrayIndexOutOfBoundsException e) {
            // Don't know why this is being thrown
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean isWorking = false;
        try {
            isWorking = super.onTouchEvent(ev);
            if (mIsBeingDragged && isWorking) {
                tryBounds(ev);
            }

        } catch (IllegalArgumentException e) {
            // Bug in Eclair - ignore any exceptions
        } catch (ArrayIndexOutOfBoundsException e) {
            // Don't know why this is being thrown
        }

        return isWorking;
    }

    private void tryBounds(MotionEvent ev) {

        switch (ev.getAction() & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:

                if (!mOutOfBoundsCalled) {

                    // Scroll to follow the motion event
                    final float x = ev.getX();
                    final float deltaX = mLastMotionX - x;
                    mLastMotionX = x;

                    final int lastItemIndex = getAdapter().getCount() - 1;
                    final int currentItem = getCurrentItem();

                    if (deltaX < 0 && currentItem == 0) {
                        if (mPageflipListener != null) {
                            mPageflipListener.onOutOfBounds(true);
                        }
                        mOutOfBoundsCalled = true;
                    } else if (deltaX > 0 && currentItem == lastItemIndex) {
                        if (mPageflipListener != null) {
                            mPageflipListener.onOutOfBounds(false);
                        }
                        mOutOfBoundsCalled = true;
                    }

                }

                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                mOutOfBoundsCalled = false;
                break;
        }
    }

    private void setScroller() {
        try {
            Class<?> viewpager = ViewPager.class;
            Field scroller = viewpager.getDeclaredField("mScroller");
            scroller.setAccessible(true);
            Field interpolator = viewpager.getDeclaredField("sInterpolator");
            interpolator.setAccessible(true);
            mScroller = new ScrollerCustomDuration(getContext(), (Interpolator) interpolator.get(null));
            scroller.set(this, mScroller);
        } catch (Exception e) {
        }
    }

    /**
     * Set the factor by which the duration will change
     */
    public void setScrollDurationFactor(double scrollFactor) {
        mScroller.setScrollDurationFactor(scrollFactor);
    }

    public class ScrollerCustomDuration extends Scroller {

        private double mScrollFactor = 1;

        public ScrollerCustomDuration(Context context) {
            super(context);
        }

        public ScrollerCustomDuration(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        public ScrollerCustomDuration(Context context, Interpolator interpolator, boolean flywheel) {
            super(context, interpolator, flywheel);
        }

        /**
         * Set the factor by which the duration will change
         */
        public void setScrollDurationFactor(double scrollFactor) {
            mScrollFactor = scrollFactor;
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, (int) (duration * mScrollFactor));
        }

    }

//	public class HackPageChangeListener implements OnPageChangeListener {
//	    
//		private float mLastPositionOffset = 0f;
//	    
//	    public OnPageChangeListener mOnPageChangeListener;
//	    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//	        if(positionOffset < mLastPositionOffset && positionOffset < 0.7) {
//	            setCurrentItem(position);
//	        } else if(positionOffset > mLastPositionOffset && positionOffset > 0.3) {
//	            setCurrentItem(position+1);
//	        }
//	        mLastPositionOffset = positionOffset;
//	        if (mOnPageChangeListener!=null) {
//	        	mOnPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
//	        }
//	    }
//		public void onPageScrollStateChanged(int arg0) {
//	        if (mOnPageChangeListener!=null) {
//	        	mOnPageChangeListener.onPageScrollStateChanged(arg0);
//	        }
//		}
//		public void onPageSelected(int arg0) {
//	        if (mOnPageChangeListener!=null) {
//	        	mOnPageChangeListener.onPageSelected(arg0);
//	        }
//		}
//	}

}
