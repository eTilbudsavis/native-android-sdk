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

package com.shopgun.android.sdk.log;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.view.View;

import com.shopgun.android.sdk.network.Request;
import com.shopgun.android.sdk.network.ShopGunError;
import com.shopgun.android.sdk.pageflip.utils.PageflipUtils;

import org.json.JSONArray;
import org.json.JSONObject;

public class SgnLogHelper {

    private SgnLogHelper() {
    }

    /**
     * Print a debug log message to LogCat.
     *
     * @param tag      A tag
     * @param name     A name identifying this print
     * @param response A {@link org.json.JSONObject} (ShopGun SDK response), this may be {@code null}
     * @param error    An {@link ShopGunError}, this may be {@code null}
     */
    public static void d(String tag, String name, JSONObject response, ShopGunError error) {
        String resp = response == null ? "null" : response.toString();
        d(tag, name, resp, error);
    }

    /**
     * Print a debug log message to LogCat.
     *
     * @param tag      A tag
     * @param name     A name identifying this print
     * @param response A {@link org.json.JSONArray} (ShopGun SDK response), this may be {@code null}
     * @param error    An {@link ShopGunError}, this may be {@code null}
     */
    public static void d(String tag, String name, JSONArray response, ShopGunError error) {
        String resp = response == null ? "null" : ("size:" + response.length());
        d(tag, name, resp, error);
    }

    /**
     * Print a debug log message to LogCat.
     *
     * @param tag      A tag
     * @param name     A name identifying this print
     * @param response A {@link String} (ShopGun SDK response), this may be {@code null}
     * @param error    An {@link ShopGunError}, this may be {@code null}
     */
    public static void d(String tag, String name, String response, ShopGunError error) {
        String e = error == null ? "null" : error.toJSON().toString();
        String s = response == null ? "null" : response;
        SgnLog.d(tag, name + ": Response[" + s + "], Error[" + e + "]");
    }

    public static void logInvalidSignature(String tag, Request<?> request, ShopGunError e) {

        if (e.getCode() == 1104) {

            StringBuilder sb = new StringBuilder();
            sb.append(request.getNetworkLog().toString());
            sb.append("/n");
            sb.append(request.getLog().getString("Invalid Signature"));
            sb.append("/n");
            sb.append(e.toJSON().toString());

            SgnLog.d(tag, sb.toString());
        }

    }

    public static void printViewDimen(String tag, String viewName, View v) {
        SgnLog.d(tag, viewName + ", getWidth: " + v.getWidth() + ", getHeight: " + v.getHeight());
        SgnLog.d(tag, viewName + ", getMeasuredWidth: " + v.getMeasuredWidth() + ", getMeasuredHeight: " + v.getMeasuredHeight());
    }

    public static void printBitmapInfo(String tag, Bitmap b) {
        printBitmapInfo(tag, null, b);
    }

    public static void printBitmapInfo(String tag, String infp, Bitmap b) {
        int w = b.getWidth();
        int h = b.getHeight();
        float size = ((float) (w * h * 4) / (float) (1024 * 1024));
        String text = null;
        if (infp == null) {
            String format = "Bitmap[w:%s, h:%s, %.2fmb]";
            text = String.format(format, w, h, size);
        } else {
            String format = "Bitmap[info:%s, w:%s, h:%s, %.2fmb]";
            text = String.format(format, infp, w, h, size);
        }
        SgnLog.d(tag, text);
    }

    public static void printScreen(String tag, Context c) {
        Point p = PageflipUtils.getDisplayDimen(c);
        String out = String.format("ScreenSize[w:%s, h:%s]", p.x, p.y);
        SgnLog.d(tag, out);
    }

    public static void printOptions(String tag, BitmapFactory.Options o) {
        String format = "Image[MimeType:%s, w:%s, h:%s]";
        String out = String.format(format, o.outMimeType, o.outWidth, o.outHeight);
        SgnLog.d(tag, out);
    }

}
