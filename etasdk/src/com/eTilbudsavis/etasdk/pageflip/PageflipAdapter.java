package com.eTilbudsavis.etasdk.pageflip;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.Log.EtaLog;

public class PageflipAdapter extends FragmentStatePagerAdapter {
	
	public static final String TAG = PageflipAdapter.class.getSimpleName();
	
	private Catalog mCatalog;
	private boolean mLandscape = false;
	private int mPageCount = -1;
	private int mCount = -1;
	
	public PageflipAdapter(FragmentManager fm, Catalog c, boolean landscape) {
		super(fm);
		mCatalog = c;
		mLandscape = landscape;
		mPageCount = mCatalog.getPageCount()-1;
		mCount = mLandscape ? (mCatalog.getPageCount()/2)+1 : mPageCount;
	}
	
	@Override
	public Fragment getItem(int position) {
		int page = PageflipUtils.positionToPage(position, mLandscape);
//		EtaLog.d(TAG, "getItem[pos:" + position + ", max:" + getCount() + ", page:" + page + ", max:" + mPageCount);
		if ( !mLandscape || page == 0 || page == mPageCount ) {
			return PageflipSinglePage.newInstance(mCatalog, page);
		} else {
			return PageflipDoublePage.newInstance(mCatalog, page);
		}
	}
	
	@Override
	public int getCount() {
		return mCount;
	}
	
	
	
}
