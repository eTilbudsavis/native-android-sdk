package com.eTilbudsavis.etasdk.pageflip;

import java.lang.reflect.Field;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class PageflipViewPager extends ViewPager {
	
	private ScrollerCustomDuration mScroller = null;
	
	public PageflipViewPager(Context context) {
		super(context);
		init();
	}

	public PageflipViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
		setScroller();
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
	
}
