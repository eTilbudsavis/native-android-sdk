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
import android.os.Build;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.model.Catalog;

public class PageflipUtils {

    public static final String TAG = Constants.getTag(PageflipUtils.class);

    private PageflipUtils() {
        // Empty constructor
    }

    /**
     * Method for joining an array of int
     *
     * @param delimiter A string to join the int's by
     * @param tokens    the values
     * @return A formatted string
     */
    public static String join(CharSequence delimiter, int[] tokens) {
        StringBuilder sb = new StringBuilder();
        for (Object token : tokens) {
            if (sb.length() != 0) {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb.toString();
    }

    /**
     * Method for detecting if two floats are almost equal (precision within 0.1)
     *
     * @param first  a float
     * @param second another float
     * @return true if equal, else false
     */
    public static boolean almost(float first, float second) {
        return almost(first, second, 0.1f);
    }

    /**
     * Method for detecting if two floats are almost equal
     *
     * @param first   a float
     * @param second  another float
     * @param epsilon The precision of the measurement
     * @return true if equal, else false
     */
    public static boolean almost(float first, float second, float epsilon) {
        return Math.abs(first - second) < epsilon;
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

    /**
     * returns the bytesize of the give bitmap
     * @param bitmap A bitmap to measure
     * @return Size of bitmap, in byte
     */
    public static int sizeOf(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        } else {
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }

    /**
     * returns the kilobytesize of the give bitmap
     * @param bitmap A bitmap to measure
     * @return Size of bitmap, in kilo byte
     */
    public static int sizeOfKb(Bitmap bitmap) {
        return sizeOf(bitmap) / 1024;
    }

}
