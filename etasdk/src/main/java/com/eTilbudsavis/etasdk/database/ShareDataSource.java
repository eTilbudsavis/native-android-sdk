package com.eTilbudsavis.etasdk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.eTilbudsavis.etasdk.Constants;

public class ShareDataSource {

    public static final String TAG = Constants.getTag(ShareDataSource.class);

    private static final String[] COUNT_PROJECTION = new String[]{"count(*)"};

    private static ShareDataSource mSource;
    private ShareSQLiteHelper mDbHelper;

    public ShareDataSource(Context c) {
        mDbHelper = new ShareSQLiteHelper(c);
    }

    private SQLiteDatabase db() {
        return mDbHelper.getWritableDatabase();
    }

    public int clear() {
        return db().delete(ShareSQLiteHelper.TABLE, null, new String[]{ "1" });
    }

    public int clear(int userId) {
        String whereClause = DatabaseHelper.USER + " = ?";
        String[] whereArgs = new String[]{ String.valueOf(userId) };
        int updates = 0;
        try {
            updates = db().delete(ShareSQLiteHelper.TABLE, whereClause, whereArgs);
        } catch (IllegalStateException e) {
            L.e(TAG, e.getMessage(), e);
        } finally {
            close();
        }
        return updates;
    }

}