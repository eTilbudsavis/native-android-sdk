package com.shopgun.android.sdk.pageflip;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.shopgun.android.sdk.model.Catalog;

public class CatalogPagerAdapter extends FragmentStatelessPagerAdapter {

    public static final String TAG = CatalogPagerAdapter.class.getSimpleName();

    private CatalogPageCallback mCallback;
    private int mViewCount = 0;
    private int mMaxHeap;
    private ReaderConfig mConfig;

    public CatalogPagerAdapter(FragmentManager fm, int maxHeap, CatalogPageCallback callback, ReaderConfig config) {
        super(fm);
        mMaxHeap = maxHeap;
        mCallback = callback;
        mConfig = config;
        int pc = mCallback.getCatalog().getPageCount();
        mViewCount = mConfig.isLandscape() ? (pc/2)+1 : pc;
//        SgnLog.d(TAG, toString());
    }

    @Override
    public Fragment getItem(int position) {
        Catalog c = mCallback.getCatalog();
        int[] pages = mConfig.positionToPages(position, c.getPageCount());
        PageLoader.Config config = new PageLoader.Config(mMaxHeap, pages, c);
        CatalogPageFragment f = CatalogPageFragment.newInstance(position, pages, config);
        f.setCatalogPageCallback(mCallback);
        return f;
    }

    @Override
    public String toString() {
        int pc = mCallback.getCatalog().getPageCount();
        String f = "%s[landscape:%s, pageCount:%s, viewCount:%s]";
        return String.format(f, TAG, mConfig.isLandscape(), pc, mViewCount);
    }

    @Override
    public int getCount() {
        return mViewCount;
    }

}
