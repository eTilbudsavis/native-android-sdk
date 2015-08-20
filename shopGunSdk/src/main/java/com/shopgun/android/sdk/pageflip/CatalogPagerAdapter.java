package com.shopgun.android.sdk.pageflip;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.pageflip.utils.PageflipUtils;

public class CatalogPagerAdapter extends FragmentStatelessPagerAdapter {

    public static final String TAG = CatalogPagerAdapter.class.getSimpleName();

    private CatalogPageCallback mCallback;
    private int mViewCount = 0;
    private boolean mLandscape = false;
    private int mMaxHeap;

    public CatalogPagerAdapter(FragmentManager fm, int maxHeap, CatalogPageCallback callback, boolean landscape) {
        super(fm);
        mCallback = callback;
        mLandscape = landscape;
        int pc = mCallback.getCatalog().getPageCount();
        mViewCount = mLandscape ? (pc/2)+1 : pc;
        mMaxHeap = maxHeap;
//        SgnLog.d(TAG, toString());
    }

    @Override
    public Fragment getItem(int position) {
        Catalog c = mCallback.getCatalog();
        int[] pages = PageflipUtils.positionToPages(position, c.getPageCount(), mLandscape);
        PageLoader.Config config = new PageLoader.Config(mMaxHeap, pages, c);
        CatalogPageFragment f = CatalogPageFragment.newInstance(position, pages, config);
        f.setCatalogPageCallback(mCallback);
        return f;
    }

    @Override
    public String toString() {
        int pc = mCallback.getCatalog().getPageCount();
        String f = "%s[landscape:%s, pageCount:%s, viewCount:%s]";
        return String.format(f, TAG, mLandscape, pc, mViewCount);
    }

    @Override
    public int getCount() {
        return mViewCount;
    }

}
