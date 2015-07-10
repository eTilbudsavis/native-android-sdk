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

package com.shopgun.android.sdk.imageloader.impl;

import android.graphics.Bitmap;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.imageloader.ImageDownloader;
import com.shopgun.android.sdk.imageloader.ImageRequest;

import org.apache.http.util.ByteArrayBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DefaultImageDownloader implements ImageDownloader {

    public static final String TAG = Constants.getTag(ImageDownloader.class);

    private static final int BUFFER_SIZE = 0x10000;
    private static final int TIMEOUT = 20000;

    private static byte[] entityToBytes(HttpURLConnection connection) throws IOException {

        int init_buf = (0 <= connection.getContentLength() ? (int) connection.getContentLength() : BUFFER_SIZE);

        ByteArrayBuffer bytes = new ByteArrayBuffer(init_buf);
        InputStream is = connection.getInputStream();
//		SgnLog.d(TAG, "InputStream: " + is.getClass().getSimpleName());
        if (is != null) {

            byte[] buf = new byte[init_buf];
            int c = -1;
            while ((c = is.read(buf)) != -1) {
                bytes.append(buf, 0, c);
            }

        }

        return bytes.toByteArray();
    }

    public Bitmap getBitmap(ImageRequest ir) throws IllegalStateException, IOException, OutOfMemoryError {
        return ir.getBitmapDecoder().decode(ir, getByteArray(ir));
    }

    public byte[] getByteArray(ImageRequest ir) throws IllegalStateException, IOException, OutOfMemoryError {
        URL imageUrl = new URL(ir.getUrl());
        HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
        conn.setConnectTimeout(TIMEOUT);
        conn.setReadTimeout(TIMEOUT);
        conn.setInstanceFollowRedirects(true);
        byte[] image = entityToBytes(conn);
        return image;
    }

}
