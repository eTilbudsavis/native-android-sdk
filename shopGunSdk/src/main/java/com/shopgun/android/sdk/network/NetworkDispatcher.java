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

import android.os.Process;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.utils.Api.Endpoint;
import com.shopgun.android.sdk.utils.Constants;
import com.shopgun.android.utils.HashUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class NetworkDispatcher extends Thread {

    public static final String TAG = Constants.getTag(NetworkDispatcher.class);

    /** ShopGun object controlling the whole lot */
    private final ShopGun mShopGun;

    /** The queue of requests to service. */
    private final BlockingQueue<Request<?>> mQueue;

    /** The RequestQueue this NetworkDispatcher receives Requests from */
    private final RequestQueue mRequestQueue;

    /** The network interface for processing requests. */
    private final Network mNetwork;

    /** The cache to write to. */
    private final Cache mCache;

    /** For posting responses and errors. */
    private final Delivery mDelivery;

    /** Used for telling us to die. */
    private volatile boolean mQuit = false;

    public NetworkDispatcher(ShopGun shopGun, RequestQueue requestQueue, BlockingQueue<Request<?>> queue, Network network, Cache cache, Delivery delivery) {
        mQueue = queue;
        mNetwork = network;
        mCache = cache;
        mDelivery = delivery;
        mRequestQueue = requestQueue;
        mShopGun = shopGun;
    }

    /**
     * Terminate this NetworkDispatcher. Once terminated, no further requests will be processed.
     */
    public void quit() {
        mQuit = true;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        Request<?> request;
        while (true) {
            try {
                // Take a request from the queue.
                request = mQueue.take();
            } catch (InterruptedException e) {
                // We may have been interrupted because it was time to quit.
                if (mQuit) {
                    return;
                }
                continue;
            }

            try {

                // If the request was cancelled already, do not perform the network request.
                if (request.isCanceled()) {
                    request.finish("network-dispatcher-cancelled-on-recieved");
                    continue;
                } else {
                    request.addEvent("recieved-by-network-dispatcher");
                }

                prepare(request);

                // Perform the network request.
                NetworkResponse networkResponse = mNetwork.performRequest(request);

                appendLogging(request, networkResponse);

                request.addEvent("parsing-network-response");
                Response<?> response = request.parseNetworkResponse(networkResponse);

                if (response.isSuccess()) {

                    mCache.put(request, response);

                } else {

                    SgnLog.d("Network dispatcher",
                            String.format(Locale.US, "Error code = %d, %s. JSON = %s", response.error.getCode(), response.error.getDetails(), response.error.toString()));
                }

                mDelivery.postResponse(request, response);


            } catch (ShopGunError e) {

                request.addEvent("network-error");
                mDelivery.postResponse(request, Response.fromError(e));

            }
        }
    }

    /**
     *  If it's a post to sessions, it's to create a new Session, then the API key is needed.
     *  In any other case, just set the headers, with the current session token and signature.
     * @param request
     */
    private void prepare(Request<?> request) {

        request.addEvent("preparing-headers");

        // todo: add headers to the remaining requests to v2. X-Token deleted
        Map<String, String> headers = new HashMap<>();
        String sha256 = HashUtils.sha256(mShopGun.getApiSecret());
        headers.put("X-Signature", sha256);
        request.setHeaders(headers);

    }

    private void appendLogging(Request<?> req, NetworkResponse resp) {

        try {

            // Server Response
            JSONObject r = new JSONObject();
            r.put("statuscode", resp.statusCode);
            if (resp.headers != null) {
                r.put("headers", new JSONObject(resp.headers));
            }
            req.getNetworkLog().put("response", r);

        } catch (JSONException e) {
            SgnLog.e(TAG, "", e);
        }

    }

}
