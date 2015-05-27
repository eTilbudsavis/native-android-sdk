package com.eTilbudsavis.etasdk.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteProgram;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.Share;
import com.eTilbudsavis.etasdk.model.Shoppinglist;
import com.eTilbudsavis.etasdk.model.ShoppinglistItem;
import com.eTilbudsavis.etasdk.model.User;
import com.eTilbudsavis.etasdk.model.interfaces.SyncState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DataSource extends SQLDataSource {

    public static final String TAG = Constants.getTag(DataSource.class);

    public DataSource(Context c) {
        super(new DatabaseHelper(c));
    }

    public int clear() {
        String whereClause = "1";
        int count = delete(ItemSQLiteHelper.TABLE, whereClause, null);
        count += delete(ListSQLiteHelper.TABLE, whereClause, null);
        count += delete(ShareSQLiteHelper.TABLE, whereClause, null);
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
     * @return number of affected rows
     */
    public int insertList(Shoppinglist sl, String userId) {
        List<Shoppinglist> list = new ArrayList<Shoppinglist>(1);
        list.add(sl);
        return insertList(list, userId);
    }

    /**
     * Insert new shopping list into DB
     *
     * @param list A list of Shoppinglist
     * @param userId A user
     * @return number of affected rows
     */
    public int insertList(List<Shoppinglist> list, String userId) {
        if (list.isEmpty()) {
            return 0;
        }
        SQLiteDatabase db = acquireDb();
        try {
            int count = 0;
            db.beginTransaction();
            SQLiteStatement s = ListSQLiteHelper.getInsertStatement(db);
            for (Shoppinglist sl : list) {
                ListSQLiteHelper.bind(s, sl, userId);
                if (s.executeInsert() > -1) {
                    deleteShares(sl.getId(), userId);
                    insertSharesTransactionLess(db, sl.getShares().values(), userId);
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

    /**
     * Get a Shoppinglist matching the given parameters
     * @param id A Shoppinglist id
     * @param userId A user id
     * @return A Shoppinglist if one exists in DB, else null;
     */
    public Shoppinglist getList(String id, String userId) {
        String selection = DatabaseHelper.ID + "=? AND " + DatabaseHelper.USER + "=? AND " + DatabaseHelper.STATE + "!=?";
        String[] selectionArgs = new String[]{id, userId, String.valueOf(SyncState.DELETE)};
        List<Shoppinglist> list = getLists(selection, selectionArgs, userId);
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
        return getLists(selection, selectionArgs, userId);
    }

    private List<Shoppinglist> getLists(String selection, String[] selectionArgs, String userId) {
        Cursor c = null;
        SQLiteDatabase db = acquireDb();
        try {
            db.beginTransaction();
            c = db.query(false, ListSQLiteHelper.TABLE, null, selection, selectionArgs, null, null, DatabaseHelper.NAME, null);
            List<Shoppinglist> lists = ListSQLiteHelper.cursorToList(c);
            for (Shoppinglist sl : lists) {
                List<Share> shares = getShares(sl.getId(), userId, false);
                sl.setShares(shares);
            }
            db.setTransactionSuccessful();
            return lists;
        } catch (IllegalStateException e) {
            log(TAG, e);
            return new ArrayList<Shoppinglist>();
        } finally {
            DbUtils.closeCursor(c);
            db.endTransaction();
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
        List<Shoppinglist> list = getLists(selection, selectionArgs, userId);
        return list.isEmpty() ? null : list.get(0);
    }

    /***********************************************************************************************
     *
     *                                            ITEMS
     *
     **********************************************************************************************/

    /**
     * Method for updating a state for all ShoppinglistItem with a given User and Shoppinglist.id
     * @param shoppinglistId A shoppinglist.id
     * @param userId A userid
     * @param modified The new Modified for the items
     * @param syncState The new SyncState for the items
     * @return the number of rows affected
     */
    public int editItemState(String shoppinglistId, String userId, Date modified, int syncState) {
        try {
            ContentValues cv = ItemSQLiteHelper.stateToContentValues(modified, syncState);
            String whereClause = DatabaseHelper.SHOPPINGLIST_ID + "=? AND " + DatabaseHelper.USER + "=? ";
            String[] whereArgs = new String[]{ shoppinglistId, userId };
            return acquireDb().updateWithOnConflict(ItemSQLiteHelper.TABLE, cv, whereClause, whereArgs, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (IllegalStateException e) {
            log(TAG, e);
            return 0;
        } finally {
            releaseDb();
        }
    }

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
            return 0;
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
            SQLiteStatement s = ItemSQLiteHelper.getInsertStatement(db);
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
            return count;
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
            Log.d(TAG, s.toJSON().toString());
            return -1;
        } finally {
            releaseDb();
        }
    }

    /**
     * Insert new shopping list into DB
     *
     * @param sl A shoppinglist
     * @param userId A user
     * @return the row ID of the newly inserted row OR -1 if any error
     */
    public int insertShares(Shoppinglist sl, String userId) {
        SQLiteDatabase db = acquireDb();
        db.beginTransaction();
        try {
            int count = insertSharesTransactionLess(db, sl.getShares().values(), userId);
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

    private int insertSharesTransactionLess(SQLiteDatabase db, Collection<Share> shares, String userId) {
        db.acquireReference();
        try {
            int count = 0;
            SQLiteStatement s = ShareSQLiteHelper.getInsertStatement(db);
            for(Share share : shares) {
                ShareSQLiteHelper.bind(s, share, userId);
                if (s.executeInsert() > -1) {
                    count++;
                }
            }
            return count;
        } catch (IllegalStateException e) {
            log(TAG, e);
            return 0;
        } finally {
            db.releaseReference();
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
