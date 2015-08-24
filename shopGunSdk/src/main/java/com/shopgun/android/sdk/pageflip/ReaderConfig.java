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

import android.content.res.Configuration;
import android.os.Parcel;
import android.os.Parcelable;

import com.shopgun.android.sdk.model.Catalog;

public abstract class ReaderConfig implements Parcelable {

    Configuration mConfig;

    public ReaderConfig() {
    }

    /**
     * Convert an array of pages into it's corresponding position in the {@link PageflipViewPager}.
     *
     * @param pages     Array to convert
     * @return A position
     */
    public abstract int pageToPosition(int[] pages);

    /**
     * Convert an page into it's corresponding position in the {@link PageflipViewPager}.
     *
     * @param page      An int to convert
     * @return A position
     */
    public abstract int pageToPosition(int page);

    /**
     * Convert a position of a {@link PageflipViewPager} into it's corresponding human readable pages.
     *
     * @param position  The {@link PageflipViewPager} position
     * @param pageCount The number of pages in the {@link Catalog} being displayed in the {@link PageflipViewPager}
     * @return An array of pages
     */
    public abstract int[] positionToPages(int position, int pageCount);

    /**
     * Check if a page is within a valid range
     *
     * @param c    A catalog
     * @param page the page number
     * @return true if valid, else false
     */
    public abstract boolean isValidPage(Catalog c, int page);

    /**
     * Method for getting the orientation
     *
     * @return true if in landscape mode, else false
     */
    public boolean isLandscape() {
        return mConfig != null && mConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public void setConfiguration(Configuration config) {
        mConfig = config;
    }

    public Configuration getConfiguration() {
        return mConfig;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mConfig, 0);
    }

    protected ReaderConfig(Parcel in) {
        this.mConfig = in.readParcelable(Configuration.class.getClassLoader());
    }

}
