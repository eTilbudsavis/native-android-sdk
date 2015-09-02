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

package com.shopgun.android.sdk.api;

import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.ShoppinglistItem;

/**
 * <p>This class contains a sub-set of strings that ShopGun uses as keys for the meta-object,
 * found on {@link Shoppinglist#getMeta()}, and {@link ShoppinglistItem#getMeta()}.</p>
 */
public class MetaKeys {

    /**
     * for comments on shoppinglistitems
     */
    public static final String COMMENT = "eta_comment";
    /**
     * for themes on shoppinglists
     */
    public static final String THEME = "eta_theme";

}
