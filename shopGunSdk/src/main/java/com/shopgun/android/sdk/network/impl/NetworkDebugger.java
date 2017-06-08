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

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.RequestDebugger;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.utils.log.Logger;

public class NetworkDebugger implements RequestDebugger {

    public static final String TAG = Constants.getTag(NetworkDebugger.class);

    private final String mTag;
    private final Logger mLogger;

    public NetworkDebugger() {
        this(SgnLog.getLogger(), TAG);
    }

    public NetworkDebugger(String tag) {
        this(SgnLog.getLogger(), tag);
    }

    public NetworkDebugger(Logger logger, String tag) {
        mLogger = logger;
        mTag = tag;
    }

    public void onFinish(Request<?> req) {
        mLogger.d(mTag, req.getNetworkLog().toString());
        mLogger.d(mTag, req.getLog().getString(getClass().getSimpleName()));
    }

    @Override
    public void onDelivery(Request<?> r, Object response, ShopGunError error) {
        mLogger.d(TAG, "TotalDuration: " + r.getLog().getTotalDuration());
    }

}
