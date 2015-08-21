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

import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.Hotspot;

import java.util.List;

public class PageflipClickCoordinate {

    public final int page;
    public final float x;
    public final float y;
    public final List<Hotspot> list;

    public PageflipClickCoordinate(Catalog c, int[] pages, float x, float y) {

        float pagesLength = (float)pages.length;
        float pageWidth = 1.0f/pagesLength;
        int position = (int)Math.floor(x/pageWidth);

        this.page = pages[position];
        this.x = (x%pageWidth)*pagesLength;
        this.y = y;
        this.list = c.getHotspots().getHotspots(page, x, y, pages);

    }

    @Override
    public String toString() {
        String format = "%s[page:%s, x:%2f, y:%2f, hotspot.size:%s]";
        return String.format(format, PageflipClickCoordinate.class.getSimpleName(), page, x, y, list.size());
    }
}

