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

package com.shopgun.android.sdk.pagedpublicationkit.impl.apiv2.quatzel.impl;


import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.Response;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.network.impl.JsonObjectRequest;
import com.shopgun.android.sdk.pagedpublicationkit.impl.apiv2.quatzel.StatDelivery;
import com.shopgun.android.sdk.utils.Constants;

import org.json.JSONObject;

public class StatDeliveryImpl implements StatDelivery {

    public static final String TAG = Constants.getTag(StatDeliveryImpl.class);

    private ShopGun mShopgun;
    private boolean mDebug = false;

    public StatDeliveryImpl(ShopGun sgn) {
        this(sgn, false);
    }

    public StatDeliveryImpl(ShopGun sgn, boolean debug) {
        mShopgun = sgn;
        mDebug = debug;
    }

    @Override
    public boolean deliver(final String url, final JSONObject data) {
        JsonObjectRequest r = new JsonObjectRequest(Request.Method.POST, url, data, new Response.Listener<JSONObject>() {

            public void onComplete(JSONObject response, ShopGunError error) {
                print(url, data, response, error);
            }
        });
        mShopgun.add(r);
        return true;
    }

    private void print(String url, JSONObject data, JSONObject response, ShopGunError error) {
        if (mDebug) {
            String status = response != null ? "OK" : "FAILED";
            String text = String.format("Collect[ status:%s url:%s, data:%s]", status, url, data.toString());
            SgnLog.d(TAG, text);
        }
    }

}
