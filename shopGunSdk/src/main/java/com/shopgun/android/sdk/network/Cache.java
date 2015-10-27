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

package com.shopgun.android.sdk.network;

import java.io.Serializable;

public interface Cache {

    /**
     * Add a request to the cache. Only successful responses where the {@link Request#getMethod()} is a
     * {@link com.shopgun.android.sdk.network.Request.Method#GET} may be cached.
     * @param request The request performed
     * @param response The response returned from the API, to the given {@link Request}
     */
    public void put(Request<?> request, Response<?> response);

    /**
     * Get a {@link com.shopgun.android.sdk.network.Cache.Item} from this cache.
     * @param key A key
     * @return A {@link com.shopgun.android.sdk.network.Cache.Item} if a valid item is found, else {@code null}
     */
    public Cache.Item get(String key);

    /**
     * Invalidate the cache
     */
    public void clear();


    public static class Item implements Serializable {

        private static final long serialVersionUID = 1L;

        // Time of insertion
        public final long expires;
        public final Object object;
        public long size;

        public Item(Object o, long timeToLive) {
            this.expires = System.currentTimeMillis() + timeToLive;
            this.object = o;
        }

        /**
         * Returns true if the Item is still valid.
         * this is based on the time to live factor
         * @return {@code true} if the item is expired, else {@code false}
         */
        public boolean isExpired() {
            return expires < System.currentTimeMillis();
        }

    }

}
