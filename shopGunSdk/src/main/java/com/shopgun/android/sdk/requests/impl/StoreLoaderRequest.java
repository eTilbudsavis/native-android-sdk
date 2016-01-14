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

package com.shopgun.android.sdk.requests.impl;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.model.Store;
import com.shopgun.android.sdk.requests.ModelLoaderRequest;

public class StoreLoaderRequest extends ModelLoaderRequest<Store> {

    public static final String TAG = Constants.getTag(StoreLoaderRequest.class);

    public StoreLoaderRequest(Store s, Listener<Store> listener) {
        super(s, listener);
    }

    @Override
    public boolean loadDealer() {
        return super.loadDealer();
    }

    @Override
    public void loadDealer(boolean dealer) {
        super.loadDealer(dealer);
    }

}
