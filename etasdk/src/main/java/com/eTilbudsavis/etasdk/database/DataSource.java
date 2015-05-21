package com.eTilbudsavis.etasdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.Share;
import com.eTilbudsavis.etasdk.model.Shoppinglist;
import com.eTilbudsavis.etasdk.model.ShoppinglistItem;
import com.eTilbudsavis.etasdk.model.User;
import com.eTilbudsavis.etasdk.model.interfaces.SyncState;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DataSource {

    public static final String TAG = Constants.getTag(DataSource.class);

    private final Object LOCK = new Object();
    private DatabaseHelper mHelper;
    private SQLiteDatabase mDb;
    private AtomicInteger mRefCount = new AtomicInteger();

    public DataSource(DatabaseHelper sqLiteHelper) {
        this.mHelper = sqLiteHelper;
    }

    public DataSource(Context c) {
        this(new DatabaseHelper(c));
    }

    public void open() {
        acquireDb();
    }

    public void close() {
        releaseDb();
    }

    protected SQLiteDatabase acquireDb() {
        synchronized (LOCK) {
            if (mDb == null || !mDb.isOpen()) {
                mDb = mHelper.getWritableDatabase();
                mRefCount.set(0);
                logRef("getWritableDatabase");
            }
            mDb.acquireReference();
            mRefCount.incrementAndGet();
            logRef("acquireDb");
            return mDb;
        }
    }

    protected void releaseDb() {
        synchronized (LOCK) {
            mDb.releaseReference();
            if (mRefCount.decrementAndGet() == 0) {
                mHelper.close();
                logRef("close");
            }
            logRef("releaseDb");
        }
    }

    public void logRef(String action) {
        EtaLog.d(TAG, String.format("Thread: %s, Action: %s, RefCount: %s", Thread.currentThread().getName(), action, mRefCount.get()));
    }

    public void log(String tag, Exception e) {
        EtaLog.e(tag, e.getMessage(), e);
    }

    public int clear() {
        String[] whereArgs = new String[]{"1"};
        int count = delete(ItemSQLiteHelper.TABLE, null, whereArgs);
        count += delete(ListSQLiteHelper.TABLE, null, whereArgs);
        count += delete(ShareSQLiteHelper.TABLE, null, whereArgs);
        return count;
    }

    public int clear(int userId) {
        String whereClause = DatabaseHelper.USER + "=?";
        String[] whereArgs = new String[]{ String.valueOf(userId) };
        int count = delete(ItemSQLiteHelper.TABLE, whereClause, whereArgs);
        count += delete(ListSQLiteHelper.TABLE, whereClause, whereArgs);
        count += delete(ShareSQLiteHelper.TABLE, whereClause, whereArgs);
        return count;
    }

    protected int delete(String table, String whereClause, String[] whereArgs) {
        try {
            return acquireDb().delete(table, whereClause, whereArgs);
        } catch (IllegalStateException e) {
            log(TAG, e);
            return 0;
        } finally {
            releaseDb();
        }
    }

    /***********************************************************************************************
     *
     *                                            LISTS
     *
     **********************************************************************************************/

    /**
     * Insert new shopping list into DB
     *
     * @param sl A shoppinglist
     * @param userId A user
     * @return the row ID of the newly inserted row OR -1 if any error
     */
    public long insertList(Shoppinglist sl, String userId) {
        try {
            ContentValues cv = ListSQLiteHelper.objectToContentValues(sl, userId);
            return acquireDb().insertWithOnConflict(ListSQLiteHelper.TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (IllegalStateException e) {
            log(TAG, e);
            return -1;
        } finally {
            releaseDb();
        }
    }

    /**
     * Get a Shoppinglist matching the given parameters
     * @param id A Shoppinglist id
     * @param userId A user id
     * @return A Shoppinglist if one exists in DB, else null;
     */
    public Shoppinglist getList(String id, String userId) {
        String selection = DatabaseHelper.ID + "=? AND " + DatabaseHelper.USER + "=? AND " + DatabaseHelper.STATE + "!=?";
        String[] selectionArgs = new String[]{id, userId, String.valueOf(SyncState.DELETE)};
        List<Shoppinglist> list = getLists(selection, selectionArgs);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * @param userId A user-id that the lists must belong to
     * @param includeDeleted Whether to include deleted items or not
     * @return A list of Shoppinglist
     */
    public List<Shoppinglist> getLists(String userId, boolean includeDeleted) {
        String selection = DatabaseHelper.USER + "=? AND " + DatabaseHelper.STATE + "!=?";
        String[] selectionArgs = new String[]{userId, String.valueOf(SyncState.DELETE)};
        if (includeDeleted) {
            selection = DatabaseHelper.USER + "=?";
            selectionArgs = new String[]{userId};
        }
        return getLists(selection, selectionArgs);
    }

    private List<Shoppinglist> getLists(String selection, String[] selectionArgs) {
        Cursor c = null;
        try {
            c = acquireDb().query(false, ListSQLiteHelper.TABLE, null, selection, selectionArgs, null, null, DatabaseHelper.NAME, null);
            return ListSQLiteHelper.cursorToList(c);
        } catch (IllegalStateException e) {
            log(TAG, e);
            return new ArrayList<Shoppinglist>();
        } finally {
            DbUtils.closeCursor(c);
            releaseDb();
        }
    }

    /**
     * Delete a (all) shoppinglist where both the Shoppinglist.id, and user.id matches
     * @param shoppinglistId A shoppinglist id
     * @param userId A user id
     * @return number of affected rows
     */
    public int deleteList(String shoppinglistId, String userId) {
        String whereClause = DatabaseHelper.ID + "=? AND " + DatabaseHelper.USER + "=? ";
        String[] whereArgs = new String[]{ shoppinglistId, userId};
        return delete(ListSQLiteHelper.TABLE, whereClause, whereArgs);
    }

    public Shoppinglist getListPrevious( String previousId, String userId) {
        String selection = DatabaseHelper.PREVIOUS_ID + "=? AND " + DatabaseHelper.USER + "=?";
        String[] selectionArgs = new String[]{previousId, userId};
        List<Shoppinglist> list = getLists(selection, selectionArgs);
        return list.isEmpty() ? null : list.get(0);
    }

    /***********************************************************************************************
     *
     *                                            ITEMS
     *
     **********************************************************************************************/

    /**
     * Adds item to db, IF it does not yet exist, else nothing
     * @param sli to add to db
     * @return number of affected rows
     */
    public long insertItem(ShoppinglistItem sli, String userId) {
        try {
            ContentValues cv = ItemSQLiteHelper.objectToContentValues(sli, userId);
            return acquireDb().insertWithOnConflict(ItemSQLiteHelper.TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (IllegalStateException e) {
            log(TAG, e);
            return -1;
        } finally {
            releaseDb();
        }
    }

    public int insertItem(List<ShoppinglistItem> list, String userId) {
        if (list.isEmpty()) {
            return -1;
        }
        SQLiteDatabase db = acquireDb();
        db.beginTransaction();
        int count = 0;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO OR REPLACE INTO ").append(ItemSQLiteHelper.TABLE).append(" VALUES (");
            ContentValues cv = ItemSQLiteHelper.objectToContentValues(list.get(0), userId);
            for (int i = 0; i < cv.size(); i++) {
                sb.append((i > 0) ? ",?" : "?");
            }
            sb.append(")");
            SQLiteStatement s = db.compileStatement(sb.toString());
            for (ShoppinglistItem sli : list) {
                ItemSQLiteHelper.bind(s, sli, userId);
                if (s.executeInsert() > -1) {
                    count++;
                }
            }
            db.setTransactionSuccessful();
            return count;
        } catch (IllegalStateException e) {
            log(TAG, e);
            return -1;
        } finally {
            db.endTransaction();
            releaseDb();
        }
    }

    public ShoppinglistItem getItem(String itemId, String userId) {
        String selection = DatabaseHelper.ID + "=? AND " + DatabaseHelper.USER + "=? AND " + DatabaseHelper.STATE + "!=?";
        String[] selectionArgs = new String[]{itemId, userId, String.valueOf(SyncState.DELETE)};
        List<ShoppinglistItem> list = getItems(selection, selectionArgs);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<ShoppinglistItem> getItems(String shoppinglistId, String userId, boolean includeDeleted) {
        String selection = DatabaseHelper.SHOPPINGLIST_ID + "=? AND " + DatabaseHelper.USER + "=? AND " + DatabaseHelper.STATE + "!=?";
        String[] selectionArgs = new String[]{shoppinglistId, userId, String.valueOf(SyncState.DELETE)};
        if (includeDeleted) {
            selection = DatabaseHelper.SHOPPINGLIST_ID + "=? AND " + DatabaseHelper.USER + "=?";
            selectionArgs = new String[]{shoppinglistId, userId};
        }
        return getItems(selection, selectionArgs);
    }

    public ShoppinglistItem getItemPrevious(String shoppinglistId, String previousId, String userId) {
        String selection = DatabaseHelper.SHOPPINGLIST_ID + "=? AND " + DatabaseHelper.PREVIOUS_ID + "=? AND " + DatabaseHelper.USER + "=?";
        String[] selectionArgs = new String[]{shoppinglistId, previousId, userId};
        List<ShoppinglistItem> list = getItems(selection, selectionArgs);
        return list.isEmpty() ? null : list.get(0);
    }

    private List<ShoppinglistItem> getItems(String selection, String[] selectionArgs) {
        Cursor c = null;
        try {
            c = acquireDb().query(false, ItemSQLiteHelper.TABLE, null, selection, selectionArgs, null, null, null, null);
            return ItemSQLiteHelper.cursorToList(c);
        } catch (IllegalStateException e) {
            log(TAG, e);
            return new ArrayList<ShoppinglistItem>();
        } finally {
            DbUtils.closeCursor(c);
            releaseDb();
        }
    }

    public int deleteItem(String itemId, String userId) {
        String whereClause = DatabaseHelper.ID + "=? AND " + DatabaseHelper.USER + "=? ";
        String[] whereArgs = new String[]{ itemId, userId };
        return delete(ItemSQLiteHelper.TABLE, whereClause, whereArgs);
    }

    /**
     * Deletes all items, in a given state, from a {@link Shoppinglist}
     *
     * <ul>
     * 		<li>{@code true} - delete ticked items</li>
     * 		<li>{@code false} - delete unticked items</li>
     * 		<li>{@code null} - delete all items</li>
     * </ul>
     *
     * @param shoppinglistId to remove items from
     * @param state that items must have to be removed
     * @return number of affected rows
     */
    public int deleteItems(String shoppinglistId, Boolean state, String userId) {
        String whereClause = DatabaseHelper.SHOPPINGLIST_ID + "=? AND " + DatabaseHelper.USER + "=? ";
        String[] whereArgs = new String[]{ shoppinglistId, userId };
        if (state != null) {
            whereClause = DatabaseHelper.SHOPPINGLIST_ID + "=? AND " + DatabaseHelper.USER + "=?  AND " + DatabaseHelper.TICK + "=?";
            whereArgs = new String[]{ shoppinglistId, userId, String.valueOf(state) };
        }
        return delete(ItemSQLiteHelper.TABLE, whereClause, whereArgs);
    }

    /***********************************************************************************************
     *
     *                                            SHARES
     *
     **********************************************************************************************/

    public long insertShare(Share s, String userId) {
        try {
            ContentValues cv = ShareSQLiteHelper.objectToContentValues(s, userId);
            return acquireDb().insertWithOnConflict(ShareSQLiteHelper.TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (IllegalStateException e) {
            log(TAG, e);
            return -1;
        } finally {
            releaseDb();
        }
    }

    public List<Share> getShares(String shoppinglistId, String userId, boolean includeDeleted) {
        String selection = DatabaseHelper.SHOPPINGLIST_ID + "=? AND " + DatabaseHelper.USER + "=? AND " + DatabaseHelper.STATE + "!=?";
        String[] selectionArgs = new String[]{shoppinglistId, userId, String.valueOf(SyncState.DELETE)};
        if (includeDeleted) {
            selection = DatabaseHelper.SHOPPINGLIST_ID + "=? AND " + DatabaseHelper.USER + "=?";
            selectionArgs = new String[]{shoppinglistId, userId};
        }
        return getShares(selection, selectionArgs, shoppinglistId);
    }

    private List<Share> getShares(String selection, String[] selectionArgs, String shoppinglistId) {
        Cursor c = null;
        try {
            c = acquireDb().query(false, ShareSQLiteHelper.TABLE, null, selection, selectionArgs, null, null, null, null);
            return ShareSQLiteHelper.cursorToList(c, shoppinglistId);
        } catch (IllegalStateException e) {
            log(TAG, e);
            return new ArrayList<Share>();
        } finally {
            DbUtils.closeCursor(c);
            releaseDb();
        }
    }

    public int deleteShare(Share s, User user) {
        return deleteShare(s.getEmail(), s.getShoppinglistId(), user.getId());
    }

    public int deleteShare(String shareEmail, String shareShoppinglistId, String userId) {
        String whereClause = DatabaseHelper.EMAIL + "=? AND " + DatabaseHelper.SHOPPINGLIST_ID + "=? AND " + DatabaseHelper.USER + "=?";
        String[] whereArgs = new String[]{ shareEmail, shareShoppinglistId, userId };
        return delete(ShareSQLiteHelper.TABLE, whereClause, whereArgs);
    }

    public int deleteShares(String shoppinglistId, String userId) {
        String whereClause = DatabaseHelper.SHOPPINGLIST_ID + "=? AND " + DatabaseHelper.USER + "=? ";
        String[] whereArgs = new String[]{ shoppinglistId, userId };
        return delete(ShareSQLiteHelper.TABLE, whereClause, whereArgs);
    }


}
