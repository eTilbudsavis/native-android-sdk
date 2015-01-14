package com.eTilbudsavis.etasdk.pageflip;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.pageflip.utils.PageflipUtils;

public class PageAdapter extends FragmentStatePagerAdapter {
	
	public static final String TAG = Eta.TAG_PREFIX + PageAdapter.class.getSimpleName();
	
	private PageCallback mCallback;
	private int mViewCount = 0;
	
	public PageAdapter(FragmentManager fm, PageCallback callback) {
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
//		EtaLog.d(TAG, "getItem: " + position);
		int[] pages = PageflipUtils.positionToPages(position, mCallback.getCatalog().getPageCount(), mCallback.isLandscape());
		PageFragment f = PageFragment.newInstance(position, pages);
		f.setPageCallback(mCallback);
		return f;
	}
	
	@Override
	public int getCount() {
		return mViewCount;
	}
	
}
