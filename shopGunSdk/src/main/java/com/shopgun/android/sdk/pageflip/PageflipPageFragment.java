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

import com.shopgun.android.sdk.Constants;

public class PageflipPageFragment extends Fragment {

    public static final String TAG = Constants.getTag(PageflipPageFragment.class);

    private boolean mPageVisible = false;
    private boolean mCalled = false;

    public float getPageWidth() {
        return 1.0f;
    }

    /**
     * Called when the {@link PageflipPageFragment} becomes invisible/has focus in the {@link PageflipViewPager}.
     * <p>Remember to </p>
     * This can be used for statistics events.
     */
    public void onInvisible() {
        mPageVisible = false;
        mCalled = true;
    }

    /**
     * Called when the {@link PageflipPageFragment} is no longer visible/has focus in the {@link PageflipViewPager}.
     * This can be used for statistics events.
     */
    public void onVisible() {
        mPageVisible = true;
        mCalled = true;
    }

    /**
     * Tell if the fragment is current visible/has focus in the {@link PageflipViewPager}.
     *
     * @return true if visible, else false.
     */
    public boolean isPageVisible() {
        return mPageVisible;
    }

    void performVisible() {
        mCalled = false;
        onVisible();
        if (!mCalled) {
            throw new SuperNotCalledException("Fragment " + this + " did not call through to super.onVisible()");
        }
    }

    void performInvisible() {
        mCalled = false;
        onInvisible();
        if (!mCalled) {
            throw new SuperNotCalledException("Fragment " + this + " did not call through to super.onInvisible()");
        }
    }

}
