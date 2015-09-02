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

public enum ThemeEnvironment {
    PRODUCTION("https://" + Environment.HOST_POSTFIX + "/rest/v1/lists/themes/"),
    STAGING("https://staging." + Environment.HOST_POSTFIX + "/rest/v1/lists/themes/");

    private final String mEnvironment;

    ThemeEnvironment(String theme) {
        mEnvironment = theme;
    }

    /**
     * Convert a string into an {@link ThemeEnvironment}.
     *
     * @param env An environment string
     * @return A matching {@link ThemeEnvironment} or {@link ThemeEnvironment#PRODUCTION} if no match was found
     */
    public static ThemeEnvironment fromString(String env) {
        if (env != null) {
            for (ThemeEnvironment e : ThemeEnvironment.values()) {
                if (env.equalsIgnoreCase(e.mEnvironment)) {
                    return e;
                }
            }
        }
        return PRODUCTION;
    }

    @Override
    public String toString() {
        return mEnvironment;
    }

    public String build(String path) {
        return mEnvironment + path;
    }

}
