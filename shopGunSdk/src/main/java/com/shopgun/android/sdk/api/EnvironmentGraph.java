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
 * The {@link EnvironmentGraph} contains the graph endpoints.
 *
 * By default, the {@link EnvironmentGraph} is set to {@link EnvironmentGraph#PRODUCTION}.
 */
public enum EnvironmentGraph {
    PRODUCTION, EDGE, STAGING, SQUID;

    public static final String HOST_POSTFIX = "shopgun.com";

    static {
        PRODUCTION.mEnvironment = "https://graph.service." + HOST_POSTFIX;
        EDGE.mEnvironment = "https://graph.service." + HOST_POSTFIX;
        STAGING.mEnvironment = "https://graph.service-staging." + HOST_POSTFIX;
        SQUID.mEnvironment = "https://squid.service-staging." + HOST_POSTFIX;
    }

    private String mEnvironment;

    /**
     * Convert a string into an {@link EnvironmentGraph}.
     *
     * @param env An environment string
     * @return A matching {@link EnvironmentGraph} or sets the {@link EnvironmentGraph#STAGING} if no match was found
     */
    public static EnvironmentGraph fromString(String env) {
        if (env != null) {
            for (EnvironmentGraph e : EnvironmentGraph.values()) {
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
