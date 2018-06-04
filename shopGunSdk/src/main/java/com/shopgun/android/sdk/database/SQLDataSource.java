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

package com.shopgun.android.sdk.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.shopgun.android.sdk.log.SgnLog;
import com.shopgun.android.sdk.utils.Constants;

import java.util.concurrent.atomic.AtomicInteger;

/***
 * Class for handling the reference count to a {@link SQLiteDatabase}, and close the DB only when all queries are done.
 */
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

    /**
     * Open the {@link SQLiteDatabase}
     */
    public void open() {
        synchronized (LOCK) {
            if (!mOpen) {
                // perform a acquireDb to bump refcount
                acquireDb();
            }
            mOpen = true;
        }
    }

    /**
     * Close the {@link SQLiteDatabase}
     */
    public void close() {
        synchronized (LOCK) {
            if (mOpen) {
                // perform a acquireDb to un-bump refcount
                releaseDb();
            }
            mOpen = false;
        }
    }

    /**
     * Ask if the {@link SQLiteDatabase} is still open for business
     * @return {@code true} if DB is open, else {@code false}
     */
    public boolean isOpen() {
        synchronized (LOCK) {
            return mRefCount.get() > 0;
        }
    }

    /**
     * Get the instance of the {@link SQLiteDatabase}. The DB will be created if it haven't already
     * been instantiated. And the reference count will be incremented, ensuring that the SDK will not
     * be able to close any connections until your reference is released again.
     *
     * <p>
     * <b>IMPORTANT: </b> You must call {@link SQLDataSource#releaseDb()} once your DB transaction finishes.
     * So for every call to {@link SQLDataSource#acquireDb()} you must at some point call
     * {@link SQLDataSource#releaseDb()} (a one to one mapping)
     * , or we are going to start leaking memory.
     * </p>
     * @return A {@link SQLiteDatabase}
     */
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

    /**
     * Release the instance of the {@link SQLiteDatabase} that you have {@link SQLDataSource#acquireDb() acquired}.
     *
     * <b>IMPORTANT: </b> You must call {@link SQLDataSource#releaseDb()} once your DB transaction finishes.
     * So for every call to {@link SQLDataSource#acquireDb()} you must at some point call
     * {@link SQLDataSource#releaseDb()} (a one to one mapping)
     * , or we are going to start leaking memory.
     */
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
        SgnLog.d(TAG, String.format("Thread: %s, Action: %s, RefCount: %s", Thread.currentThread().getName(), action, mRefCount.get()));
    }

    public void log(String tag, Exception e) {
        SgnLog.e(tag, e.getMessage(), e);
    }

}
