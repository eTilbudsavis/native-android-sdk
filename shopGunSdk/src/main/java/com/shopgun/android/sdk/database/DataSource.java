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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.shopgun.android.sdk.Constants;
import com.shopgun.android.sdk.model.Share;
import com.shopgun.android.sdk.model.Shoppinglist;
import com.shopgun.android.sdk.model.ShoppinglistItem;
import com.shopgun.android.sdk.model.User;
import com.shopgun.android.sdk.model.interfaces.SyncState;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class DataSource extends SQLDataSource {

    public static final String TAG = Constants.getTag(DataSource.class);

    public DataSource(Context c) {
        super(new DatabaseHelper(c));
    }

    /**
     * Clear all data in the database
     * @return number of changes
     */
    public int clear() {
        String whereClause = "1";
        int count = delete(ItemSQLiteHelper.TABLE, whereClause, null);
        count += delete(ListSQLiteHelper.TABLE, whereClause, null);
        count += delete(ShareSQLiteHelper.TABLE, whereClause, null);
        return count;
    }

    /**
     * Clear all data from a given {@link User}
     * @param userId A {@link User#getId()}
     * @return number of changes
     */
    public int clear(int userId) {
        String whereClause = DatabaseHelper.USER + "=?";
        String[] whereArgs = new String[]{String.valueOf(userId)};
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
     * Insert a {@link Shoppinglist}, into the database
     * @param sl A {@link Shoppinglist}
     * @param userId A {@link User#getId()}
     * @return number of affected rows
     */
    public int insertList(Shoppinglist sl, String userId) {
        List<Shoppinglist> list = new ArrayList<Shoppinglist>(1);
        list.add(sl);
        return insertList(list, userId);
    }

    /**
     * Insert a list of {@link Shoppinglist}, into the database
     * @param list A list of {@link Shoppinglist}
     * @param userId A {@link User#getId()}
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
     * Get a Shoppinglist from database, matching the given criteria.
     *
     * @param id A {@link Shoppinglist#getId()}
     * @param userId A {@link User#getId()}
     * @return A Shoppinglist if one exists in DB, else null;
     */
    public Shoppinglist getList(String id, String userId) {
        String selection = DatabaseHelper.ID + "=? AND " + DatabaseHelper.USER + "=? AND " + DatabaseHelper.STATE + "!=?";
        String[] selectionArgs = new String[]{id, userId, String.valueOf(SyncState.DELETE)};
        List<Shoppinglist> list = getLists(selection, selectionArgs, userId);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * @param userId         A user-id that the lists must belong to
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
     *
     * @param shoppinglistId A shoppinglist id
     * @param userId         A user id
     * @return number of affected rows
     */
    public int deleteList(String shoppinglistId, String userId) {
        String whereClause = DatabaseHelper.ID + "=? AND " + DatabaseHelper.USER + "=? ";
        String[] whereArgs = new String[]{shoppinglistId, userId};
        return delete(ListSQLiteHelper.TABLE, whereClause, whereArgs);
    }

    /**
     * Get the {@link Shoppinglist} with the given {@code previousId}
     * @param previousId An {@link Shoppinglist#getId()} or {@link com.shopgun.android.sdk.utils.ListUtils#FIRST_ITEM}
     * @param userId A {@link User#getId()}
     * @return A {@link Shoppinglist} if one exists with the {@code previousId}, else {@code null}
     */
    public Shoppinglist getListPrevious(String previousId, String userId) {
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
     *
     * @param shoppinglistId A {@link Shoppinglist#getId()}
     * @param userId A {@link User#getId()}
     * @param modified       The new Modified for the items
     * @param syncState      The new SyncState for the items
     * @return the number of rows affected
     */
    public int editItemState(String shoppinglistId, String userId, Date modified, int syncState) {
        try {
            ContentValues cv = ItemSQLiteHelper.stateToContentValues(modified, syncState);
            String whereClause = DatabaseHelper.SHOPPINGLIST_ID + "=? AND " + DatabaseHelper.USER + "=? ";
            String[] whereArgs = new String[]{shoppinglistId, userId};
            return acquireDb().updateWithOnConflict(ItemSQLiteHelper.TABLE, cv, whereClause, whereArgs, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (IllegalStateException e) {
            log(TAG, e);
            return 0;
        } finally {
            releaseDb();
        }
    }

    /**
     * Adds item to db, <b>if and only if</b> it does not yet exist, else nothing
     *
     * @param sli A {@link ShoppinglistItem} to add to the database
     * @param userId A {@link User#getId()}
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

    /**
     * Insert a list of {@link ShoppinglistItem} into the database
     * @param list A list of {@link ShoppinglistItem}
     * @param userId A {@link User#getId()}
     * @return The number of inserted items
     */
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

    /**
     * Get all {@link ShoppinglistItem} from a {@link Shoppinglist}.
     * @param shoppinglistId a {@link Shoppinglist#getId()}
     * @param userId A {@link User#getId()}
     * @param includeDeleted {@code true} to include the items that have locally been marked as deleted, else {@code false}
     * @return A list of {@link ShoppinglistItem}
     */
    public List<ShoppinglistItem> getItems(String shoppinglistId, String userId, boolean includeDeleted) {
        String selection = DatabaseHelper.SHOPPINGLIST_ID + "=? AND " + DatabaseHelper.USER + "=? AND " + DatabaseHelper.STATE + "!=?";
        String[] selectionArgs = new String[]{shoppinglistId, userId, String.valueOf(SyncState.DELETE)};
        if (includeDeleted) {
            selection = DatabaseHelper.SHOPPINGLIST_ID + "=? AND " + DatabaseHelper.USER + "=?";
            selectionArgs = new String[]{shoppinglistId, userId};
        }
        return getItems(selection, selectionArgs);
    }

    /**
     * Get the {@link ShoppinglistItem} that has the given set of criteria
     * @param shoppinglistId A {@link Shoppinglist#getId()}
     * @param previousId A {@link ShoppinglistItem#getPreviousId()}
     * @param userId A {@link User#getId()}
     * @return A {@link ShoppinglistItem} if one exists with the {@code previousId}, else {@code null}
     */
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
        String[] whereArgs = new String[]{itemId, userId};
        return delete(ItemSQLiteHelper.TABLE, whereClause, whereArgs);
    }

    /**
     * Deletes all items, in a given state, from a {@link Shoppinglist}
     *
     * <ul>
     *      <li>{@code true} - delete ticked items</li>
     *      <li>{@code false} - delete unticked items</li>
     *      <li>{@code null} - delete all items</li>
     * </ul>
     *
     * @param shoppinglistId A {@link Shoppinglist#getId()} to remove items from
     * @param state that items must have to be removed
     * @param userId A {@link User#getId()}
     * @return number of affected rows
     */
    public int deleteItems(String shoppinglistId, Boolean state, String userId) {
        String whereClause = DatabaseHelper.SHOPPINGLIST_ID + "=? AND " + DatabaseHelper.USER + "=? ";
        String[] whereArgs = new String[]{shoppinglistId, userId};
        if (state != null) {
            whereClause = DatabaseHelper.SHOPPINGLIST_ID + "=? AND " + DatabaseHelper.USER + "=?  AND " + DatabaseHelper.TICK + "=?";
            whereArgs = new String[]{shoppinglistId, userId, DbUtils.unescape(state)};
        }
        return delete(ItemSQLiteHelper.TABLE, whereClause, whereArgs);
    }

    /**
     * ********************************************************************************************
     *
     * SHARES
     *
     * ********************************************************************************************
     */

    /**
     * Insert a share into the database,
     * @param s A share
     * @param userId A {@link User#getId()}
     * @return the row ID of the newly inserted row OR -1 if any error
     */
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

    /**
     * Insert new shopping list into DB
     *
     * @param sl     A shoppinglist
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
            for (Share share : shares) {
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
        String[] whereArgs = new String[]{shareEmail, shareShoppinglistId, userId};
        return delete(ShareSQLiteHelper.TABLE, whereClause, whereArgs);
    }

    public int deleteShares(String shoppinglistId, String userId) {
        String whereClause = DatabaseHelper.SHOPPINGLIST_ID + "=? AND " + DatabaseHelper.USER + "=? ";
        String[] whereArgs = new String[]{shoppinglistId, userId};
        return delete(ShareSQLiteHelper.TABLE, whereClause, whereArgs);
    }

    public int clean() {
        int c = cleanItemsForOfflineUser();
        c += cleanListssForOfflineUser();
        return c;
    }

    private int cleanItemsForOfflineUser() {
        /*
        The ListManager didn't actually delete items from the tables. Possibly causing a lot of
        shoppinglistitems to remain in the item table.
        Cleanup by performing a delete WHERE user = -1 and state = DELETE
         */
        String whereClause = DatabaseHelper.USER + "=? AND " + DatabaseHelper.STATE + "=? ";
        String[] whereArgs = new String[]{"-1", String.valueOf(SyncState.DELETE)};
        return delete(ItemSQLiteHelper.TABLE, whereClause, whereArgs);
    }

    private int cleanListssForOfflineUser() {
        /*
        The ListManager didn't actually delete lists from the tables. Possibly causing a lot of
        Shoppinglists to remain in the item table.
        Cleanup by performing a delete WHERE user = -1 and state = DELETE
         */
        String whereClause = DatabaseHelper.USER + "=? AND " + DatabaseHelper.STATE + "=? ";
        String[] whereArgs = new String[]{"-1", String.valueOf(SyncState.DELETE)};
        return delete(ListSQLiteHelper.TABLE, whereClause, whereArgs);
    }

    public JSONArray dumpListTable() {
        return dumpTable(ListSQLiteHelper.TABLE);
    }

    public JSONArray dumpItemTable() {
        return dumpTable(ItemSQLiteHelper.TABLE);
    }

    public JSONArray dumpShareTable() {
        return dumpTable(ShareSQLiteHelper.TABLE);
    }

    private JSONArray dumpTable(String table) {
        try {
            return DbUtils.dumpTableToJSONArray(acquireDb(), table);
        } catch (IllegalStateException e) {
            log(TAG, e);
            return new JSONArray();
        } finally {
            releaseDb();
        }
    }

}
