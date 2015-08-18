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

package com.shopgun.android.sdk.pageflip;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.pageflip.utils.PageflipUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PageLoader implements ViewTreeObserver.OnPreDrawListener {

    public static final String TAG = PageLoader.class.getSimpleName();

    private Picasso mPicasso;
    private ImageView mImageView;
    private int[] mPages;
    private final Object LOCK = new Object();
    private Bitmap mBitmap;
    private Bitmap.Config mConfig;
    private int mBitmapsLoadedCount = 0;
    private List<String> mUrls = new ArrayList<String>();
    private PageLoaderListener mListener;
    // Keep references to PageTransformerTarget to avoid GC
    private List<PageTransformerTarget> mPageTransformerTargetReference = new ArrayList<PageTransformerTarget>();

    public PageLoader(Context c, List<String> urls, int[] pages) {
        this(Picasso.with(c), urls, pages, PageflipUtils.hasLowMemory(c) ? Bitmap.Config.RGB_565 : Bitmap.Config.ARGB_8888);
    }

    public PageLoader(Picasso picasso, List<String> urls, int[] pages, Bitmap.Config config) {
        mPicasso = picasso;
        mPages = pages;
        mConfig = config;
        mUrls.addAll(urls);
    }

    public void into(ImageView imageView) {
        mImageView = imageView;
        mImageView.getViewTreeObserver().addOnPreDrawListener(this);
        onPreDraw();
    }

    @Override
    public boolean onPreDraw() {

        if (mImageView == null) {
            return true;
        }

        ViewTreeObserver vto = mImageView.getViewTreeObserver();
        if (!vto.isAlive()) {
            return true;
        }

        int width = mImageView.getWidth();
        int height = mImageView.getHeight();

        if (width <= 0 || height <= 0) {
            return true;
        }

        vto.removeOnPreDrawListener(this);


        try {
            loadImages(width, height);
        } catch (Exception e) {
            SgnLog.d(TAG, "mUrls.size: " + mUrls.size());
            SgnLog.d(TAG, mUrls.get(0));
        }

        return true;
    }

    private void loadImages(int width, int height) {

        SgnLog.d(TAG, "pages: " + PageflipUtils.join(",", mPages));
        for (int i = 0; i < mPages.length; i++) {

            int page = mPages[i];
            // convert from human readable to array index
            page--;
            SgnLog.d(TAG, "page: " + page);
            String url = mUrls.get(page);
            PageTransformerTarget t = new PageTransformerTarget(page);
            mPageTransformerTargetReference.add(t);
            mPicasso.load(url)
                    .tag(LOCK)
                    .resize(width, height)
                    .onlyScaleDown()
                    .centerInside()
                    .transform(t)
                    .config(mConfig)
                    .into(t);
        }

    }

    public void cancel() {
        if (mPicasso != null) {
            mPicasso.cancelTag(LOCK);
        }
    }

    public void pause() {
        if (mPicasso != null) {
            mPicasso.pauseTag(LOCK);
        }
    }

    public void resume() {
        if (mPicasso != null) {
            mPicasso.resumeTag(LOCK);
        }
    }

    public void setPageLoaderListener(PageLoaderListener l) {
        mListener = l;
    }

    public PageLoaderListener getPageLoaderListener() {
        return mListener;
    }

    public boolean isDoneLoading() {
        return mBitmapsLoadedCount == mPages.length;
    }

    public interface PageLoaderListener {
        void onComplete();
        void onError();
    }

    public class PageTransformerTarget implements Transformation, Target {

        int mPage;

        public PageTransformerTarget(int page) {
            mPage = page;
        }

        @Override
        public Bitmap transform(Bitmap source) {
//            Log.d(TAG, String.format("transform bitmap[%s, %s]", source.getWidth(), source.getHeight()));
            if (mPages.length == 1) {
                // We don't need to do work on this
                mBitmap = source;
            } else {
                synchronized (LOCK) {
                    mBitmap = createBitmapIfNotExists(mBitmap, source, mConfig, mPages);
                    drawOnto(mBitmap, source, mPage, mPages);
                }
            }
            return source;
        }

        @Override
        public String key() {
            // If we want to get the callback for onBitmapLoaded, then we need random shit here
            // Or find a better way of handling/hacking cache
            return TAG + mPage + new Random().nextFloat();
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mBitmapsLoadedCount++;
            if (isDoneLoading()) {
                Bitmap b = mBitmap == null ? bitmap : mBitmap;
                Log.d(TAG, "onBitmapLoaded bitmap.bytes: " + byteSizeOf(b) / (1024) + "kb");
                mImageView.setImageBitmap(b);
            }
//            Log.d(TAG, "onBitmapLoaded page: " + mPage + ", from: " + from.name());
            if (mListener != null) {
                mListener.onComplete();
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            Log.d(TAG, "onBitmapFailed");
            if (mListener != null) {
                mListener.onError();
            }
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
//            Log.d(TAG, "onPrepareLoad page: " + mPage);
        }

    };

    public static Bitmap createBitmapIfNotExists(Bitmap orig, Bitmap b, Bitmap.Config config, int[] pages) {

        boolean allowRetry = true;
        while (orig == null && allowRetry) {

            try {

                if (pages.length == 1) {
                    return b;
                } else {
                    int w = b.getWidth() * pages.length;
                    int h = b.getHeight();
                    return Bitmap.createBitmap(w, h, config);
                }

            } catch (OutOfMemoryError e) {

                if (allowRetry) {
                    allowRetry = false;
                    SgnLog.e(TAG, e.getMessage(), e);
                    try {
                        // 'force' a GC
                        Runtime.getRuntime().gc();
                        // Wait, and hope for the best
                        Thread.sleep(250);
                    } catch (InterruptedException e1) {
                        SgnLog.e(TAG, "Sleep failed");
                    }
                } else {
                    throw e;
                }

            }

        }

        return orig;

    }

    public static void drawOnto(Bitmap orig, Bitmap tmp, int page, int[] pages) {

        if (orig == null) {
            SgnLog.d(TAG, "Can't draw on double-page-bitmap it's null");
        } else if (orig.isRecycled()) {
            SgnLog.d(TAG, "Can't draw on double-page-bitmap it's recycled");
        } else {
            int pos = page%pages.length;
            int widthOffset = pos * tmp.getWidth();
            Canvas c = new Canvas(orig);
            c.drawBitmap(tmp, widthOffset, 0, null);
        }

    }

    /**
     * returns the bytesize of the give bitmap
     */
    public static int byteSizeOf(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return bitmap.getAllocationByteCount();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        } else {
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }

}
