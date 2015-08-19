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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.pageflip.utils.PageflipUtils;

import java.util.ArrayList;
import java.util.List;

public class PageAdapter extends FragmentStatePagerAdapter {

    public static final String TAG = Constants.getTag(PageAdapter.class);

    private CatalogPageCallback mCallback;
    private int mViewCount = 0;
    private boolean mLandscape = false;
    private FragmentManager mFragmentManager;
    private List<Fragment> mFragments = new ArrayList<Fragment>();

    public PageAdapter(FragmentManager fm, CatalogPageCallback callback, boolean landscape) {
        super(fm);
        mFragmentManager = fm;
        mCallback = callback;
        mLandscape = landscape;
        int pc = mCallback.getCatalog().getPageCount();
        mViewCount = mLandscape ? (pc/2)+1 : pc;
        SgnLog.d(TAG, toString());
    }

    @Override
    public String toString() {
        int pc = mCallback.getCatalog().getPageCount();
        String f = "landscape:%s, pageCount:%s, viewCount:%s";
        return String.format(f, mLandscape, pc, mViewCount);
    }

    @Override
    public Fragment getItem(int position) {
        int[] pages = PageflipUtils.positionToPages(position, mCallback.getCatalog().getPageCount(), mLandscape);
        CatalogPageFragment f = CatalogPageFragment.newInstance(position, pages);
        f.setCatalogPageCallback(mCallback);
        mFragments.add(f);
        return f;
    }

    public void clear() {
        clearDestroyItem();
    }

    public void clearFragmentManager() {
        SgnLog.d(TAG, "clearFragmentManager()");
        FragmentTransaction ft = mFragmentManager.beginTransaction();
        for (Fragment f : mFragments) {
            ft.remove(f);
        }
        ft.commit();
        mFragmentManager.executePendingTransactions();
        mFragments.clear();
    }

    public void clearDestroyItem() {
        SgnLog.d(TAG, "clearDestroyItem()");
        for (int i = 0; i < mFragments.size(); i++) {
            destroyItem(null, i, mFragments.get(i));
        }
        mFragments.clear();
    }

    @Override
    public int getCount() {
        return mViewCount;
    }

}
