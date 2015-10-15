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

package com.shopgun.android.sdk.palette;

import android.os.Parcelable;

public interface MaterialColor extends Parcelable {

    MaterialColor getColor(Shade s);

    int getValue();

    int getPrimaryText();

    int getSecondaryText();

    int getDisabledText();

    /**
     * Returns the luminance of the color.
     *
     * Formula defined here: http://www.w3.org/TR/2008/REC-WCAG20-20081211/#relativeluminancedef
     */
    double getLuminance();

    /**
     * luminance value above 0.95
     */
    boolean isVeryBright();

    /** luminance value above 0.87 */
    boolean isBright();

    /** luminance value above 0.64 */
    boolean isLight();

    /** luminance value below 0.13 */
    boolean isDark();

    /** luminance value below 0.025 */
    boolean isVeryDark();

}
