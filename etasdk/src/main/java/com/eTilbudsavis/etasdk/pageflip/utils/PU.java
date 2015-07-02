package com.eTilbudsavis.etasdk.pageflip.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.Toast;

import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.imageloader.FileNameGenerator;
import com.eTilbudsavis.etasdk.imageloader.ImageDebugger;
import com.eTilbudsavis.etasdk.imageloader.ImageRequest;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.Catalog;
import com.eTilbudsavis.etasdk.model.Images;

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
        Eta.getInstance().getImageloader().displayImage(ir);
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

