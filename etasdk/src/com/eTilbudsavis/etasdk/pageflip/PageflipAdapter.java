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
	private int mViewCount = 0;
	private int mPageCount = 0;
	
	public PageflipAdapter(FragmentManager fm, Catalog c, boolean landscape) {
		super(fm);
		mCatalog = c;
		mLandscape = landscape;
		mViewCount = mLandscape ? (mCatalog.getPageCount()/2)+1 : mCatalog.getPageCount();
		mPageCount = mCatalog.getPageCount()-1;
	}
	
	@Override
	public Fragment getItem(int position) {
		int page = PageflipUtils.positionToPage(position, mLandscape);
		EtaLog.d(TAG, "getItem[pos:" + position + ", maxPos:" + (mViewCount-1) + ", page:" + page + ", maxPage:" + mPageCount);
		if ( !mLandscape || page == 0 || page == mPageCount ) {
			return PageflipSinglePage.newInstance(mCatalog, page);
		} else {
			return PageflipDoublePage.newInstance(mCatalog, page);
		}
	}
	
	@Override
	public int getCount() {
		return mViewCount;
	}
	
}
