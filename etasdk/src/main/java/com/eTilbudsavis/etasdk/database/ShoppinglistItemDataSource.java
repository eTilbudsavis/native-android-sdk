package com.eTilbudsavis.etasdk.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.eTilbudsavis.etasdk.Constants;

public class ShoppinglistItemDataSource {

    public static final String TAG = Constants.getTag(ShoppinglistItemSQLiteHelper.class);

    private static final String[] COUNT_PROJECTION = new String[]{"count(*)"};

    private static ShoppinglistDataSource mSource;
    private ShoppinglistItemSQLiteHelper mDbHelper;

    public ShoppinglistItemDataSource(Context c) {
        mDbHelper = new ShoppinglistItemSQLiteHelper(c);
    }

    private SQLiteDatabase db() {
        return mDbHelper.getWritableDatabase();
    }

    public int clear() {
        return db().delete(ShoppinglistSQLiteHelper.TABLE, null, new String[]{ "1" });
    }

    public int clear(int userId) {
        String whereClause = DatabaseHelper.USER + " = ?";
        String[] whereArgs = new String[]{ String.valueOf(userId) };
        int updates = 0;
        try {
            updates = db().delete(ShoppinglistItemSQLiteHelper.TABLE, whereClause, whereArgs);
        } catch (IllegalStateException e) {
            L.e(TAG, e.getMessage(), e);
        } finally {
            close();
        }
        return updates;
    }

}
