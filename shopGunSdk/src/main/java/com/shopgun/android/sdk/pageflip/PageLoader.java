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
import android.os.Parcel;
import android.os.Parcelable;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.model.Catalog;
import com.shopgun.android.sdk.model.Images;
import com.shopgun.android.sdk.pageflip.utils.PageflipUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PageLoader implements ViewTreeObserver.OnPreDrawListener {

    public static final String TAG = PageLoader.class.getSimpleName();

    private Picasso mPicasso;
    private ImageView mImageView;
    private final Object LOCK = new Object();
    private Bitmap mBitmap;
    private Config mConfig;
    private int mBitmapsLoadedCount = 0;
    private PageLoaderListener mListener;
    private Quality mQuality;
    // Keep references to PageTransformerTarget to avoid GC
    private List<PageTransformerTarget> mPageTransformerTargetReference = new ArrayList<PageTransformerTarget>();

    public PageLoader(Context ctx, int[] pages, Catalog c) {
        this(Picasso.with(ctx), new Config(ctx, pages, c));
    }

    public PageLoader(Context ctx, Config config) {
        this(Picasso.with(ctx), config);
    }

    public PageLoader(Picasso picasso, Config config) {
        mPicasso = picasso;
        mConfig = config;
    }

    public void into(ImageView imageView, Quality q) {
        if (imageView == mImageView && mQuality == q) {
            return;
        }
        cancel();
        mQuality = q;
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
        loadImages(width, height);
        return true;
    }

    private void loadImages(int width, int height) {

        for (int i = 0; i < mConfig.getPageCount(); i++) {

            String url = mConfig.getUrl(mQuality, i);
            int page = mConfig.getPage(i);
            PageTransformerTarget t = new PageTransformerTarget(page);
            mPageTransformerTargetReference.add(t);
            RequestCreator rc = mPicasso.load(url);
            rc.tag(LOCK);
            if (mConfig.shouldResize(mQuality)) {
                // we want to resize medium quality views
                rc.resize(width, height);
                rc.onlyScaleDown();
                rc.centerInside();
            }
            rc.transform(t);
            rc.config(mConfig.getBitmapConfig());
            rc.into(t);
        }

    }

    public PageLoader cancel() {
        if (mPicasso != null) {
            mPicasso.cancelTag(LOCK);
        }
        if (mImageView != null) {
            ViewTreeObserver vto = mImageView.getViewTreeObserver();
            if (vto.isAlive()) {
                vto.removeOnPreDrawListener(this);
            }
        }
        mPageTransformerTargetReference.clear();
        mBitmapsLoadedCount = 0;
        return this;
    }

    public PageLoader pause() {
        if (mPicasso != null) {
            mPicasso.pauseTag(LOCK);
        }
        return this;
    }

    public PageLoader resume() {
        if (mPicasso != null) {
            mPicasso.resumeTag(LOCK);
        }
        return this;
    }

    public boolean isDoneLoading() {
        return mBitmapsLoadedCount == mConfig.getPageCount();
    }

    public PageLoader setPageLoaderListener(PageLoaderListener l) {
        mListener = l;
        return this;
    }

    public PageLoaderListener getPageLoaderListener() {
        return mListener;
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
            if (mConfig.isSinglePage()) {
                // We don't need to do work on this
                mBitmap = source;
            } else {
                synchronized (LOCK) {
                    int[] pages = mConfig.getPages();
                    mBitmap = createBitmapIfNotExists(mBitmap, source, mConfig.getBitmapConfig(), pages);
                    drawOnto(mBitmap, source, mPage, pages);
                }
            }
            return source;
        }

        @Override
        public String key() {
//            if (mConfig.isSinglePage()) {
//                int p = mConfig.getPage(0);
//                String url = mConfig.getUrl(mQuality, 0);
//                return String.valueOf(url+p);
//            }
            // If we want to get the callback for onBitmapLoaded, then we need random shit here
            // Or find a better way of handling/hacking cache
            return TAG + mPage + new Random().nextFloat();
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            mBitmapsLoadedCount++;
            if (isDoneLoading()) {
                boolean bn = mBitmap == null;
                Bitmap b = bn ? bitmap : mBitmap;
                SgnLog.d(TAG, "mBitmap.size: " + PageflipUtils.sizeOfKb(b) + "kb");
                loadLog("onBitmapLoaded");
                loadLog("- " + mConfig.toString());
                loadLog("- mBitmap.bytes: " + PageflipUtils.sizeOfKb(b) + "kb");
                mImageView.setImageBitmap(b);

                if (mListener != null) {
                    mListener.onComplete();
                }

                // Don't want to hold reference to this chunk of mem
                mBitmap = null;
            }

        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            loadLog("onBitmapFailed");
            if (mListener != null) {
                mListener.onError();
            }
            // Don't want to hold reference to this chunk of mem
            mBitmap = null;
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            loadLog("onPrepareLoad");
        }

        private void loadLog(String s) {
//            String pages = PageflipUtils.join(",", mConfig.getPages());
//            SgnLog.d(TAG, String.format("pages[%s], %s", pages, s));
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
            SgnLog.w(TAG, "Can't draw on double-page-bitmap it's null");
        } else if (orig.isRecycled()) {
            SgnLog.w(TAG, "Can't draw on double-page-bitmap it's recycled");
        } else {
            int pos = getPosition(page, pages);
            int widthOffset = pos * tmp.getWidth();
            Canvas c = new Canvas(orig);
            c.drawBitmap(tmp, widthOffset, 0, null);
        }

    }

    private static int getPosition(int page, int[] pages) {
        for (int i = 0; i < pages.length; i++) {
            if (page == pages[i]) {
                return i;
            }
        }
        return -1;
    }

    public enum Quality {
        LOW, MEDIUM, HIGH
    }

    public static class Config implements Parcelable {

        private int mMaxMem;
        private int[] mPages;
        private List<String> mLow;
        private List<String> mMedium;
        private List<String> mHigh;
        Bitmap.Config mConfig;

        public Config(Context ctx, int[] pages, Catalog c) {
            int heap = PageflipUtils.getMaxHeap(ctx);
            set(heap, pages, c);
        }

        public Config(int heap, int[] pages, Catalog c) {
            set(heap, pages, c);
        }

        private void set(int maxMem, int[] pages, Catalog c) {
            mMaxMem = maxMem;
            mPages = pages;
            // Thumbs are worthless as they have white padding... argh!
            mLow = getPagesUrls(c, Quality.MEDIUM, mPages);
            mMedium = getPagesUrls(c, Quality.MEDIUM, mPages);

            /*
            Calculating worst case scenario is landscape mode
            (each view has two bitmaps), with high quality images.

            Memory not included:
            - The bitmap being replaced in the view
            - The drawing cache in the PageTransformerTarget
            - Bitmaps that have not yet been recycled

            Overkill:
            - Technically only two zoom images will be loaded (depending on lifecycle e.t.c.)
            - Screen-size/View-size will possibly force down scale og bitmap.

            ------------------------------------------------------------
            |                 Decoding memory consumption              |
            ------------------------------------------------------------
            |                    | RGB_565 (2byte) | ARGB_8888 (4byte) |
            ------------------------------------------------------------
            | thumb (177x212)px  |            75kb |             150kb |
            | view (800x1000)px  |          1600kb |            3200kb |
            | zoom (1500x2000)px |          6000kb |           12000kb |
            ------------------------------------------------------------

            */

            if (mMaxMem >= 96) {
                // worst case: (2*12000)*3 = 72mb
                mConfig = Bitmap.Config.ARGB_8888;
                mHigh = getPagesUrls(c, Quality.HIGH, mPages);
            } else if (mMaxMem >= 48) {
                // worst case: (2*6000)*3 = 36mb
                mConfig = Bitmap.Config.RGB_565;
                mHigh = getPagesUrls(c, Quality.HIGH, mPages);
            } else {
                // worst case: (2*1600)*3 = 10mb
                // essentially we just hope for the best
                mConfig = Bitmap.Config.RGB_565;
                mHigh = mMedium;
            }

        }

        public List<String> getPagesUrls(Catalog c, Quality q, int[] pages) {
            List<Images> images = c.getPages();
            List<String> urls = new ArrayList<String>(pages.length);
            for (int i = 0; i < pages.length; i++) {
                int pos = pages[i]-1;
                Images img = images.get(pos);
                switch (q) {
                    case LOW:
                        urls.add(img.getThumb());
                        break;
                    case MEDIUM:
                        urls.add(img.getView());
                        break;
                    case HIGH:
                        urls.add(img.getZoom());
                        break;
                }
            }
            return urls;
        }

        public int getPage(int position) {
            return mPages[position];
        }

        public String getUrl(Quality q, int position) {
            switch (q) {
                case LOW:
                    return mLow.get(position);
                case MEDIUM:
                    return mMedium.get(position);
                case HIGH:
                    return mHigh.get(position);
                default:
                    return null;
            }
        }

        public int getPageCount() {
            return mPages.length;
        }

        public boolean isSinglePage() {
            return mPages.length == 1;
        }

        public Bitmap.Config getBitmapConfig() {
            return mConfig;
        }

        public int[] getPages() {
            return mPages;
        }

        public boolean shouldResize(Quality q) {
            return q != Quality.HIGH;
        }

        @Override
        public String toString() {
            String format = "%s[maxMem:%s, pages:%s, Bitmap.Config:%s, url.isSame:%s]";
            return String.format(format, getClass().getSimpleName(), mMaxMem, PageflipUtils.join(",", mPages), mConfig.toString(), (mMedium == mHigh));
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mMaxMem);
            dest.writeIntArray(this.mPages);
            dest.writeStringList(this.mLow);
            dest.writeStringList(this.mMedium);
            dest.writeStringList(this.mHigh);
            dest.writeInt(this.mConfig == null ? -1 : this.mConfig.ordinal());
        }

        protected Config(Parcel in) {
            this.mMaxMem = in.readInt();
            this.mPages = in.createIntArray();
            this.mLow = in.createStringArrayList();
            this.mMedium = in.createStringArrayList();
            this.mHigh = in.createStringArrayList();
            int tmpMConfig = in.readInt();
            this.mConfig = tmpMConfig == -1 ? null : Bitmap.Config.values()[tmpMConfig];
        }

        public static final Creator<Config> CREATOR = new Creator<Config>() {
            public Config createFromParcel(Parcel source) {
                return new Config(source);
            }

            public Config[] newArray(int size) {
                return new Config[size];
            }
        };
    }


}
