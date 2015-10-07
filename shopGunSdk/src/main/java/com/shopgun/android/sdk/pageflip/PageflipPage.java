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

public interface PageflipPage {

    /**
     * Returns the proportional width of this {@link PageflipPage} as a percentage of the
     * ViewPager's measured width from (0.f-1.f]
     *
     * @return Proportional width for the given page position
     */
    float getPageWidth();

    /**
     * Called when this {@link PageflipPage} becomes invisible/has focus in the {@link PageflipViewPager}.
     * <p>Remember to </p>
     * This can be used for statistics events.
     */
    void onInvisible();

    /**
     * Called when the {@link PageflipPage} is no longer visible/has focus in the {@link PageflipViewPager}.
     * This can be used for statistics events.
     */
    void onVisible();

}
