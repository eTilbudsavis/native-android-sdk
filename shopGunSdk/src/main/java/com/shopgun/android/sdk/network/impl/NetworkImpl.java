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
import com.shopgun.android.sdk.network.HttpStack;
import com.shopgun.android.sdk.network.Network;
import com.shopgun.android.sdk.network.NetworkResponse;
import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.utils.Constants;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.ByteArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class NetworkImpl implements Network {

    public static final String TAG = Constants.getTag(NetworkImpl.class);

    private static final int BUFFER_SIZE = 0x1000; // 4K

    HttpStack mStack;

    public NetworkImpl(HttpStack stack) {
        mStack = stack;
    }

    private static byte[] entityToBytes(HttpEntity entity) throws IllegalStateException, IOException {

        // Find best buffer size
        int init_buf = 0 <= entity.getContentLength() ? (int) entity.getContentLength() : BUFFER_SIZE;

        ByteArrayBuffer bytes = new ByteArrayBuffer(init_buf);

        InputStream is = entity.getContent();
        if (is == null)
            return bytes.toByteArray();

        byte[] buf = new byte[init_buf];
        int c = -1;
        while ((c = is.read(buf)) != -1) {
            bytes.append(buf, 0, c);
        }

        return bytes.toByteArray();
    }

    public NetworkResponse performRequest(Request<?> request) throws ShopGunError {

        byte[] content;
        Map<String, String> responseHeaders = new HashMap<String, String>();
        try {

            HttpResponse resp = mStack.performNetworking(request);

            if (resp.getEntity() == null) {
                // add 0-byte for to mock no-content
                content = new byte[0];
            } else {
                request.addEvent("reading-input");
                content = entityToBytes(resp.getEntity());
            }

			/*
			 * TODO report back content and body length, to collect stats on
			 * transferred data, to compare with MsgPack later.
			 */
            int respLength = content.length;
            int bodyLength = (request.getBody() == null ? 0 : request.getBody().length);

            request.stats(respLength, bodyLength);

            for (org.apache.http.Header h : resp.getAllHeaders()) {
                responseHeaders.put(h.getName(), h.getValue());
            }

            return new NetworkResponse(resp.getStatusLine().getStatusCode(), content, responseHeaders);

        } catch (Exception e) {
            SgnLog.e(TAG, e.getMessage(), e);
            throw new NetworkError(e);
        }

    }

}
