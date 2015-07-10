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

package com.shopgun.android.sdk.network.impl;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.RequestDebugger;

public class NetworkDebugger implements RequestDebugger {

    public static final String TAG = Constants.getTag(NetworkDebugger.class);

    public void onFinish(Request<?> req) {
        SgnLog.d(TAG, req.getNetworkLog().toString());
        SgnLog.d(TAG, req.getLog().getString(getClass().getSimpleName()));
    }

    public void onDelivery(Request<?> r) {
        SgnLog.d(TAG, "TotalDuration: " + r.getLog().getTotalDuration());
    }

}
