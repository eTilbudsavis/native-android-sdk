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

public class SinglePageReaderConfig extends ReaderConfig {

    public SinglePageReaderConfig() {
    }

    @Override
    public int pageToPosition(int[] pages) {
        return pageToPosition(pages[0]);
    }

    @Override
    public int pageToPosition(int page) {
        return page-1;
    }

    @Override
    public int[] positionToPages(int position, int pageCount) {
        return new int[]{position+1};
    }

    @Override
    public boolean isValidPage(Catalog c, int page) {
        return 1 <= page && (c == null || page <= c.getPageCount());
    }

    @Override
    public boolean isLandscape() {
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    protected SinglePageReaderConfig(Parcel in) {
        super(in);
    }

    public static final Creator<SinglePageReaderConfig> CREATOR = new Creator<SinglePageReaderConfig>() {
        public SinglePageReaderConfig createFromParcel(Parcel source) {
            return new SinglePageReaderConfig(source);
        }

        public SinglePageReaderConfig[] newArray(int size) {
            return new SinglePageReaderConfig[size];
        }
    };

}
