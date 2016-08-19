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

package com.shopgun.android.sdk.pageflip.utils;

import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.Hotspot;
import com.shopgun.android.sdk.utils.Constants;

import java.util.List;

public class PageflipClickCoordinate {

    public static final String TAG = Constants.getTag(PageflipClickCoordinate.class);

    private final int mPage;
    private final float mActualX;
    private final float mActualY;
    private final List<Hotspot> mHotspots;

    public PageflipClickCoordinate(Catalog c, int[] pages, float x, float y) {

        float pagesLength = (float)pages.length;
        float pageWidth = 1.0f/pagesLength;
        int position = (int)Math.floor(x/pageWidth);

        mPage = pages[position];
        mActualX = (x%pageWidth)*pagesLength;
        mActualY = y;
        mHotspots = c.getHotspots().getHotspots(mPage, mActualX, mActualY, pages);

    }

    public int getPage() {
        return mPage;
    }

    public float getX() {
        return mActualX;
    }

    public float getY() {
        return mActualY;
    }

    public List<Hotspot> getHotspots() {
        return mHotspots;
    }

    @Override
    public String toString() {
        String format = "%s[page:%s, x:%2f, y:%2f, hotspot.size:%s]";
        return String.format(format, PageflipClickCoordinate.class.getSimpleName(), mPage, mActualX, mActualY, mHotspots.size());
    }
}

