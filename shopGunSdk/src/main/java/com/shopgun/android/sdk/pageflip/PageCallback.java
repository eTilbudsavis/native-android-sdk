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

public interface PageCallback {

    /**
     * Get the catalog bring displayed
     *
     * @return A catalog
     */
    public Catalog getCatalog();

    /**
     * The current configuration of the device
     *
     * @return true if the device is in landscape, else false
     */
    public boolean isLandscape();

    /**
     * Determining, if the {@link PageflipViewPager} if in the same position, as the position in the
     * {@link PageflipFragment}. Is among other things used to determine, if the {@link PageflipFragment}
     * and {@link PageflipViewPager} is ready to display the catalog pages.
     *
     * @return
     */
    public boolean isPositionSet();

    /**
     * Check if the device has small heap size
     *
     * @return true if heap size is small.
     */
    public boolean isLowMemory();

    /**
     * Get the {@link PageflipListener} that the {@link PageflipFragment} contains, in order to print debug info.
     *
     * @return A {@link PageflipListener}
     */
    public PageflipListener getWrapperListener();

    /**
     * Get the current view-session. A variable used to give correct statistics to the ShopGun API.
     *
     * @return A string representation of the view-session token
     */
    public String getViewSession();

    /**
     * Called by a {@link PageflipFragment} when it's ready to display catalog pages. This is used
     * to control events during configuration changes
     *
     * @param position The position of the {@link CatalogPageFragment}
     */
    public void onReady(int position);

}