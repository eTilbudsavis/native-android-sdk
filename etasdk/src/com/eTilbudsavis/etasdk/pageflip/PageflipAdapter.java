package com.eTilbudsavis.etasdk.pageflip;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;
import com.eTilbudsavis.etasdk.Log.EtaLog;

public class PageflipAdapter extends FragmentPagerAdapter {
	
	public static final String TAG = PageflipAdapter.class.getSimpleName();
	
	Catalog mCatalog;
	private boolean mLandscape = false;
	
	public PageflipAdapter(FragmentManager fm, Catalog c, boolean landscape) {
		super(fm);
		mCatalog = c;
		mLandscape = landscape;
		EtaLog.d(TAG, "PageflipAdapter");
	}
	
	@Override
	public Fragment getItem(int position) {
		EtaLog.d(TAG, "getItem:" + position);
		if (position == 0 || position == getCount()) {
			// First page
			return SinglePage.newInstance(mCatalog, position, false);
		} else {
			// All other pages
			return PageflipPage.newInstance(mCatalog, position, mLandscape);
		}
		
	}

	@Override
	public int getCount() {
		EtaLog.d(TAG, "getCount:" + (mCatalog.getPageCount()-1));
		return mCatalog.getPageCount()-1;
	}
	
}
