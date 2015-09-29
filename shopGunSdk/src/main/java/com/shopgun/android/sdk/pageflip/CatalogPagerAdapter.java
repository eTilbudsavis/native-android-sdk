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

import com.shopgun.android.sdk.model.Catalog;

public class CatalogPagerAdapter extends FragmentStatelessPagerAdapter {

    public static final String TAG = CatalogPagerAdapter.class.getSimpleName();

    private CatalogPageCallback mCallback;
    private int mMaxHeap;
    private ReaderConfig mConfig;
    private PageflipPageFragment mIntro;
    private PageflipPageFragment mOutro;

    public CatalogPagerAdapter(FragmentManager fm, int maxHeap, CatalogPageCallback callback, ReaderConfig config) {
        super(fm);
        mMaxHeap = maxHeap;
        mCallback = callback;
        mConfig = config;
//        SgnLog.d(TAG, toString());
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0 && mIntro != null) {
            return mIntro;
        }
        if (position == getCount()-1 && mOutro != null) {
            return mOutro;
        }
        Catalog c = mCallback.getCatalog();
        int[] pages = mConfig.positionToPages(position, c.getPageCount());
        PageLoader.Config config = new PageLoader.Config(mMaxHeap, pages, c);
        CatalogPageFragment f = CatalogPageFragment.newInstance(position, pages, config);
        f.setCatalogPageCallback(mCallback);
        return f;
    }

    @Override
    public float getPageWidth(int position) {
        return ((PageflipPageFragment)getItem(position)).getPageWidth();
    }

    @Override
    public int getCount() {
        int pc = mCallback.getCatalog().getPageCount();
        int count = mConfig.isLandscape() ? (pc/2)+1 : pc;
        if (mIntro != null) {
            count++;
        }
        if (mOutro != null) {
            count ++;
        }
        return count;
    }

    public void setIntroFragment(PageflipPageFragment intro) {
        if (mIntro != intro) {
            mIntro = intro;
            mConfig.setHasIntro(mIntro != null);
            notifyDataSetChanged();
        }
        mConfig.setHasIntro(mIntro != null);
    }

    public void setOutroFragment(PageflipPageFragment outro) {
        if (mOutro != outro) {
            mOutro = outro;
            mConfig.setHasOutro(mOutro != null);
            notifyDataSetChanged();
        }
        mConfig.setHasOutro(mOutro != null);
    }

    @Override
    public String toString() {
        int pc = mCallback.getCatalog().getPageCount();
        String f = "%s[landscape:%s, pageCount:%s, viewCount:%s]";
        return String.format(f, TAG, mConfig.isLandscape(), pc, getCount());
    }

}
