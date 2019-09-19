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

/**
 * The {@link EnvironmentEvents} contains the event endpoints.
 *
 * By default, the {@link EnvironmentEvents} is set to {@link EnvironmentEvents#PRODUCTION}.
 */
public enum EnvironmentEvents {
    PRODUCTION, EDGE, STAGING;

    public static final String HOST_POSTFIX = "shopgun.com/sync";

    static {
        PRODUCTION.mEnvironment = "https://events.service." + HOST_POSTFIX;
        EDGE.mEnvironment = "https://events.service." + HOST_POSTFIX;
        STAGING.mEnvironment = "https://events.service-staging." + HOST_POSTFIX;
    }

    private String mEnvironment;

    /**
     * Convert a string into an {@link EnvironmentEvents}.
     *
     * @param env An environment string
     * @return A matching {@link EnvironmentEvents} or sets the {@link EnvironmentEvents#STAGING} if no match was found
     */
    public static EnvironmentEvents fromString(String env) {
        if (env != null) {
            for (EnvironmentEvents e : EnvironmentEvents.values()) {
                if (env.equalsIgnoreCase(e.mEnvironment)) {
                    return e;
                }
            }
        }
        return STAGING;
    }

    @Override
    public String toString() {
        return mEnvironment;
    }

}
