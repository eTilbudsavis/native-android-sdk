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

import android.graphics.Bitmap;
import android.view.View;

import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.Hotspot;

import java.util.List;

public interface CatalogPageCallback {

    /**
     * Determining, if the {@link PageflipViewPager} if in the same position, as the position in the
     * {@link PageflipFragment}. Is among other things used to determine, if the {@link PageflipFragment}
     * and {@link PageflipViewPager} is ready to display the catalog pages.
     *
     * @return
     */
    boolean isPositionSet();

    /**
     * Called by a {@link PageflipFragment} when it's ready to display catalog pages. This is used
     * to control events during configuration changes
     *
     * @param position The position of the
     */
    void onReady(int position);

    void onSingleClick(View v, int page, float x, float y, List<Hotspot> hotspots);

    void onDoubleClick(View v, int page, float x, float y, List<Hotspot> hotspots);

    void onLongClick(View v, int page, float x, float y, List<Hotspot> hotspots);

    void onZoom(View v, int[] pages, boolean isZoomed);

    /**
     *
     * @return
     */
    Catalog getCatalog();

    /**
     *
     * @return
     */
    String getViewSession();

    Bitmap onDrawPage(int page, int[] pages, Bitmap b);

    void normalizeCatalogDimensions(Bitmap b);

}
