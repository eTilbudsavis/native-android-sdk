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

package com.shopgun.android.sdk.pageflip.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.Toast;

import com.shopgun.android.sdk.ShopGun;
import com.shopgun.android.sdk.imageloader.FileNameGenerator;
import com.shopgun.android.sdk.imageloader.ImageDebugger;
import com.shopgun.android.sdk.imageloader.ImageRequest;
import com.shopgun.android.sdk.log.EtaLog;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.Images;

/**
 * Methods in this class <b>CHANGES A LOT</b>, and will break your stuff.
 * Please don't use them for anything serious.
 *
 * @author Danny Hvam - danny@etilbudsavis.dk
 */
public class PU {

    private PU() {
    }

    public static ImageDebugger getSimpleDebugger(final String tag) {
        return new ImageDebugger() {

            public void debug(ImageRequest ir) {
                EtaLog.d(tag, ir.getFileName() + ", " + ir.getLog().getTotalDuration());
            }
        };
    }

    public static void cacheAllImages(final String tag, final Context ctx, final Catalog c) {
        Runnable downloader = new Runnable() {

            public void run() {

                toast(ctx, "Downloading " + c.getBranding().getName());
                int count = 0;
                ImageRequest t = null;
                ImageRequest v = null;
                ImageRequest z = null;

                for (Images i : c.getPages()) {

                    while ((t != null && v != null && z != null) &&
                            (!t.isFinished() || !v.isFinished() || !z.isFinished())) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            // nothing really
                        }
                    }

                    count++;

                    // Get thumb
                    t = display(i.getThumb(), ctx);
                    v = display(i.getView(), ctx);
                    z = display(i.getZoom(), ctx);

                    // Give feedback
                    String s = String.format("%s / %s", count, c.getPageCount());
                    EtaLog.d(tag, s);
                    if (count % 5 == 0) {
                        toast(ctx, s);
                    }

                }
                toast(ctx, "Finished downloading " + c.getBranding().getName());
            }
        };
        new Thread(downloader).start();
    }

    private static ImageRequest display(String url, Context ctx) {
        ImageRequest ir = new ImageRequest(url, new ImageView(ctx));
        ir.setFileName(new PageflipFileNameGenerator());
        ShopGun.getInstance().getImageloader().displayImage(ir);
        return ir;
    }

    private static void toast(final Context ctx, final String s) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {

            public void run() {
                Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class PageflipFileNameGenerator implements FileNameGenerator {

        public String getFileName(ImageRequest ir) {
            String s[] = ir.getUrl().split("/");
            int l = s.length - 1;
            return s[l - 1] + "-" + s[l];
        }

    }

}

