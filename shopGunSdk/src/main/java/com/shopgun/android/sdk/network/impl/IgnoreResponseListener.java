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
import com.shopgun.android.sdk.network.Response;
import com.shopgun.android.sdk.network.ShopGunError;

import java.util.List;

public class IgnoreResponseListener<T> implements Response.Listener<T> {

    public static final String TAG = IgnoreResponseListener.class.getSimpleName();

    boolean mDebug = false;

    public IgnoreResponseListener(boolean debug) {
        mDebug = debug;
    }

    public IgnoreResponseListener() {
        this(false);
    }

    @Override
    public void onComplete(T response, ShopGunError error) {
        if (mDebug) {
            if (response instanceof List) {
                response(TAG, TAG, ((List) response), error);
            } else {
                response(TAG, TAG, response, error);
            }
        }
    }

    private static void response(String tag, String name, List<?> response, ShopGunError error) {
        if (error == null) {
            String clazz = response.isEmpty() ? "unknown" : response.get(0).getClass().getSimpleName();
            SgnLog.d(tag, name + ": response<" + clazz + ">.size: " + response.size());
        } else {
            SgnLog.d(tag, name + ": error - " + error.toString());
        }
    }

    private static void response(String tag, String name, Object response, ShopGunError error) {
        if (error == null) {
            String clazz = response == null ? "null" : response.getClass().getSimpleName();
            SgnLog.d(tag, name + ": response<" + clazz + "> - " + String.valueOf(response));
        } else {
            SgnLog.d(tag, name + ": error - " + error.toString());
        }
    }

}
