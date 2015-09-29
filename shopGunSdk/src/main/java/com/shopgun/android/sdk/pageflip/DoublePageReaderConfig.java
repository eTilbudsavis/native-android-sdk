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

import android.os.Parcel;

public class DoublePageReaderConfig extends ReaderConfig {

    public static final String TAG = DoublePageReaderConfig.class.getSimpleName();

    public DoublePageReaderConfig() {

    }

    @Override
    public int pageToPosition(int[] pages) {
        return pageToPosition(pages[0]);
    }

    @Override
    public int pageToPosition(int page) {
        if (isLandscape() && page > 1) {
            // normalize so we'll get the first actual page from a position
            page -= page % 2;
            return hasIntro() ? (page/2)+1 : page/2;
        }
        return hasIntro() ? page : page-1;
    }

    @Override
    public int[] positionToPages(int position, int pageCount) {

        boolean landscape = isLandscape();

        if (hasIntro() && position > 0) {
            // if the intro is present just offset everything by one,
            // except if it's the intro position it self
            position--;
        }

        if (hasOutro()) {
            // Correct values if there is an outro page
            int maxPagePos = landscape ? pageCount/2 : pageCount-1;
            if (maxPagePos < position) {
                position = maxPagePos;
            }
        }

        // default is offset by one
        int page;
        if (landscape && position > 0) {
            page = (position * 2);
        } else {
            page = position + 1;
        }

        int[] pages;
        if (!landscape || page == 1 || page == pageCount) {
            // first, last, and everything in portrait is single-page
            pages = new int[]{page};
        } else {
            // Anything else is double page
            pages = new int[]{page, (page + 1)};
        }
        return pages;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected DoublePageReaderConfig(Parcel in) {
        super(in);
    }

    public static final Creator<DoublePageReaderConfig> CREATOR = new Creator<DoublePageReaderConfig>() {
        public DoublePageReaderConfig createFromParcel(Parcel source) {
            return new DoublePageReaderConfig(source);
        }

        public DoublePageReaderConfig[] newArray(int size) {
            return new DoublePageReaderConfig[size];
        }
    };

}
