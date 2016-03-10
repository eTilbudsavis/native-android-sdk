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

import android.os.Handler;
import android.os.Looper;

import com.shopgun.android.sdk.network.Delivery;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.Response;

public class HandlerDelivery implements Delivery {

    /**
     * Used for posting responses, typically to the main thread.
     */
    private final Handler mHandler;

    /**
     * Creates a new response delivery interface, that delivers the response to the UI thread.
     */
    public HandlerDelivery() {
        this(new Handler(Looper.getMainLooper()));
    }

    /**
     * Creates a new response delivery interface, that delivers the response to the thread attached to the delivery.
     *
     * @param handler {@link Handler} to post responses on
     */
    public HandlerDelivery(Handler handler) {
        mHandler = handler;
    }

    /**
     * Post the Response to a Request, back to the UI-thread, and then trigger the listener waiting for the callback.
     *
     * @param request made by the user
     * @param response response response from the API fulfilling the Request
     */
    public void postResponse(Request<?> request, Response<?> response) {

        if (request.isCanceled()) {
            request.finish("cancelled-at-delivery");
        } else if (request.getDelivery() != null && !HandlerDelivery.this.equals(request.getDelivery())) {
            // If there isn't a check, you'll end up in an infinite loop
            request.addEvent("post-to-custom-delivery");
            request.getDelivery().postResponse(request, response);
        } else {
            request.addEvent("post-response");
            mHandler.post(new DeliveryRunnable(request, response));
        }

    }

}