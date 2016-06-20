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

package com.shopgun.android.sdk.log;

import com.shopgun.android.sdk.network.ShopGunError;

import org.json.JSONArray;
import org.json.JSONObject;

public class SgnLogHelper {

    private SgnLogHelper() {
    }

    /**
     * Print a debug log message to LogCat.
     *
     * @param tag      A tag
     * @param name     A name identifying this print
     * @param response A {@link org.json.JSONObject} (ShopGun SDK response), this may be {@code null}
     * @param error    An {@link ShopGunError}, this may be {@code null}
     */
    public static void d(String tag, String name, JSONObject response, ShopGunError error) {
        String resp = response == null ? "null" : response.toString();
        d(tag, name, resp, error);
    }

    /**
     * Print a debug log message to LogCat.
     *
     * @param tag      A tag
     * @param name     A name identifying this print
     * @param response A {@link org.json.JSONArray} (ShopGun SDK response), this may be {@code null}
     * @param error    An {@link ShopGunError}, this may be {@code null}
     */
    public static void d(String tag, String name, JSONArray response, ShopGunError error) {
        String resp = response == null ? "null" : ("size:" + response.length());
        d(tag, name, resp, error);
    }

    /**
     * Print a debug log message to LogCat.
     *
     * @param tag      A tag
     * @param name     A name identifying this print
     * @param response A {@link String} (ShopGun SDK response), this may be {@code null}
     * @param error    An {@link ShopGunError}, this may be {@code null}
     */
    public static void d(String tag, String name, String response, ShopGunError error) {
        String e = error == null ? "null" : error.toJSON().toString();
        String s = response == null ? "null" : response;
        SgnLog.d(tag, name + ": Response[" + s + "], Error[" + e + "]");
    }

}
