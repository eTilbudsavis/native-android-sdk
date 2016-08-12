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

import android.graphics.Bitmap;

import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.utils.BitmapUtils;
import com.shopgun.android.utils.NumberUtils;
import com.shopgun.android.utils.TextUtils;

public class PageflipUtils {

    public static final String TAG = Constants.getTag(PageflipUtils.class);

    private PageflipUtils() {
        // Empty constructor
    }

    /** @deprecated see {@link TextUtils#join(CharSequence, int[])}*/
    @Deprecated
    public static String join(CharSequence delimiter, int[] tokens) {
        return TextUtils.join(delimiter,tokens);
    }

    /** @deprecated see {@link NumberUtils#isEqual(float, float, float)}*/
    @Deprecated
    public static boolean almost(float first, float second) {
        return NumberUtils.isEqual(first, second, 0.1f);
    }

    /** @deprecated see {@link NumberUtils#isEqual(float, float, float)}*/
    @Deprecated
    public static boolean almost(float first, float second, float epsilon) {
        return NumberUtils.isEqual(first, second, epsilon);
    }

    /**
     * Method for determining if the caralog is ready;
     *
     * @param c A {@link Catalog}
     * @return true, if the catalog is fully loaded, including pages and hotspots
     */
    public static boolean isCatalogReady(Catalog c) {
        return c != null && isPagesReady(c) && isHotspotsReady(c);
    }

    /**
     * Check if the given catalog has a list of valid Hotspots
     *
     * @param c A catalog
     * @return {@code true} if the catalog has hotspots, else {@code false}
     */
    public static boolean isHotspotsReady(Catalog c) {
        return c != null && c.getHotspots() != null;
    }

    /**
     * Check if a given {@link Catalog} has a valid list of {@link com.shopgun.android.sdk.model.Images}
     *
     * @param c A catalog
     * @return {@code true} if the catalog has pages, else {@code false}
     */
    public static boolean isPagesReady(Catalog c) {
        return c != null && c.getPages() != null && !c.getPages().isEmpty();
    }

    /** @deprecated see {@link BitmapUtils#sizeOf(Bitmap)}*/
    @Deprecated
    public static int sizeOf(Bitmap bitmap) {
        return BitmapUtils.sizeOf(bitmap);
    }

    /** @deprecated see {@link BitmapUtils#sizeOfKb(Bitmap)}*/
    @Deprecated
    public static int sizeOfKb(Bitmap bitmap) {
        return BitmapUtils.sizeOfKb(bitmap);
    }

}
