package com.eTilbudsavis.etasdk.pageflip;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.eTilbudsavis.etasdk.EtaObjects.Catalog;

public class PageflipAdapter extends FragmentPagerAdapter {
	
	public static final String TAG = PageflipAdapter.class.getSimpleName();
	
	Catalog mCatalog;
	private boolean mLandscape = false;
	
	public PageflipAdapter(FragmentManager fm, Catalog c, boolean landscape) {
		super(fm);
		mCatalog = c;
		mLandscape = landscape;
	}
	
	@Override
	public Fragment getItem(int position) {
		return PageflipPage.newInstance(mCatalog, position, mLandscape);
	}

	@Override
	public int getCount() {
		return mCatalog.getPageCount()-1;
	}
	
	
	
}
