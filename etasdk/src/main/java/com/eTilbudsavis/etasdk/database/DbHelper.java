/*******************************************************************************
* Copyright 2014 eTilbudsavis
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
*******************************************************************************/
package com.eTilbudsavis.etasdk.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.eTilbudsavis.etasdk.Constants;
import com.eTilbudsavis.etasdk.Eta;
import com.eTilbudsavis.etasdk.log.EtaLog;
import com.eTilbudsavis.etasdk.model.Share;
import com.eTilbudsavis.etasdk.model.Shoppinglist;
import com.eTilbudsavis.etasdk.model.ShoppinglistItem;
import com.eTilbudsavis.etasdk.model.User;
import com.eTilbudsavis.etasdk.model.interfaces.SyncState;
import com.eTilbudsavis.etasdk.utils.ListUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DbHelper {
	
	public static final String TAG = Constants.getTag(DbHelper.class);

	private Object LOCK = new Object();

	private static DbHelper mDbHelper;
	private SQLiteDatabase mDatabase;
	private Eta mEta;

    private ShoppinglistDataSource mListSource;
    private ShoppinglistItemDataSource mItemSource;
    private ShareDataSource mShareSource;
	
	private DbHelper(Eta eta) {
		mEta = eta;
        Context c = mEta.getContext();
        mListSource = new ShoppinglistDataSource(c);
        mItemSource = new ShoppinglistItemDataSource(c);
        mShareSource = new ShareDataSource(c);
	}

	public static DbHelper getInstance(Eta eta) {
		if (mDbHelper == null) {
			mDbHelper = new DbHelper(eta);
		}
		return mDbHelper;
	}
	
	/**
	 * Clears the whole DB. This cannot be undone.
	 */
	public int clear() {
        int result = mListSource.clear();
        result += mItemSource.clear();
        result += mShareSource.clear();
        return result;
	}

	public int clear(int userId) {
        int result = mListSource.clear(userId);
        result += mItemSource.clear(userId);
        result += mShareSource.clear(userId);
        return result;
	}

	/**
	 * Insert new shopping list into DB
	 * @param sl to insert
	 * @return number of affected rows
	 */
	public long insertList(Shoppinglist sl, User user) {
        long id = mListSource.insertList(sl, user);
		if (id > -1) {
			cleanShares(sl, user);
		}
		return id;
	}

	/**
	 * Get a shoppinglist by it's id
	 * @param id to get from db
	 * @return A shoppinglist or null if no march is found
	 */
	public Shoppinglist getList(String id, User user) {
        Shoppinglist sl = mListSource.getList(id, user);
		if (sl != null) {
			sl.putShares(getShares(sl, user, false));
			sl = sl.getShares().containsKey(user.getEmail()) ? sl : null;
		}
		return sl;
	}
	
	/**
	 * Get all shoppinglists, deleted lists are not included
	 * @return A list of shoppinglists
	 */
	public List<Shoppinglist> getLists(User user) {
		return getLists(user, false);
	}
	
	/**
	 * 
	 * @param userId
	 * @param includeDeleted
	 * @return A list of shoppinglists
	 */
	public List<Shoppinglist> getLists(User user, boolean includeDeleted) {
		String q = null;
		if (includeDeleted) {
			q = String.format("SELECT * FROM %s WHERE %s=%s", LIST_TABLE, USER, user.getUserId());
		} else {
			q = String.format("SELECT * FROM %s WHERE %s!=%s AND %s=%s", LIST_TABLE, STATE, SyncState.DELETE, USER, user.getUserId());
		}
		Cursor c = null;
		List<Shoppinglist> tmp = new ArrayList<Shoppinglist>();
		try {
			c = execQuery(q);
			if (c.moveToFirst()) {
				do { 
					tmp.add(cursorToSl(c));
				} while (c.moveToNext());
			}
		} catch (IllegalStateException e) {
			EtaLog.d(TAG, e.getMessage(), e);
		} finally {
			closeCursorAndDB(c);
		}
		
		List<Shoppinglist> lists = new ArrayList<Shoppinglist>(tmp.size());
		for (Shoppinglist sl : tmp) {
			sl.putShares(getShares(sl, user, includeDeleted));
			// Only add list, if list has user as share
			if (sl.getShares().containsKey(user.getEmail())) {
				lists.add(sl);
			}
		}
		Collections.sort(lists);
		return lists;
	}

	/**
	 * Delete a list, from the db
	 * @param shoppinglistId to delete
	 * @return number of affected rows
	 */
	public int deleteList(Shoppinglist sl, User user) {
		return deleteList(sl.getId(), user);
	}
	
	/**
	 * Delete a list, from the db
	 * @param shoppinglistId to delete
	 * @return number of affected rows
	 */
	public int deleteList(String shoppinglistId, User user) {
		String id = escape(shoppinglistId);
		String q = String.format("DELETE FROM %s WHERE %s=%s AND %s=%s", LIST_TABLE, ID, id, USER, user.getUserId());
		return execQueryWithChangesCount(q);
	}

	/**
	 * Replaces a shoppinglist, that have been updated in some way
	 * @param sl that have been edited
	 * @return number of affected rows
	 */
	public int editList(Shoppinglist sl, User user) {
		String q = String.format("REPLACE INTO %s %s", LIST_TABLE, listToValues(sl, user.getUserId()));
		int count = execQueryWithChangesCount(q);
		return count;
	}

	/**
	 * Adds item to db, IF it does not yet exist, else nothing
	 * @param sli to add to db
	 * @return number of affected rows
	 */
	public int insertItem(ShoppinglistItem sli, User user) {
		String q = String.format("INSERT OR REPLACE INTO %s %s", ITEM_TABLE, itemToValues(sli, user.getUserId()));
		return execQueryWithChangesCount(q);
	}
	
	/**
	 * Adds a list of items to db, IF they do not yet exist, else nothing
	 * @param items to insert
	 * @return number of affected rows
	 */
	public int insertItems(ArrayList<ShoppinglistItem> items, User user) {
		int count = 0;
		for (ShoppinglistItem sli : items) {
			count += insertItem(sli, user);
		}
		return count;
	}

	/**
	 * Get a shoppinglistitem from the db
	 * @param itemId to get from db
	 * @return A shoppinglistitem or null if no match is found
	 */
	public ShoppinglistItem getItem(String itemId, User user) {
		itemId = escape(itemId);
		String q = String.format("SELECT * FROM %s WHERE %s=%s AND  %s=%s", ITEM_TABLE, ID, itemId, USER , user.getUserId());
		Cursor c = null;
		ShoppinglistItem sli = null;
		try {
			c = execQuery(q);
			if (c.moveToFirst()) {
				sli = cursorToSli(c);
			}
		} catch (IllegalStateException e) {
			EtaLog.d(TAG, e.getMessage(), e);
		} finally {
			closeCursorAndDB(c);
		}
		return sli;
	}
	
	/**
	 * Get all Shoppinglistitems from a shoppinglist.
	 * @param sl from which to get items
	 * @return A list of shoppinglistitems
	 */
	public List<ShoppinglistItem> getItems(Shoppinglist sl, User user) {
		return getItems(sl.getId(), user, false);
	}

	/**
	 * Get all Shoppinglistitems from a shoppinglist.
	 * @param sl from which to get items
	 * @return A list of shoppinglistitems
	 */
	public List<ShoppinglistItem> getItems(Shoppinglist sl, User user, boolean includeDeleted) {
		return getItems(sl.getId(), user, includeDeleted);
	}
	
	/**
	 * Get all {@link ShoppinglistItem} from a {@link Shoppinglist}.
	 * @param shoppinglistId from which to get items
	 * @return A list of shoppinglistitems
	 */
	public List<ShoppinglistItem> getItems(String shoppinglistId, User user, boolean includeDeleted) {
		String id = escape(shoppinglistId);
		String q = null;
		if (includeDeleted) {
			q = String.format("SELECT * FROM %s WHERE %s=%s AND %s=%s", ITEM_TABLE, SHOPPINGLIST_ID, id, USER, user.getUserId());
		} else {
			q = String.format("SELECT * FROM %s WHERE %s=%s AND %s!=%s AND %s=%s", ITEM_TABLE, SHOPPINGLIST_ID, id, STATE, SyncState.DELETE, USER, user.getUserId());
		}
		List<ShoppinglistItem> items = new ArrayList<ShoppinglistItem>();
		Cursor c = null;
		try {
			c = execQuery(q);
			if (c.moveToFirst() ) {
				do { 
					items.add(cursorToSli(c));
				} while (c.moveToNext());
			}
		} catch (IllegalStateException e) {
			EtaLog.d(TAG, e.getMessage(), e);
		} finally {
			closeCursorAndDB(c);
		}
		return items;
	}

	public ShoppinglistItem getFirstItem(String shoppinglistId, User user) {
		return getItemPrevious(shoppinglistId, ListUtils.FIRST_ITEM, user);
	}
	
	public ShoppinglistItem getItemPrevious(String shoppinglistId, String previousId, User user) {
		String id = escape(shoppinglistId);
		String prev = escape(previousId);
		String q = String.format("SELECT * FROM %s WHERE %s=%s AND %s=%s AND %s=%s", ITEM_TABLE, SHOPPINGLIST_ID, id, PREVIOUS_ID, prev, USER, user.getUserId());
		ShoppinglistItem sli = null;
		Cursor c = null;
		try {
			c = execQuery(q);
			if (c.moveToFirst()) {
				sli = cursorToSli(c);
			}
		} catch (IllegalStateException e) {
			EtaLog.d(TAG, e.getMessage(), e);
		} finally {
			closeCursorAndDB(c);
		}
		return sli;
	}

	public Shoppinglist getFirstList(User user) {
		return getListPrevious(ListUtils.FIRST_ITEM, user);
	}
	
	public Shoppinglist getListPrevious(String previousId, User user) {
		String prev = escape(previousId);
		String q = String.format("SELECT * FROM %s WHERE %s=%s AND %s=%s", LIST_TABLE, PREVIOUS_ID, prev, USER, user.getUserId());
		Shoppinglist sl = null;
		Cursor c = null;
		try {
			c = execQuery(q);
			if (c.moveToFirst()) {
				sl = cursorToSl(c); 
			}
		} catch (IllegalStateException e) {
			EtaLog.d(TAG, e.getMessage(), e);
		} finally {
			closeCursorAndDB(c);
		}
		return sl;
	}
	
	/**
	 * Deletes an {@link ShoppinglistItem} from db
	 * @param id of the item to delete
	 * @return the number of rows affected
	 */
	public int deleteItem(ShoppinglistItem sli, User user) {
		String id = escape(sli.getId());
		String q = String.format("DELETE FROM %s WHERE %s=%s AND %s=%s", ITEM_TABLE, ID, id, USER, user.getUserId());
		return execQueryWithChangesCount(q);
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
	public int deleteItems(String shoppinglistId, Boolean state, User user) {
		shoppinglistId = escape(shoppinglistId);
		String q = String.format("DELETE FROM %s WHERE %s=%s AND %s=%s", ITEM_TABLE, SHOPPINGLIST_ID, shoppinglistId, USER, user.getUserId());
		if (state != null) {
			q += String.format(" AND %s=%s", TICK, escape(state));
		}
		return execQueryWithChangesCount(q);
	}

	/**
	 * replaces an item in db
	 * @param sli to insert
	 * @return number of affected rows
	 */
	public int editItem(ShoppinglistItem sli, User user) {
		String q = String.format("REPLACE INTO %s %s", ITEM_TABLE,  itemToValues(sli, user.getUserId()));
		return execQueryWithChangesCount(q);
	}
	
	/**
	 * 
	 * @param sl
	 * @param userId
	 * @param includeDeleted
	 * @return
	 */
	public List<Share> getShares(Shoppinglist sl, User user, boolean includeDeleted) {
		String slId = escape(sl.getId());
		String q = null;
		if (includeDeleted) {
			q = String.format("SELECT * FROM %s WHERE %s=%s AND %s=%s", SHARE_TABLE, SHOPPINGLIST_ID, slId, USER, user.getUserId());
		} else {
			q = String.format("SELECT * FROM %s WHERE %s=%s AND %s!=%s AND %s=%s", SHARE_TABLE, SHOPPINGLIST_ID, slId, STATE, SyncState.DELETE, USER, user.getUserId());
		}
		
		List<Share> shares = new ArrayList<Share>();
		Cursor c = null;
		try {
			c = execQuery(q);
			if (c.moveToFirst() ) {
				do {
					shares.add(cursorToShare(c, sl));
				} while (c.moveToNext());
			}
		} catch (IllegalStateException e) {
			EtaLog.d(TAG, e.getMessage(), e);
		} finally {
			closeCursorAndDB(c);
		}
		
		return shares;
	}
	
	public int cleanShares(Shoppinglist sl, User user) {
		deleteShares(sl, user);
		int count = 0;
		for (Share s: sl.getShares().values())
			count += editShare(s, user);
		return count;
	}
	
	public int insertShare(Share s, User user) {
		String q = String.format("INSERT OR REPLACE INTO %s %s", SHARE_TABLE, shareToValues(s, user.getUserId()));
		return execQueryWithChangesCount(q);
	}

	public int editShare(Share s, User user) {
		deleteShare(s, user);
		return insertShare(s, user);
	}
	
	public int deleteShare(Share s, User user) {
		String email = escape(s.getEmail());
		String slId = escape(s.getShoppinglistId());
		String q = String.format("DELETE FROM %s WHERE %s=%s AND %s=%s AND %s=%s", SHARE_TABLE, SHOPPINGLIST_ID, slId, EMAIL, email, USER, user.getUserId());
		return execQueryWithChangesCount(q);
	}

	public int deleteShares(Shoppinglist sl, User user) {
		return deleteShares(sl.getId(), user);
	}
	
	public int deleteShares(String shoppinglistId, User user) {
		String slId = escape(shoppinglistId);
		String q = String.format("DELETE FROM %s WHERE %s=%s AND %s=%s", SHARE_TABLE, SHOPPINGLIST_ID, slId, USER, user.getUserId());
		return execQueryWithChangesCount(q);
	}

	private int execQueryWithChangesCount(String query) {
		
		int i = 0;
		
		synchronized (LOCK) {
			
			Cursor c = null;
			
			// Get the actual query out of the way...
			try {
				c = execQuery(query);
				if (c != null) {
					c.moveToFirst();
				}
			} catch (IllegalStateException e) {
				EtaLog.d(TAG, e.getMessage(), e);
			} finally {
				closeCursor(c);
			}
			
			// Get number of affected rows in last statement (query)
			try {
				c = execQuery("SELECT changes() AS 'changes'");
				if (c != null && c.moveToFirst()) {
					i = c.getInt(0);
				}
			} catch (IllegalStateException e) {
				EtaLog.d(TAG, e.getMessage(), e);
			} finally {
				closeCursorAndDB(c);
			}
			
		}
		
		return i;
	}
	
	private Cursor execQuery(String query) {
		
		synchronized (LOCK) {
			
			// If we are resuming the activity
			if (mDatabase == null || !mDatabase.isOpen()) {
				mDatabase = getWritableDatabase();
			}
			
			return mDatabase.rawQueryWithFactory(null, query, null, null);
			
		}
		
	}

	private void closeCursorAndDB(Cursor c) {

		synchronized (LOCK) {
			
			closeCursor(c);
			
			if (!mEta.isStarted()) {
				mDatabase.close();
			}
			
		}
		
	}
	
	private void closeCursor(Cursor c) {
		if (c != null) {
			c.close();
		}
	}

}
