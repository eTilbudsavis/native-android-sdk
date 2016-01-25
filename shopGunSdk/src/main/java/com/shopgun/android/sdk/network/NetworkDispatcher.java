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

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.SessionManager;
import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.network.Request.Method;
import com.shopgun.android.sdk.utils.Api.Endpoint;
import com.shopgun.android.sdk.utils.HashUtils;
import com.shopgun.android.sdk.utils.HeaderUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
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

                    updateSessionInfo(networkResponse.headers);
                    mCache.put(request, response);
                    mDelivery.postResponse(request, response);

                } else {

                    if (SessionManager.recoverableError(response.error)) {

                        request.addEvent("recoverable-session-error");

                        if (isSessionEndpoint(request)) {

                            mDelivery.postResponse(request, response);

                        } else {

                            // Query the session manager to perform an update
                            if (mShopGun.getSessionManager().recover(response.error)) {
                                mRequestQueue.add(request);
                            } else {
                                mDelivery.postResponse(request, response);
                            }

                        }

                    } else {

                        request.addEvent("non-recoverable-error");
                        mDelivery.postResponse(request, response);

                    }

                }


            } catch (ShopGunError e) {

                request.addEvent("network-error");
                mDelivery.postResponse(request, Response.fromError(e));

            }
        }
    }

    /**
     * Wrapper to check for session endpoint
     * @param request to check
     * @return true if session-endpoint, eler false
     */
    private boolean isSessionEndpoint(Request<?> request) {
        return request.getUrl().contains(Endpoint.SESSIONS);
    }

    /**
     *  If it's a post to sessions, it's to create a new Session, then the API key is needed.
     *  In any other case, just set the headers, with the current session token and signature.
     * @param request
     */
    private void prepare(Request<?> request) {

        request.addEvent("preparing-headers");

        boolean newSession = (request.getMethod() == Method.POST && request.getUrl().contains(Endpoint.SESSIONS));

        if (!newSession) {

            Map<String, String> headers = new HashMap<String, String>();
            String token = mShopGun.getSessionManager().getSession().getToken();
            headers.put(HeaderUtils.X_TOKEN, token);
            String sha256 = HashUtils.sha256(mShopGun.getApiSecret() + token);
            headers.put(HeaderUtils.X_SIGNATURE, sha256);
            request.setHeaders(headers);

        }

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

    /**
     * Method checks headers to find X-Token and X-Token-Expires.<br>
     * If they do not exist, nothing happens as the call has a wrong endpoint, or other
     * non-API regarding error. If they do exist, then they are checked by the Session
     * to find out if there are any changes.
     * @param headers to check for new token.
     */
    private void updateSessionInfo(Map<String, String> headers) {
        if (headers != null) {
            String token = headers.get(HeaderUtils.X_TOKEN);
            String expire = headers.get(HeaderUtils.X_TOKEN_EXPIRES);

            if (!(token == null || expire == null)) {
                mShopGun.getSessionManager().updateTokens(token, expire);
            }
        }

    }

}
