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
 * The {@link Environment} contains information on how (and where) to contact the ShopGun API.
 * It by default contains three predefined settings, which maps directly to it's counterparts in our API.
 * And furthermore there is an option to set a {@link Environment#CUSTOM} environment, this is for local
 * development and testing.
 *
 * By default, the {@link Environment} is set to {@link Environment#PRODUCTION}.
 */
public enum Environment {
    PRODUCTION, EDGE, STAGING, CUSTOM;

    public static final String HOST_POSTFIX = "etilbudsavis.dk";

    static {
        PRODUCTION.mEnvironment = "https://api." + HOST_POSTFIX;
        EDGE.mEnvironment = "https://api-edge." + HOST_POSTFIX;
        STAGING.mEnvironment = "https://api-staging." + HOST_POSTFIX;
        CUSTOM.mEnvironment = "https://api." + HOST_POSTFIX;
    }

    private String mEnvironment;

    public static void setCustom(String env) {
        if (env == null || env.isEmpty()) {
            CUSTOM.mEnvironment = PRODUCTION.mEnvironment;
        } else {
            CUSTOM.mEnvironment = env;
        }
    }

    /**
     * Convert a string into an {@link Environment}.
     *
     * @param env An environment string
     * @return A matching {@link Environment} or {@link Environment#PRODUCTION} if no match was found
     */
    public static Environment fromString(String env) {
        if (env != null) {
            for (Environment e : Environment.values()) {
                if (env.equalsIgnoreCase(e.mEnvironment)) {
                    return e;
                }
            }
        }
        Environment.setCustom(env);
        return CUSTOM;
    }

    @Override
    public String toString() {
        return mEnvironment;
    }

    /**
     * Apply the environment to a given path/url.
     * <p>
     * The {@link Environment} will only be applied, if the url given is only the path of an url.
     * In a {@link Environment#PRODUCTION PRODUCTION} environment we will translate as follows:
     * <ul>
     * <li>"/v2/catalogs" -&gt; "https://api.etilbudsavis.dk/v2/catalogs"
     * <li><code>null</code> -&gt; "https://api.etilbudsavis.dk/"
     * <li>"" -&gt; "https://api.etilbudsavis.dk/"
     * </ul>
     * </p>
     *
     * @param path The path to apply the given Environment to
     * @return A url
     */
    public String apply(String path) {

        if (path == null || path.isEmpty()) {
            return mEnvironment;
        }

        if (path.startsWith("http://") || path.startsWith("https://")) {
            // cannot prefix environment to existing full url
            return path;
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        return mEnvironment + path;

    }

}
