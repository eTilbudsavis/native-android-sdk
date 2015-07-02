/*******************************************************************************
 * Copyright 2015 eTilbudsavis
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

package com.eTilbudsavis.etasdk.pageflip;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.pageflip.utils.PageflipUtils;

public class PageAdapter extends FragmentStatePagerAdapter {

    public static final String TAG = Constants.getTag(PageAdapter.class);

    private PageCallback mCallback;
    private int mViewCount = 0;

    public PageAdapter(FragmentManager fm, PageCallback callback) {
        super(fm);
        mCallback = callback;
        if (mCallback.isLandscape()) {
            mViewCount = (mCallback.getCatalog().getPageCount() / 2) + 1;
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
