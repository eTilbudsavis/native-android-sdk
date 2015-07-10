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

package com.shopgun.android.sdk.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.SgnThreadFactory;
import com.shopgun.android.sdk.imageloader.impl.DefaultBitmapDecoder;
import com.shopgun.android.sdk.imageloader.impl.DefaultFileCache;
import com.shopgun.android.sdk.imageloader.impl.DefaultImageDownloader;
import com.shopgun.android.sdk.imageloader.impl.LruMemoryCache;
import com.shopgun.android.sdk.log.SgnLog;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {

//	public static final String TAG = Constants.getTag(

    public static final String TAG = Constants.getTag(ImageLoader.class);

    private Map<ImageView, String> mImageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    private MemoryCache mMemoryCache;
    private FileCache mFileCache;
    private ExecutorService mExecutor;
    private ImageDownloader mDownloader;
    private Handler mHandler;

    public ImageLoader(Context c) {
        this(c, Executors.newFixedThreadPool(3, new SgnThreadFactory("eta-il-")));
    }

    public ImageLoader(Context c, ExecutorService executor) {
        this(executor, new LruMemoryCache(), new DefaultFileCache(c, executor), new DefaultImageDownloader());
    }

    public ImageLoader(ExecutorService executor, MemoryCache mamoryCache, FileCache fileCache, ImageDownloader downloader) {
        mExecutor = executor;
        mMemoryCache = mamoryCache;
        mFileCache = fileCache;
        mDownloader = downloader;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void displayImage(ImageRequest ir) {
        ir.add("start-image-request");
        if (ir.getImageView().getTag() == null) {
            ir.getImageView().setTag(ir.getUrl());
        }

        if (ir.getBitmapDecoder() == null) {
            ir.setBitmapDecoder(new DefaultBitmapDecoder());
        }
        mImageViews.put(ir.getImageView(), ir.getUrl());

        ir.setBitmap(mMemoryCache.get(ir.getUrl()));
        if (ir.getBitmap() != null) {
            ir.setLoadSource(LoadSource.MEMORY);
            ir.add("loaded-from-" + ir.getLoadSource());
            processAndDisplay(ir);
        } else {
            mExecutor.execute(new PhotosLoader(ir));
            if (ir.getPlaceholderLoading() != 0) {
                ir.getImageView().setImageResource(ir.getPlaceholderLoading());
            }
        }
    }

    private void addToCache(ImageRequest ir, byte[] image) {

        // This is a bit messy, but i'll cleanup later

        if (image == null || ir.getBitmap() == null || ir.getLoadSource() == null) {
            ir.add("cannot-cache-request");
            return;
        }

        LoadSource s = ir.getLoadSource();
        if (s == LoadSource.WEB) {

            if (ir.useFileCache()) {
                ir.add("adding-to-file-cache");
                mFileCache.save(ir, image);
            }
            if (ir.useMemoryCache()) {
                ir.add("adding-to-memory-cache");
                mMemoryCache.put(ir.getUrl(), ir.getBitmap());
            }

        } else if (s == LoadSource.FILE) {

            if (ir.useMemoryCache()) {
                ir.add("adding-to-memory-cache");
                mMemoryCache.put(ir.getUrl(), ir.getBitmap());
            }

        }

    }

    private void processAndDisplay(final ImageRequest ir) {

        if (imageViewReused(ir)) {
            ir.finish("imageview-reused");
            return;
        }

        if (ir.getBitmap() != null && ir.getBitmapProcessor() != null) {


            Runnable processPoster = new Runnable() {

                public void run() {

                    try {
                        ir.add("processing-bitmap");
                        Bitmap tmp = ir.getBitmapProcessor().process(ir.getBitmap());
                        ir.setBitmap(tmp);
                        display(ir);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            if (Looper.myLooper() == Looper.getMainLooper()) {
                mExecutor.execute(processPoster);
            } else {
                processPoster.run();
            }

        } else {
            display(ir);
        }

    }

    private void display(final ImageRequest ir) {

        Runnable work = new Runnable() {

            public void run() {

//				ir.isAlive("run-display");
                if (imageViewReused(ir)) {
                    ir.finish("imageview-reused");
                    return;
                }

                ir.finish("display-on-UI-thread");
                ir.getBitmapDisplayer().display(ir);
            }
        };

        if (Looper.myLooper() == Looper.getMainLooper()) {
//			ir.isAlive("just run");
            work.run();
        } else {
//			ir.isAlive("post run");
            ir.add("posting-to-UI-thread");
            mHandler.post(work);
        }

    }

    /**
     * Method to check in the ImageView that the ImageRequest references, have been
     * reused. This can happen in e.g. ListViews, where the adapter reuses views,
     * while scrolling.
     *
     * @param ir Request to check
     * @return true if the View have been reused, false otherwise
     */
    private boolean imageViewReused(ImageRequest ir) {
        String url = mImageViews.get(ir.getImageView());
        return ((url == null || !url.contains(ir.getUrl())));
    }

    public void clear() {
        mImageViews.clear();
        mMemoryCache.clear();
        mFileCache.clear();
    }

    public MemoryCache getMemoryCache() {
        return mMemoryCache;
    }

    public FileCache getFileCache() {
        return mFileCache;
    }

    class PhotosLoader implements Runnable {

        ImageRequest ir;

        PhotosLoader(ImageRequest request) {
            ir = request;
        }

        public void run() {
            ir.add("running-on-executor");
            if (imageViewReused(ir)) {
                ir.finish("imageview-reused");
                return;
            }

            int retries = 0;
            while (ir.getBitmap() == null && retries < 2) {

                ir.add("retries-" + retries);
                byte[] image = null;

                try {

                    retries++;
                    ir.add("trying-file-cache");
                    image = mFileCache.getByteArray(ir);

                    if (image != null) {

                        ir.setLoadSource(LoadSource.FILE);

                    } else {

                        ir.add("trying-download");
                        image = mDownloader.getByteArray(ir);
                        if (image != null) {
                            ir.setLoadSource(LoadSource.WEB);
                        }

                    }

                    if (image != null) {
                        Bitmap b = ir.getBitmapDecoder().decode(ir, image);
                        ir.setBitmap(b);
                        ir.add("loaded-from-" + ir.getLoadSource());
                    } else {
                        ir.add("no-image-loaded");
                    }

                } catch (OutOfMemoryError t) {
                    ir.add("out-of-memory");
                    mMemoryCache.clear();
                } catch (IOException e) {
                    ir.add("download-failed");
                    SgnLog.e(TAG, "Download error", e);
                }
                addToCache(ir, image);

            }

            processAndDisplay(ir);

        }
    }

}