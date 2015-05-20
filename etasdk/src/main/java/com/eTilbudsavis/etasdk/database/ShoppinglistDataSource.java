package com.eTilbudsavis.etasdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.model.Shoppinglist;
import com.eTilbudsavis.etasdk.model.User;
import com.eTilbudsavis.etasdk.model.interfaces.SyncState;
import com.eTilbudsavis.etasdk.utils.Utils;

public class ShoppinglistDataSource {

    public static final String TAG = Constants.getTag(ShoppinglistDataSource.class);

    private static final String[] COUNT_PROJECTION = new String[]{"count(*)"};

    private static ShoppinglistDataSource mSource;
    private ShoppinglistSQLiteHelper mDbHelper;

    public ShoppinglistDataSource(Context c) {
        mDbHelper = new ShoppinglistSQLiteHelper(c);
    }

    private SQLiteDatabase db() {
        return mDbHelper.getWritableDatabase();
    }

    public int clear() {
        return db().delete(ShoppinglistSQLiteHelper.TABLE, null, new String[]{"1"});
    }

    public int clear(int userId) {
        String whereClause = DatabaseHelper.USER + " = ?";
        String[] whereArgs = new String[]{ String.valueOf(userId) };
        int updates = 0;
        try {
            updates = db().delete(ShoppinglistSQLiteHelper.TABLE, whereClause, whereArgs);
        } catch (IllegalStateException e) {
            L.e(TAG, e.getMessage(), e);
        } finally {
            close();
        }
        return updates;
    }

    /**
     * Insert new shopping list into DB
     * @param sl A shoppinglist
     * @param user A user
     * @return the row ID of the newly inserted row OR -1 if any error
     */
    public long insertList(Shoppinglist sl, User user) {
        long id = -1;
        try {
            ContentValues cv = ShoppinglistSQLiteHelper.objectToContentValues(sl, user.getUserId());
            id = db().insertWithOnConflict(ShoppinglistSQLiteHelper.TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (IllegalStateException e) {
            L.e(TAG, e.getMessage(), e);
        } finally {
            close();
        }
        return id;
    }

    public Shoppinglist getList(String id, User user) {

        Cursor c = db().query(false, ShoppinglistSQLiteHelper.TABLE, null, selection, selectionArgs, null, null, orderBy, null);

        String q = String.format("SELECT * FROM %s WHERE %s=%s AND %s=%s AND %s!=%s", LIST_TABLE, ID, id, USER, user.getUserId(), STATE, SyncState.DELETE);
        Cursor c = null;
        Shoppinglist sl = null;

            c = execQuery(q);
            sl = c.moveToFirst() ? cursorToSl(c) : null;
    }

}
