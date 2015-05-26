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

    public SQLDataSource(SQLiteOpenHelper sqLiteHelper) {
        mHelper = sqLiteHelper;
    }

    public void open() {
        acquireDb();
    }

    public void close() {
        releaseDb();
    }

    protected SQLiteDatabase acquireDb() {
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

    protected void releaseDb() {
        synchronized (LOCK) {
            mDatabase.releaseReference();
            if (mRefCount.decrementAndGet() == 0) {
                mHelper.close();
//                logRef("close");
            }
//            logRef("releaseDb");
        }
    }

//    public void logRef(String action) {
//        EtaLog.d(TAG, String.format("Thread: %s, Action: %s, RefCount: %s", Thread.currentThread().getName(), action, mRefCount.get()));
//    }

    public void log(String tag, Exception e) {
        EtaLog.e(tag, e.getMessage(), e);
    }

}
