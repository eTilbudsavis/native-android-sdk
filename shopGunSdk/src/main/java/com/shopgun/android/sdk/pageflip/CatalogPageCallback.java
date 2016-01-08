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
import com.shopgun.android.sdk.pageflip.stats.PageflipStatsCollector;

import java.util.List;

public interface CatalogPageCallback {

    /**
     * Called by a {@link PageflipFragment} when it's ready to display catalog pages. This is used
     * to control events during configuration changes
     *
     * @param position The position of the
     */
    void onReady(int position);

    /**
     * Called on a catalog page single-click
     * @param v The view clicked
     * @param page The page clicked. If displaying a two page layout, only the page clicked will be returned.
     * @param x The x coordinate of the click (relative to the page returned)
     * @param y The y coordinate of the click (relative to the page returned)
     * @param hotspots A list of hotspots (empty if no hotspots was found)
     */
    void onSingleClick(View v, int page, float x, float y, List<Hotspot> hotspots);

    /**
     * Called on a catalog page double-click
     * @param v The view clicked
     * @param page The page clicked. If displaying a two page layout, only the page clicked will be returned.
     * @param x The x coordinate of the click (relative to the page returned)
     * @param y The y coordinate of the click (relative to the page returned)
     * @param hotspots A list of hotspots (empty if no hotspots was found)
     */
    void onDoubleClick(View v, int page, float x, float y, List<Hotspot> hotspots);

    /**
     * Called on a catalog page long-click
     * @param v The view clicked
     * @param page The page clicked. If displaying a two page layout, only the page clicked will be returned.
     * @param x The x coordinate of the click (relative to the page returned)
     * @param y The y coordinate of the click (relative to the page returned)
     * @param hotspots A list of hotspots (empty if no hotspots was found)
     */
    void onLongClick(View v, int page, float x, float y, List<Hotspot> hotspots);

    void onZoom(View v, int[] pages, boolean isZoomed);

    /**
     * Get the catalog currently being displayed
     * @return A {@link Catalog}
     */
    Catalog getCatalog();

    /**
     * Called when a {@link CatalogPageFragment} has a page that is ready to be drawn.
     * Each page in a given array of pages will be called individually.
     * @param page Human readable page number to be drawn
     * @param pages The array of pages that this page will be drawn with
     * @param b The page bitmap
     * @return A bitmap.
     */
    Bitmap onDrawPage(int page, int[] pages, Bitmap b);

    /**
     * Get the collector to be used for stats collection
     * @return A collector
     */
    PageflipStatsCollector getCollector(int[] pages);

}
