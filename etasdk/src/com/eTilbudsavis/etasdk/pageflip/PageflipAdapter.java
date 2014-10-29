package com.eTilbudsavis.etasdk.pageflip;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class PageflipAdapter extends FragmentStatePagerAdapter {
	
	public static final String TAG = PageflipAdapter.class.getSimpleName();
	
	private PageCallback mCallback;
	private int mViewCount = 0;
	
	public PageflipAdapter(FragmentManager fm, PageCallback callback) {
		super(fm);
		mCallback = callback;
		if (mCallback.isLandscape()) {
			mViewCount = (mCallback.getCatalog().getPageCount()/2)+1;
		} else {
			mViewCount = mCallback.getCatalog().getPageCount();
		}
	}
	
	@Override
	public Fragment getItem(int position) {
		int[] pages = PageflipUtils.positionToPages(position, mCallback.getCatalog().getPageCount(), mCallback.isLandscape());
		PageFragment f = PageFragment.newInstance(pages);
		f.setPageCallback(mCallback);
		return f;
	}
	
	@Override
	public int getCount() {
		return mViewCount;
	}
	
}
