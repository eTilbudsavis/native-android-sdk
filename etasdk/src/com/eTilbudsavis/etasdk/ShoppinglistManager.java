package com.eTilbudsavis.etasdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.EtaObjects.Shoppinglist;
import com.eTilbudsavis.etasdk.EtaObjects.ShoppinglistItem;
import com.eTilbudsavis.etasdk.EtaObjects.User;
import com.eTilbudsavis.etasdk.Tools.Endpoint;
import com.eTilbudsavis.etasdk.Tools.Params;
import com.eTilbudsavis.etasdk.Tools.Utilities;

public class ShoppinglistManager {

	public static final String TAG = "ShoppinglistManager";

	private static final String SHOPPINGLIST = DbHelper.SL;
	private static final String SHOPPINGLISTITEM = DbHelper.SLI;
	public static final String PREFS_SHOPPINGLISTMANAGER_USER = "session_user";
	
	private Eta mEta;
	private DbHelper mDbHelper;
	private SQLiteDatabase mDatabase;
	private int mListSyncInterval = 20000;
	private int mItemSyncInterval = 6000;
	private String mCurrentSlId = null;
	private ArrayList<ShoppinglistListener> mSubscribers = new ArrayList<ShoppinglistManager.ShoppinglistListener>();
	
	private Runnable mListSync = new Runnable() {
		
		public void run() {
			syncLists();
			mEta.getHandler().postDelayed(mListSync, mListSyncInterval);
		}
	};

	private Runnable mItemSync = new Runnable() {
		
		public void run() {
			syncItems();
			mEta.getHandler().postDelayed(mItemSync, mItemSyncInterval);
		}
	};
	
	public ShoppinglistManager(Eta eta) {
		mEta = eta;
		mDbHelper = new DbHelper(mEta.getContext());
		mCurrentSlId = mEta.getPrefs().getString(PREFS_SHOPPINGLISTMANAGER_USER, null);
	}

	/**
	 * Get the shopping list that is currently in use, by the user.<br>
	 * The current list is synchronized more often, than other lists, since the list is likely
	 * to be visible to the user. This is useful when juggling multiple lists.
	 * @return The shopping list currently in use.
	 */
	public Shoppinglist getCurrentList() {
		Shoppinglist sl = null;
		if (mCurrentSlId == null) {
			Cursor c = dbGetAllLists();
			if (c.moveToFirst()) {
				sl = DbHelper.curToSl(c);
				setCurrentList(sl);
			}
			c.close();
		} else {
			sl = getList(mCurrentSlId);
		}
		return sl;
	}
	
	/**
	 * Set the shopping list that is currently in use, by the user.<br>
	 * The current list is synchronized more often, than other lists, since the list is likely
	 * to be visible to the user. This is useful when juggling multiple lists.
	 * @param sl - shopping list to make the currently visible list
	 * @return This ShoppinglistManager
	 */
	public ShoppinglistManager setCurrentList(Shoppinglist sl) {
		mCurrentSlId = sl.getId();
		mEta.getPrefs().edit().putString(PREFS_SHOPPINGLISTMANAGER_USER, mCurrentSlId).commit();
		return this;
	}
	
	/**
	 * Get a shoppinglist from it's ID.
	 * @param id of the shoppinglist to get
	 * @return A shopping list, or <code>null</code> if no shopping list exists
	 */
	public Shoppinglist getList(String id) {
		Cursor c = dbGetList(id);
		return c.moveToFirst() == true ? DbHelper.curToSl(c) : null;
	}

	/**
	 * Get a shopping list from it's human readable name
	 * @param id of the shopping list to get
	 * @return <li>Shopping list or null if no shopping list exists
	 */
	public ArrayList<Shoppinglist> getListFromName(String name) {
		Cursor c = dbGetListFromName(name);
		ArrayList<Shoppinglist> list = new ArrayList<Shoppinglist>();
		if (c.moveToFirst() ) {
			do { 
				list.add(DbHelper.curToSl(c));
			} while (c.moveToNext());
		}
		c.close();
		return list;
	}

	/**
	 * Add a new shopping list.<br>
	 * shopping list added to the database, and changes is synchronized to the server if possible.<br>
	 * 
	 * @param sl - the new shoppinglist to add to the database, and server
	 */
	public void addList(Shoppinglist sl) {
		
		// Make sure the UUID does not exist, before adding it
		while (dbExistsList(sl.getId())) {
			sl.setId(Utilities.createUUID());
		};
		
		// Insert the new list to DB
		dbInsertList(sl);

		if (!mustSync()) return;
		
		// Sync online if possible
		mEta.api().put(Endpoint.getListFromId(user().getId(), sl.getId()), new Api.CallbackString() {
			
			public void onComplete(int statusCode, String data, EtaError error) {
				Utilities.logd(TAG, "addList", statusCode, data, error);
				if (Utilities.isSuccess(statusCode)) {
					
				} else {
					mEta.addError(error);
					Utilities.logd(TAG, error.toString());
				}
				notifyListUpdate(null, null, null);
				
			}
		}, sl.getApiParams()).execute();
		
	}

	/**
	 * Edit a shopping list already in the database.<br>
	 * shopping list is replaced in the database, and changes is synchronized to the server if possible.<br>
	 * @param sl - Shopping list to be replaced
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public boolean editList(final Shoppinglist sl) {
		
		long l = dbEditList(sl);
		
		if (mustSync()) {
			
			Api.CallbackString editItem = new Api.CallbackString() {
				
				public void onComplete(int statusCode, String data, EtaError error) {
					Utilities.logd(TAG, "editList", statusCode, data, error);
					if (Utilities.isSuccess(statusCode)) {
						dbEditList(Shoppinglist.fromJSON(data));
					} else {
						mEta.addError(error);
						Utilities.logd(TAG, error.toString());
					}
				}
			};
			
			mEta.api().put(Endpoint.getListFromId(user().getId(), sl.getId()), editItem, sl.getApiParams()).execute();
		}
		
		return l == -1 ? false : true;
		
	}

	public int deleteList(String id) {
		Shoppinglist sl = getList(id);
		return sl == null ? 0 : deleteList(sl);
	}

	/**
	 * Delete a shopping list.<br>
	 * shopping list is deleted from the database, and changes is synchronized to the server if possible.<br>
	 * All shopping list items associated with the shopping list are also deleted.
	 * @param sl - Shopping list to delete
	 * @return the number of rows affected.
	 */
	public int deleteList(Shoppinglist sl) {
		
		int count = dbDeleteList(sl.getId());
		
		dbDeleteItems(sl.getId());
		
		if (mustSync()) {
			mEta.api().delete(Endpoint.getListFromId(user().getId(), sl.getId()), new Api.CallbackString() {
				
				public void onComplete(int statusCode, String data, EtaError error) {
					Utilities.logd(TAG, "deleteList", statusCode, data, error);
					if (Utilities.isSuccess(statusCode)) {
						
					} else {
						mEta.addError(error);
						Utilities.logd(TAG, error.toString());
					}
				}
			}, sl.getApiParams()).execute();
		}
		return count;
	}

	/**
	 * Get a shopping list item by it's ID
	 * @param id of the shopping list item
	 * @return A shopping list item, or <code>null</code> if no item can be found.
	 */
	public ShoppinglistItem getItem(String id) {
		Cursor c = dbGetItem(id);
		boolean b = c.moveToFirst();
		c.close();
		return b ? DbHelper.curToSli(c) : null;
	}
	
	/**
	 * Add an item to a shopping list.<br>
	 * shopping list items is inserted into the database, and changes is synchronized to the server if possible.
	 * If the shopping list does not exist in the database or the server, a new one is created and synchronized if possible
	 * TODO: Check if shopping list exists, and do stuff accordingly
	 * @param sl - shopping list where item should be added to
	 * @param sli - shopping list item that should be added.
	 */
	public void addItem(final Shoppinglist sl, ShoppinglistItem sli) {
		
		dbAddItem(sli.setShoppinglistId(sl.getId()));
		
		if (mustSync()) {
			
			Api.CallbackString cb = new Api.CallbackString() {
				
				public void onComplete(int statusCode, String data, EtaError error) {
					Utilities.logd(TAG, "addItem", statusCode, data, error);
					if (Utilities.isSuccess(statusCode)) {
						dbEditItem(ShoppinglistItem.fromJSON(data, sl.getId()));
					} else {
						mEta.addError(error);
						Utilities.logd(TAG, error.toString());
					}
				}
			};
			
			mEta.api().put(Endpoint.getItemID(user().getId(), sl.getId(), sli.getId()), cb, sli.getApiParams()).execute();
		}
	}

	/**
	 * Insert an updated shopping list item into the db.<br>
	 * shopping list items is replaced in the database, and changes is synchronized to the server if possible.
	 * @param sli - shopping list item to edit
	 */
	public void editItem(final ShoppinglistItem sli) {
		
		dbEditItem(sli);
		
		if (mustSync()) {
			
			Api.CallbackString cb = new Api.CallbackString() {
				
				public void onComplete(int statusCode, String data, EtaError error) {
					Utilities.logd(TAG, "editItem", statusCode, data, error);
					if (Utilities.isSuccess(statusCode)) {
						dbEditItem(ShoppinglistItem.fromJSON(data, sli.getId()));
					} else {
						mEta.addError(error);
						Utilities.logd(TAG, error.toString());
					}
				}
			};
			
			mEta.api().put(Endpoint.getItemID(user().getId(), sli.getShoppinglistId(), sli.getId()), cb, sli.getApiParams()).execute();
			
		}
	}

	/**
	 * Delete all items from a shoppinglist where <code>isTicked() == true.</code><br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl - shoppinglist to delete items from
	 */
	public void deleteAllTickedItems(Shoppinglist sl) {
		Bundle b = new Bundle();
		b.putString(Params.FILTER_DELETE, Shoppinglist.EMPTY_TICKED);
		deleteAllItems(sl, b);
	}

	/**
	 * Delete all items from a shoppinglist where <code>isTicked() == false.</code><br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl - shoppinglist to delete items from
	 */
	public void deleteAllUntickedItems(Shoppinglist sl) {
		Bundle b = new Bundle();
		b.putString(Params.FILTER_DELETE, Shoppinglist.EMPTY_UNTICKED);
		deleteAllItems(sl, b);
	}

	/**
	 * Delete ALL items from a given shoppinglist.<br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl - shoppinglist to delete items from
	 */
	public void deleteAllItems(Shoppinglist sl) {
		Bundle b = new Bundle();
		b.putString(Params.FILTER_DELETE, Shoppinglist.EMPTY_ALL);
		deleteAllItems(sl, b);
	}
	
	/**
	 * Generic method to delete all items that matches any clauses given in the <code>apiParams</code><br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl to remove items from
	 * @param apiParams that describe what needs to be deleted
	 */
	private void deleteAllItems(Shoppinglist sl, Bundle apiParams) {
		
		dbDeleteItems(sl.getId());
		
		if (mustSync()) {
			
			Api.CallbackString cb = new Api.CallbackString() {
				
				public void onComplete(int statusCode, String data, EtaError error) {
					Utilities.logd(TAG, "deleteAllItems", statusCode, data, error);
					if (Utilities.isSuccess(statusCode)) {
						
					} else {
						mEta.addError(error);
						Utilities.logd(TAG, error.toString());
					}
				}
			};
			mEta.api().delete(Endpoint.getListEmpty(user().getId(), sl.getId()), cb, apiParams).execute();
		}
	}
	
	/**
	 * TODO: finish this method<br>
	 * Deletes a given shopping list item<br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sli to delete from the db
	 * @return the number of rows affected
	 */
	public int deleteItem(ShoppinglistItem sli) {
		return dbDeleteItem(sli.getId());
	}

	/**
	 * Sync all shopping lists.<br>
	 * This is run at certain intervals if startSync() has been called.<br>
	 * startSync() is called if Eta.onResume() is called.
	 * @param shoppinglist update
	 */
	public void syncLists() {
		
		if (!mustSync()) return;
		
		Api.CallbackString listener = new Api.CallbackString() {
			
			public void onComplete(int statusCode, String data, EtaError error) {

				Utilities.logd(TAG, "syncLists", statusCode, data, error);
				
				if (Utilities.isSuccess(statusCode)) {
					ArrayList<Shoppinglist> sl = Shoppinglist.fromJSONArray(data);
					mergeLists(sl, getAllLists());
				} else {
					mEta.addError(error);
					Utilities.logd(TAG, error.toString());
				}
				
			}
		};
		mEta.api().get(Endpoint.getListList(user().getId()), listener).execute();
	}
	
	/**
	 * Method, that will merge to sets of shopping lists and find which lists
	 * have been edited, added and deleted. Finally it will <code>notifyListUpdate()</code>
	 * @param newLists - The newly recieved items
	 * @param oldLists - The old list of items
	 */
	private void mergeLists(ArrayList<Shoppinglist> newLists, ArrayList<Shoppinglist> oldLists) {
		
		HashMap<String, Shoppinglist> dblist = new HashMap<String, Shoppinglist>();
		for (Shoppinglist sl : oldLists) {
			dblist.put(sl.getId(), sl);
		}

		HashMap<String, Shoppinglist> tmplists = new HashMap<String, Shoppinglist>();
		for (Shoppinglist sl : newLists) {
			tmplists.put(sl.getId(), sl);
		}
		
		HashSet<String> union = new HashSet<String>();
		union.addAll(tmplists.keySet());
		union.addAll(dblist.keySet());

		List<String> added = new ArrayList<String>();
		List<String> deleted = new ArrayList<String>();
		List<String> edited = new ArrayList<String>();

		for (String key : union) {
			
			if (dblist.containsKey(key)) {
				if (tmplists.containsKey(key)) {
					if (!dblist.get(key).equals(tmplists.get(key))) {
						edited.add(key);
						dbEditList(tmplists.get(key));
					}
				} else {
					deleted.add(key);
					dbDeleteList(key);
				}
			} else {
				added.add(key);
				dbInsertList(tmplists.get(key));
			}
		}
		
		// If no changes has been registeres, ship the rest
		if (!added.isEmpty() || !deleted.isEmpty() || !edited.isEmpty()) {
			syncItems();
		}
		notifyListUpdate(added, deleted, edited);
	}

	/**
	 * Sync all shopping list items, in all shopping lists.<br>
	 * This is run at certain intervals if startSync() has been called.<br>
	 * startSync() is called if Eta.onResume() is called.
	 * @param shoppinglist update
	 */
	public void syncItems() {

		if (!mustSync()) return;
		
		for (final Shoppinglist sl : getAllLists()) {
			if (!sl.isSyncing()) {
				if (!sl.isSynced()) {
					// If the list haven't synced yet, then don't ask, just do it
					// this is the case for both new lists and modified lists
					syncItems(sl);
				} else {
					
					// Else make a call to check when it's been modified
					Api.CallbackString cb = new Api.CallbackString() {
						
						public void onComplete(int statusCode, String data, EtaError error) {

							Utilities.logd(TAG, "syncItems()", statusCode, data, error);
							
							if (Utilities.isSuccess(statusCode)) {
								// If callback says the list has been modified, then sync items
								long oldModified = sl.getModified();
								sl.setModifiedFromJSON(data);
								if (oldModified < sl.getModified())
									syncItems(sl);
							} else {
								mEta.addError(error);
								Utilities.logd(TAG, error.toString());
							}
						}
					};
					
					mEta.api().get(Endpoint.getListModified(user().getId(), sl.getId()), cb).execute();
				}
			}
			
		}
		
	}
	
	/**
	 * Sync all shopping list items, associated with the given shopping list.<br>
	 * This is run at certain intervals if startSync() has been called.<br>
	 * startSync() is called if Eta.onResume() is called.
	 * @param shoppinglist update
	 */
	public void syncItems(Shoppinglist shoppinglist) {
		
		final Shoppinglist sl = shoppinglist;
		
		sl.setSyncing(true);
		
		Api.CallbackString listener = new Api.CallbackString() {
			
			public void onComplete(int statusCode, String data, EtaError error) {
				
				Utilities.logd(TAG, "syncItems(sl)", statusCode, data, error);
				
				if (Utilities.isSuccess(statusCode)) {
					dbDeleteItems(sl.getId());
					dbAddItems(ShoppinglistItem.fromJSONArray(data, sl.getId()));
					sl.setSynced(true);
					sl.setSyncing(false);
					dbEditList(sl);
					notifyItemUpdate(sl.getId());
				} else {
					mEta.addError(error);
					Utilities.logd(TAG, error.toString());
				}
			}
		};
		
		mEta.api().get(Endpoint.getItemList(user().getId(), sl.getId()), listener).execute();
		
	}
	
	/**
	 * Start synchronization with server
	 * If Eta.onResume() is called, there should be no need to call this.
	 */
	public void startSync() {
		if (mustSync()) {
			mListSync.run();
			mItemSync.run();
		}
	}
	
	/**
	 * Stop synchronization to server
	 * If Eta.onPause() is called, there should be no need to call this.
	 */
	public void stopSync() {
		mEta.getHandler().removeCallbacks(mListSync);
		mEta.getHandler().removeCallbacks(mItemSync);
	}

	/**
	 * Returns User<br>
	 * Just a convenience wrapper
	 * @return
	 */
	private User user() {
		return mEta.getSession().getUser();
	}

	/**
	 * Checks if a user is logged in, and are able to sync items.
	 * @return true if we can sync, else false.
	 */
	private boolean mustSync() {

		if (!user().isLoggedIn()) {
			Utilities.logd(TAG, "No user loggedin, cannot sync shoppinglists");
			stopSync();
			return false;
		}
		return true;
	}
	
	/**
	 * Open a connection to database<br>
	 * If Eta.onResume() is called, there should be no need to call this.
	 */
	public void openDB() {
		mDatabase = mDbHelper.getWritableDatabase();
		if (getAllLists().size() == 0) {
			//TODO: Get shopping list name from localized string instead of hardcoding
			Shoppinglist sl = Shoppinglist.fromName("Shoppinglist");
			sl.setOffline(true);
			dbInsertList(sl);
		}
	}
	
	/**
	 * Close the database connection<br>
	 * If Eta.onPause() is called, there should be no need to call this.
	 */
	public void closeDB() {
		mDbHelper.close();
	}

	/**
	 * Deletes all tables (<i>shoppinglists</i> and <i>shoppinglistitems</i>)
	 * and creates two new tables<br>
	 * This cannot be undone.
	 */
	public void clearDatabase() {
		mDbHelper.onUpgrade(mDatabase, 0, 0);
	}
	
	/**
	 * Insert new shoppinglist into db
	 * @param list to insert
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	private long dbInsertList(Shoppinglist list) {
		return mDatabase.insert(SHOPPINGLIST, null, DbHelper.slToCV(list));
	}

	/**
	 * Get a shopping list by it's id
	 * @param id to get
	 * @return A cursor with a shopping list that matches the id, or empty cursor 
	 */
	private Cursor dbGetList(String id) {
		return mDatabase.query(SHOPPINGLIST, null, DbHelper.ID + "=?", new String[]{id}, null, null, null, null);
	}

	/**
	 * Check if a list by a given id exists in db.<br>
	 * Good for checking if a UUID exists before inserting a new list.
	 * @param id to check for
	 * @return true if it exists, else false
	 */
	private boolean dbExistsList(String id) {
		Cursor c = dbGetList(id);
		boolean res = c.moveToFirst();
		c.close();
		return res;
	}
	
	/**
	 * Get a shopping list from it's readable name
	 * @param name of the shopping list
	 * @return Cursor with shopping lists that matches name
	 */
	private Cursor dbGetListFromName(String name) {
		return mDatabase.query(SHOPPINGLIST, null, DbHelper.NAME + "=?", new String[]{name}, null, null, null);
	}
	
	/**
	 * Get all shoppinglists
	 * @return Cursor with shoppinglists from db
	 */
	private Cursor dbGetAllLists() {
		return mDatabase.query(SHOPPINGLIST, null, null, null, null, null, null);
	}
	
	/**
	 * Delete a list, from the db
	 * @param shoppinglistId to delete
	 * @return the number of rows affected.
	 */
	private int dbDeleteList(String shoppinglistId) {
		return mDatabase.delete(SHOPPINGLIST, DbHelper.ID + "=?", new String[]{shoppinglistId});
	}
	
	/**
	 * Replaces a shoppinglist, that have been updated in some way
	 * @param list that have been edited
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	private long dbEditList(Shoppinglist list) {
		return mDatabase.replace(SHOPPINGLIST, null, DbHelper.slToCV(list));
	}

	/**
	 * Adds a list of items to db, IF they do not yet exist, else nothing
	 * @param items to insert
	 * @return true if all items have successfully been inserted, else false
	 */
	private boolean dbAddItems(ArrayList<ShoppinglistItem> items) {
		boolean resp = true;
		for (ShoppinglistItem sli : items)
			if (dbAddItem(sli) == -1)
				resp = false;
		
		return resp;
	}
	
	/**
	 * Get a shopping lite item from the db
	 * @param id to get from db
	 * @return A Cursor object, which is positioned before the first entry
	 */
	private Cursor dbGetItem(String id) {
		return mDatabase.query(SHOPPINGLISTITEM, null, DbHelper.ID + "=?", new String[]{id}, null, null, null);
	}

	/**
	 * Check if a given item id exists in db
	 * @param id to check for
	 * @return true, if it exists, else false
	 */
	private boolean dbExistsItem(String id) {
		Cursor c = dbGetItem(id);
		boolean res = c.moveToFirst();
		c.close();
		return res;
	}
	
	/**
	 * Adds item to db, IF it does not yet exist, else nothing
	 * @param sli to add to db
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	private long dbAddItem(ShoppinglistItem sli) {
		return dbExistsItem(sli.getId()) ? -1 : mDatabase.insert(SHOPPINGLISTITEM, null, DbHelper.sliToCV(sli));
	}
	
	/**
	 * Deletes all items from a specific shopping list
	 * @param shoppinglistId to remove items from
	 * @return number of affected rows
	 */
	private int dbDeleteItems(String shoppinglistId) {
		return mDatabase.delete(SHOPPINGLISTITEM, DbHelper.ID + "=?", new String[]{shoppinglistId});
	}

	/**
	 * Deletes an item from db
	 * @param id of the item to delete
	 * @return the number of rows affected
	 */
	private int dbDeleteItem(String id) {
		return mDatabase.delete(SHOPPINGLISTITEM, DbHelper.ID + "=?", new String[]{id});
	}

	/**
	 * replaces an item in db
	 * @param sli to insert
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	private long dbEditItem(ShoppinglistItem sli) {
		return mDatabase.replace(SHOPPINGLISTITEM, null, DbHelper.sliToCV(sli));
	}

	/**
	 * The complete set of shopping lists
	 * @return <li>All shopping lists
	 */
	public ArrayList<Shoppinglist> getAllLists() {
		Cursor c = dbGetAllLists();
		ArrayList<Shoppinglist> list = new ArrayList<Shoppinglist>();
		if (c.moveToFirst() ) {
			do { 
				list.add(DbHelper.curToSl(c));
			} while (c.moveToNext());
		}
		c.close();
		return list;
	}
	
	public ShoppinglistManager subscribe(ShoppinglistListener listener) {
		mSubscribers.add(listener);
		return this;
	}

	public boolean unsubscribe(ShoppinglistListener listener) {
		return mSubscribers.remove(listener);
	}

	public ShoppinglistManager notifyListUpdate(List<String> added, List<String> deleted, List<String> edited) {
		Utilities.logd(TAG, "Sending LIST notifications - " + String.valueOf(mSubscribers.size()));
		for (ShoppinglistListener s : mSubscribers)
			s.onListUpdate(added, deleted, edited);
		
		return this;
	}

	public ShoppinglistManager notifyItemUpdate(String shoppinglistId) {
		Utilities.logd(TAG, "Sending ITEM notifications - " + String.valueOf(mSubscribers.size()));
		for (ShoppinglistListener s : mSubscribers)
			s.onItemUpdate(shoppinglistId);
		
		return this;
	}
	
	public interface ShoppinglistListener {
		public void onListUpdate(List<String> added, List<String> deleted, List<String> edited);
		public void onItemUpdate(String shoppinglistId);
	}
	
}
