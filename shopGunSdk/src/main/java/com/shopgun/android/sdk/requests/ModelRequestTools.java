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

package com.shopgun.android.sdk.requests;

import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.RequestQueue;

public class ModelRequestTools {

    private ModelRequestTools() {
        // private
    }

    /**
     * Method for applying the current request state to the given request
     *
     * @param from A {@link Request} to copy state from
     * @param to A {@link Request} to copy state to
     */
    public static void copyRequestState(Request<?> from, Request<?> to) {
        to.setDebugger(from.getDebugger());
        to.setTag(from.getTag());
        to.setIgnoreCache(from.ignoreCache());
        to.setTimeOut(from.getTimeOut());
        to.setUseLocation(from.useLocation());
    }

    /**
     * Run the requests needed to load the given requests, model object with the requested data.
     * @param request The calling request
     * @param loaderRequest A {@link LoaderRequest} to load data into the given model
     * @param data The model data
     * @param requestQueue The {@link RequestQueue} to run the {@link LoaderRequest} on
     * @param <T> The model type
     * @return {@code true} of the {@link LoaderRequest} has any {@link Request}'s to perform
     *         (Same as {@link LoaderRequest#isFinished()}), else {@code false}
     */
    public static <T> boolean runLoader(Request<?> request, LoaderRequest<T> loaderRequest, T data, RequestQueue requestQueue) {

        if (loaderRequest != null) {
            if (loaderRequest.getData() == null) {
                loaderRequest.setData(data);
            }
            // Load extra data into result
            ModelRequestTools.copyRequestState(request, loaderRequest);
            requestQueue.add(loaderRequest);
            return !loaderRequest.isFinished();
        }
        return false;
    }

}
