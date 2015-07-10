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

package com.shopgun.android.sdk.utils;

/**
 * Helper class for headers the ShopGun API uses
 * @author Danny Hvam - danny@etilbudsavis.dk
 */
public class HeaderUtils {

    /** Header name for the session token */
    public static final String X_TOKEN = "X-Token";

    /** Header name for the session expire token */
    public static final String X_TOKEN_EXPIRES = "X-Token-Expires";

    /** Header name for the signature */
    public static final String X_SIGNATURE = "X-Signature";

    /** Header name for content_type */
    public static final String CONTENT_TYPE = "Content-Type";

    /** Header name for content_type */
    public static final String RETRY_AFTER = "Retry-After";

    /** Header name for cash control */
    public static final String CACHE_CONTROL = "Cache-Control";

    class Values {
        public static final String NO_CACHE = "no-cache";
        public static final String NO_STORE = "no-store";
    }

}
