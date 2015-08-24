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

import com.shopgun.android.sdk.model.Catalog;

public class TwoPageReaderConfig extends ReaderConfig {

    public TwoPageReaderConfig() {

    }

    @Override
    public int pageToPosition(int[] pages) {
        return pageToPosition(pages[0]);
    }

    @Override
    public int pageToPosition(int page) {
        int pos = page - 1;
        if (isLandscape() && page > 1) {
            if (page % 2 == 1) {
                page--;
            }
            pos = page / 2;
        }
        return pos;
    }

    @Override
    public int[] positionToPages(int position, int pageCount) {
        // default is offset by one
        int page;
        boolean land = isLandscape();
        if (land && position != 0) {
            page = (position * 2);
        } else {
            page = position + 1;
        }

        int[] pages;
        if (!land || page == 1 || page == pageCount) {
            // first, last, and everything in portrait is single-page
            pages = new int[]{page};
        } else {
            // Anything else is double page
            pages = new int[]{page, (page + 1)};
        }
        return pages;
    }

    @Override
    public boolean isValidPage(Catalog c, int page) {
        return 1 <= page && (c == null || page <= c.getPageCount());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected TwoPageReaderConfig(Parcel in) {
        super(in);
    }

    public static final Creator<TwoPageReaderConfig> CREATOR = new Creator<TwoPageReaderConfig>() {
        public TwoPageReaderConfig createFromParcel(Parcel source) {
            return new TwoPageReaderConfig(source);
        }

        public TwoPageReaderConfig[] newArray(int size) {
            return new TwoPageReaderConfig[size];
        }
    };

}
