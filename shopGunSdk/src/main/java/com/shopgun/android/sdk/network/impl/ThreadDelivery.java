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

import com.shopgun.android.sdk.network.Delivery;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.Response;

import java.util.concurrent.ExecutorService;

public class ThreadDelivery implements Delivery {

    /** Used for posting responses, typically to the main thread. */
    private final ExecutorService mExecutorService;

    /**
     * Creates a new response delivery interface.
     * @param handler {@link Handler} to post responses on
     */
    public ThreadDelivery(ExecutorService executor) {
        mExecutorService = executor;
    }

    /**
     * Post the Response to a Request, back to the UI-thread, and then trigger the listener waiting for the callback.
     * @param request made by the user
     * @param a response response from the API fulfilling the Request
     */
    public void postResponse(Request<?> request, Response<?> response) {
        request.addEvent("post-response");
        mExecutorService.submit(new DeliveryRunnable(request, response));
    }

}
