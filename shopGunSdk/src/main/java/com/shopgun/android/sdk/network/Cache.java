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

    public void put(Request<?> request, Response<?> response);

    public Cache.Item get(String key);

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
         *
         * this is based on the time to live factor
         *
         * @return
         */
        public boolean isExpired() {
            return expires < System.currentTimeMillis();
        }

    }

}
