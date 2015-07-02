/*******************************************************************************
 * Copyright 2015 eTilbudsavis
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

package com.eTilbudsavis.etasdk.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.log.EtaLog;

import java.util.concurrent.atomic.AtomicInteger;

public class SQLDataSource {

    public static final String TAG = Constants.getTag(SQLDataSource.class);

    private final Object LOCK = new Object();
    private SQLiteOpenHelper mHelper;
    private SQLiteDatabase mDatabase;
    private AtomicInteger mRefCount = new AtomicInteger();
    private boolean mOpen = false;

    public SQLDataSource(SQLiteOpenHelper sqLiteHelper) {
        mHelper = sqLiteHelper;
    }

    public void open() {
        synchronized (LOCK) {
            if (!mOpen) {
                acquireDb();
            }
            mOpen = true;
        }
    }

    public void close() {
        synchronized (LOCK) {
            if (mOpen) {
                releaseDb();
            }
            mOpen = false;
        }
    }

    public boolean isOpen() {
        synchronized (LOCK) {
            return mRefCount.get() > 0;
        }
    }

    protected synchronized SQLiteDatabase acquireDb() {
        synchronized (LOCK) {
            if (mDatabase == null || !mDatabase.isOpen()) {
                mDatabase = mHelper.getWritableDatabase();
                mRefCount.set(0);
//                logRef("getWritableDatabase");
            }
            mDatabase.acquireReference();
            mRefCount.incrementAndGet();
//            logRef("acquireDb");
            return mDatabase;
        }
    }

    protected synchronized void releaseDb() {
        synchronized (LOCK) {
            mDatabase.releaseReference();
            if (mRefCount.decrementAndGet() == 0) {
                mHelper.close();
//                logRef("close");
            }
//            logRef("releaseDb");
        }
    }

    protected void logRef(String action) {
        EtaLog.d(TAG, String.format("Thread: %s, Action: %s, RefCount: %s", Thread.currentThread().getName(), action, mRefCount.get()));
    }

    public void log(String tag, Exception e) {
        EtaLog.e(tag, e.getMessage(), e);
    }

}
