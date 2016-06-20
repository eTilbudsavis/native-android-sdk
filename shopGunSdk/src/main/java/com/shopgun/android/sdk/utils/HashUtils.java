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

import com.shopgun.android.sdk.Constants;

@Deprecated
public class HashUtils {

    public static final String TAG = Constants.getTag(HashUtils.class);

    /**
     * Generate a SHA256 checksum of a string.
     *
     * @param string to SHA256
     * @return A SHA256 string
     */
    public static String sha256(String string) {
        return com.shopgun.android.utils.HashUtils.sha256(string);
    }

    public static String md5(String s) {
        return com.shopgun.android.utils.HashUtils.md5(s);
    }

}
