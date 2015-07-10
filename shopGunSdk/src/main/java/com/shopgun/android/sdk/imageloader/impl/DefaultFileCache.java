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

import android.content.Context;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.imageloader.FileCache;
import com.shopgun.android.sdk.imageloader.ImageRequest;
import com.shopgun.android.sdk.log.EtaLog;
import com.shopgun.android.sdk.utils.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;

public class DefaultFileCache implements FileCache {

    public static final String TAG = Constants.getTag(DefaultFileCache.class);
    private static final long WEEK_IN_MILLIS = 1000 * 60 * 60 * 24 * 7;
    private File mCacheDir;
    Runnable cleaner = new Runnable() {

        public void run() {
            File[] files = mCacheDir.listFiles();
            if (files == null) {
                return;
            }
            int count = 0;
            for (File f : files) {
                boolean recentlyModified = (System.currentTimeMillis() - f.lastModified()) < WEEK_IN_MILLIS;
                if (!recentlyModified) {
                    count++;
                    f.delete();
                }
            }
            if (count > 0) {
                EtaLog.v(TAG, "Deleted " + count + " files from " + getClass().getSimpleName());
            }
        }
    };
    private ExecutorService mExecutor;

    public DefaultFileCache(Context context, ExecutorService executor) {
        mCacheDir = FileUtils.getCacheDirectory(context, true);
        mExecutor = executor;
        EtaLog.v(TAG, "CacheDir: " + mCacheDir.getAbsolutePath());
        cleanup();
    }

    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }

    public void save(final ImageRequest ir, final byte[] b) {

        Runnable r = new Runnable() {

            public void run() {

                File f = new File(mCacheDir, ir.getFileName());
                FileOutputStream fos = null;
                if (f.exists()) {
                    f.delete();
                }
                try {
                    fos = new FileOutputStream(f);
                    fos.write(b);
                } catch (IOException e) {

                } finally {
                    try {
                        fos.close();
                    } catch (Throwable t) {

                    }
                }
//				EtaLog.d(TAG, "SaveByteArray: " + (System.currentTimeMillis()-start));
            }
        };

        mExecutor.execute(r);

    }

    public byte[] getByteArray(ImageRequest ir) {
        File f = new File(mCacheDir, ir.getFileName());
        byte[] b = null;
        if (f.exists()) {
            try {
                b = readFile(f);
            } catch (FileNotFoundException e) {

            } catch (IOException e2) {

            }
        }
        return b;
    }

    public void cleanup() {

        mExecutor.execute(cleaner);

    }

    public void clear() {
        File[] files = mCacheDir.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            f.delete();
        }
    }

}
