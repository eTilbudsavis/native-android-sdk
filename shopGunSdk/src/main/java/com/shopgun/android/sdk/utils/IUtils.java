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

import com.shopgun.android.sdk.model.interfaces.ICatalog;
import com.shopgun.android.sdk.model.interfaces.IDealer;
import com.shopgun.android.sdk.model.interfaces.IErn;
import com.shopgun.android.sdk.model.interfaces.IStore;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IUtils {

    private IUtils() {
        // private
    }

    public static Set<String> getIds(List<? extends IErn<?>> list) {
        Set<String> ids = new HashSet<String>(list.size());
        for (IErn ern : list) {
            ids.add(ern.getId());
        }
        return ids;
    }

    public static Set<String> toErnSet(List<? extends IErn<?>> list) {
        Set<String> set = new HashSet<String>();
        for (IErn<?> ern : list) {
            set.add(ern.getErn());
        }
        return set;
    }

    public static Set<String> getStoreIds(List<? extends IStore<?>> list) {
        Set<String> ids = new HashSet<String>(list.size());
        for (IStore is : list) {
            if (is.getStore() == null) {
                ids.add(is.getStoreId());
            }
        }
        return ids;
    }

    public static Set<String> getDealerIds(List<? extends IDealer<?>> list) {
        Set<String> ids = new HashSet<String>(list.size());
        for (IDealer is : list) {
            if (is.getDealer() == null) {
                ids.add(is.getDealerId());
            }
        }
        return ids;
    }

    public static Set<String> getCatalogIds(List<? extends ICatalog<?>> list, boolean forceReplace) {
        Set<String> ids = new HashSet<String>(list.size());
        for (ICatalog ic : list) {
            if (ic.getCatalog() == null || forceReplace) {
                ids.add(ic.getCatalogId());
            }
        }
        return ids;
    }


}
