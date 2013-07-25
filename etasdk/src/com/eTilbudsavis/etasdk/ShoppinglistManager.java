package com.eTilbudsavis.etasdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.database.Cursor;
import android.os.Bundle;

import com.eTilbudsavis.etasdk.Api.JsonArrayListener;
import com.eTilbudsavis.etasdk.Api.JsonObjectListener;
import com.eTilbudsavis.etasdk.Api.ListListener;
import com.eTilbudsavis.etasdk.EtaObjects.EtaError;
import com.eTilbudsavis.etasdk.EtaObjects.Offer;
import com.eTilbudsavis.etasdk.EtaObjects.Shoppinglist;
import com.eTilbudsavis.etasdk.EtaObjects.ShoppinglistItem;
import com.eTilbudsavis.etasdk.EtaObjects.User;
import com.eTilbudsavis.etasdk.EtaObjects.Helpers.Share;
import com.eTilbudsavis.etasdk.Utils.Endpoint;
import com.eTilbudsavis.etasdk.Utils.Params;
import com.eTilbudsavis.etasdk.Utils.Utils;

public class ShoppinglistManager {

	public static final String TAG = "ShoppinglistManager";

	public static final String PREFS_SHOPPINGLISTMANAGER_CURRENT = "shoppinglistmanager_current";
	
	private Eta mEta;
	private DbHelper mDatabase;
	private int mItemSyncInterval = 6000;
	private int mListSyncInterval = mItemSyncInterval*3;
	private String mCurrentSlId = null;
	private ArrayList<ShoppinglistManagerListener> mSubscribers = new ArrayList<ShoppinglistManager.ShoppinglistManagerListener>();
	
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
		mDatabase = new DbHelper(mEta.getContext());
		mCurrentSlId = mEta.getPrefs().getString(PREFS_SHOPPINGLISTMANAGER_CURRENT, null);
	}

	/**
	 * Get the shopping list that is currently in use, by the user.<br>
	 * The current list is synchronized more often, than other lists, since the list is likely
	 * to be visible to the user. This is useful when juggling multiple lists.
	 * @return The shopping list currently in use. or <code>null</code> if no shoppinglists exist.
	 */
	public Shoppinglist getCurrentList() {
		Shoppinglist sl = null;
		if (mCurrentSlId == null) {
			sl = setRandomCurrent();
		} else {
			sl = getList(mCurrentSlId);
			if (sl == null) {
				sl = setRandomCurrent();
			}
		}
		return sl;
	}
	
	private Shoppinglist setRandomCurrent() {
		Shoppinglist sl = null;
		Cursor c = mDatabase.getLists();
		if (c.moveToFirst()) {
			sl = DbHelper.curToSl(c);
			setCurrentList(sl);
		} 
		c.close();
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
		mEta.getPrefs().edit().putString(PREFS_SHOPPINGLISTMANAGER_CURRENT, mCurrentSlId).commit();
		return this;
	}
	
	/**
	 * Get a shoppinglist from it's ID.
	 * @param id of the shoppinglist to get
	 * @return A shopping list, or <code>null</code> if no shopping list exists
	 */
	public Shoppinglist getList(String id) {
		Cursor c = mDatabase.getList(id);
		return c.moveToFirst() == true ? DbHelper.curToSl(c) : null;
	}

	/**
	 * The complete set of shopping lists
	 * @return <li>All shopping lists
	 */
	public ArrayList<Shoppinglist> getLists() {
		Cursor c = mDatabase.getLists();
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
	 * Get a shopping list from it's human readable name
	 * @param id of the shopping list to get
	 * @return <li>Shopping list or null if no shopping list exists
	 */
	public ArrayList<Shoppinglist> getListFromName(String name) {
		Cursor c = mDatabase.getListFromName(name);
		ArrayList<Shoppinglist> list = new ArrayList<Shoppinglist>();
		if (c.moveToFirst() ) {
			do { 
				list.add(DbHelper.curToSl(c));
			} while (c.moveToNext());
		}
		c.close();
		return list;
	}

	public void addList(String name, SyncListener listener) {
		addList(Shoppinglist.fromName(name), listener);
	}
	
	/**
	 * Add a new shopping list.<br>
	 * If owner haven't been set, we will assume that it is the user who is currently logged in.
	 * if no user is logged inn, then we assume it is a offline list.<br>
	 * shopping list added to the database, and changes is synchronized to the server if possible.<br>
	 * 
	 * @param sl - the new shoppinglist to add to the database, and server
	 */
	public void addList(final Shoppinglist sl, SyncListener listener) {
		
		// Make sure the UUID does not exist, before adding it
		while (mDatabase.existsList(sl.getId())) {
			sl.setId(Utils.createUUID());
		};
		
		if (mustSync()) {

			if (sl.getOwner().getUser() == null) {
				sl.getOwner().setUser(mEta.getUser().getEmail());
				sl.getOwner().setAccess(Share.ACCESS_OWNER);
				sl.getOwner().setAccepted(true);
			}
			
			sl.setState(Shoppinglist.STATE_SYNCHRONIZING);
			
			// Sync online if possible
			JsonObjectListener cb = new JsonObjectListener() {
				
				public void onComplete(int statusCode, JSONObject data, EtaError error) {
//					Utils.logd(TAG, "addList", statusCode, data, error);
					if (Utils.isSuccess(statusCode)) {
						Shoppinglist newSl = new Shoppinglist(data);
						newSl.setState(Shoppinglist.STATE_SYNCHRONIZED);
						mDatabase.editList(newSl);
						notifyListAdd(newSl.getId());
					} else {
//						Utilities.logd(TAG, error.toString());
						sl.setState(Shoppinglist.STATE_INIT);
						mDatabase.editList(sl);
						notifyListAdd(sl.getId());
					}
				}
			};
			
			mEta.api().put(Endpoint.getListById(mEta.getUser().getId(), sl.getId()), cb, sl.getApiParams()).execute();
			
		}

		// Insert the new list to DB
		mDatabase.insertList(sl);

		notifyListAdd(sl.getId());
		
	}

	/**
	 * Edit a shopping list already in the database.<br>
	 * shopping list is replaced in the database, and changes is synchronized to the server if possible.<br>
	 * @param sl - Shopping list to be replaced
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public void editList(final Shoppinglist sl, SyncListener listener) {
		
		if (mustSync()) {

			sl.setState(Shoppinglist.STATE_SYNCHRONIZING);
			mDatabase.editList(sl);
			
			JsonObjectListener editItem = new JsonObjectListener() {
				
				public void onComplete(int statusCode, JSONObject data, EtaError error) {
					Utils.logd(TAG, "editList", statusCode, data, error);
					Shoppinglist s = sl;
					if (Utils.isSuccess(statusCode)) {
						s = new Shoppinglist(data);
						s.setState(Shoppinglist.STATE_SYNCHRONIZED);
					} else {
						Utils.logd(TAG, error.toString());
						s.setState(Shoppinglist.STATE_ERROR);
					}
					mDatabase.editList(s);
					notifyListEdit(s.getId());
				}
			};
			
			mEta.api().put(Endpoint.getListById(mEta.getUser().getId(), sl.getId()), editItem, sl.getApiParams()).execute();
		} else {
			mDatabase.editList(sl);
			notifyListEdit(sl.getId());
		}
	}

	public void deleteList(String id, SyncListener listener) {
		Shoppinglist sl = getList(id);
		if (sl != null) 
			deleteList(sl, listener);
	}

	/**
	 * Delete a shopping list.<br>
	 * shopping list is deleted from the database, and changes is synchronized to the server if possible.<br>
	 * All shopping list items associated with the shopping list are also deleted.
	 * @param sl - Shopping list to delete
	 * @return the number of rows affected.
	 */
	public void deleteList(final Shoppinglist sl, SyncListener listener) {
		
		if (mustSync()) {
			
			sl.setState(Shoppinglist.STATE_DELETING);
			mDatabase.editList(sl);
			
			JsonObjectListener cb = new JsonObjectListener() {
				
				public void onComplete(int statusCode, JSONObject data, EtaError error) {
//					Utilities.logd(TAG, "deleteList", statusCode, data, error);
					if (Utils.isSuccess(statusCode)) {
						mDatabase.deleteList(sl.getId());
						mDatabase.deleteItems(sl.getId(), null);
					} else {
						// TODO: What state are we in here? Server knows?
						Utils.logd(TAG, error.toString());
						sl.setState(Shoppinglist.STATE_ERROR);
					}
				}
			};
			
			mEta.api().delete(Endpoint.getListById(mEta.getUser().getId(), sl.getId()), cb, sl.getApiParams()).execute();
		} else {
			mDatabase.deleteList(sl.getId());
			mDatabase.deleteItems(sl.getId(), null);
			notifyListDelete(sl.getId());
		}
		
	}

	/**
	 * Get a shopping list item by it's ID
	 * @param id of the shopping list item
	 * @return A shopping list item, or <code>null</code> if no item can be found.
	 */
	public ShoppinglistItem getItem(String id) {
		Cursor c = mDatabase.getItem(id);
		boolean b = c.moveToFirst();
		c.close();
		return b ? DbHelper.curToSli(c) : null;
	}

	/**
	 * Get a shopping list item by it's ID
	 * @param id of the shopping list item
	 * @return A shopping list item, or <code>null</code> if no item can be found.
	 */
	public ArrayList<ShoppinglistItem> getItems(Shoppinglist sl) {

		Cursor c = mDatabase.getItems(sl);
		ArrayList<ShoppinglistItem> list = new ArrayList<ShoppinglistItem>();
		if (c.moveToFirst() ) {
			do { 
				list.add(DbHelper.curToSli(c));
			} while (c.moveToNext());
		}
		c.close();
		return list;
		
	}
	
	public long addItem(final Shoppinglist sl, final Offer offer, SyncListener listener) {
		// TODO: Add item implementation
		return 0L;
	}
	
	/**
	 * TODO: Check if shopping list exists, and do stuff accordingly
	 * Add an item to a shopping list.<br>
	 * shopping list items is inserted into the database, and changes is synchronized to the server if possible.
	 * If the shopping list does not exist in the database or the server, a new one is created and synchronized if possible
	 * @param sl - shopping list where item should be added to
	 * @param sli - shopping list item that should be added.
	 * @return the row ID of the newly inserted row, or -1 if an error occurred
	 */
	public long addItem(final Shoppinglist sl, final ShoppinglistItem sli, SyncListener listener) {
		
		if (mustSync()) {

			JsonObjectListener cb = new JsonObjectListener() {
				
				public void onComplete(int statusCode, JSONObject data, EtaError error) {
//					Utilities.logd(TAG, "addItem", statusCode, data, error);
					if (Utils.isSuccess(statusCode)) {
						ShoppinglistItem s = ShoppinglistItem.fromJSON(data, sl.getId());
						s.setState(ShoppinglistItem.STATE_SYNCHRONIZED);
						mDatabase.editItem(s);
						notifyItemUpdate(s.getId());
					} else {
						Utils.logd(TAG, error.toString());
						sli.setState(ShoppinglistItem.STATE_ERROR);
						mDatabase.editItem(sli);
						notifyItemUpdate(sli.getId());
					}
				}
			};
			
			mEta.api().put(Endpoint.getItemById(mEta.getUser().getId(), sl.getId(), sli.getId()), cb, sli.getApiParams()).execute();
		} else {
			
		}
		
		return mDatabase.addItem(sli.setShoppinglistId(sl.getId()));
		
	}

	/**
	 * Insert an updated shopping list item into the db.<br>
	 * shopping list items is replaced in the database, and changes is synchronized to the server if possible.
	 * @param sli - shopping list item to edit
	 */
	public void editItem(final ShoppinglistItem sli, SyncListener listener) {
		
		if (mustSync()) {
			
			sli.setState(ShoppinglistItem.STATE_SYNCHRONIZING);

			JsonObjectListener cb = new JsonObjectListener() {
				
				public void onComplete(int statusCode, JSONObject data, EtaError error) {
//					Utilities.logd(TAG, "editItem", statusCode, data, error);
					ShoppinglistItem s = sli;
					if (Utils.isSuccess(statusCode)) {
						s = ShoppinglistItem.fromJSON(data, sli.getId());
						s.setState(ShoppinglistItem.STATE_SYNCHRONIZED);
					} else {
						Utils.logd(TAG, error.toString());
						s.setState(ShoppinglistItem.STATE_ERROR);
					}
				}
			};
			
			mEta.api().put(Endpoint.getItemById(mEta.getUser().getId(), sli.getShoppinglistId(), sli.getId()), cb, sli.getApiParams()).execute();
			
		} else {
			
			mDatabase.editItem(sli);
		}

	}

	/**
	 * Delete all items from a shoppinglist where <code>isTicked() == true.</code><br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl - shoppinglist to delete items from
	 * @return number of affected rows
	 */
	public void deleteItemsTicked(Shoppinglist sl, SyncListener listener) {
		Bundle b = new Bundle();
		b.putString(Params.FILTER_DELETE, Shoppinglist.EMPTY_TICKED);
		deleteItems(sl, b, listener);
	}

	/**
	 * Delete all items from a shoppinglist where <code>isTicked() == false.</code><br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl - shoppinglist to delete items from
	 * @return number of affected rows
	 */
	public void deleteItemsUnticked(Shoppinglist sl, SyncListener listener) {
		Bundle b = new Bundle();
		b.putString(Params.FILTER_DELETE, Shoppinglist.EMPTY_UNTICKED);
		deleteItems(sl, b, listener);
	}

	/**
	 * Delete ALL items from a given shoppinglist.<br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl - shoppinglist to delete items from
	 * @return number of affected rows
	 */
	public void deleteItems(Shoppinglist sl, SyncListener listener) {
		Bundle b = new Bundle();
		b.putString(Params.FILTER_DELETE, Shoppinglist.EMPTY_ALL);
		deleteItems(sl, b, listener);
	}
	
	/**
	 * Generic method to delete all items that matches any clauses given in the <code>apiParams</code><br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sl to remove items from
	 * @param apiParams that describe what needs to be deleted
	 * @return number of affected rows
	 */
	private void deleteItems(final Shoppinglist sl, Bundle apiParams, SyncListener listener) {
		
		if (mustSync()) {
			sl.setState(Shoppinglist.STATE_SYNCHRONIZING);
			JsonObjectListener cb = new JsonObjectListener() {
				
				public void onComplete(int statusCode, JSONObject data, EtaError error) {
//					Utilities.logd(TAG, "deleteAllItems", statusCode, data, error);
					if (Utils.isSuccess(statusCode)) {
						mDatabase.deleteItems(sl.getId(), null);
						sl.setState(Shoppinglist.STATE_SYNCHRONIZED);
					} else {
						Utils.logd(TAG, error.toString());
						sl.setState(Shoppinglist.STATE_ERROR);
					}
				}
			};
			mEta.api().delete(Endpoint.getListEmpty(mEta.getUser().getId(), sl.getId()), cb, apiParams).execute();
		} else {
			mDatabase.deleteItems(sl.getId(), null);
			notifyItemUpdate(sl.getId());
		}

	}
	
	/**
	 * Deletes a given shopping list item<br>
	 * shopping list items is removed from database, and changes is synchronized to the server if possible.
	 * @param sli to delete from the db
	 * @return the number of rows affected
	 */
	public int deleteItem(ShoppinglistItem sli, SyncListener listener) {
		// TODO: Shoppinglist - Delete item from server
		return mDatabase.deleteItem(sli.getId());
	}

	public void addShare(Shoppinglist sl, Share share, SyncListener listener) {
		// TODO: Share - Add share implementation
	}

	public void deleteShare(Shoppinglist sl, Share share, SyncListener listener) {
		// TODO: Share - Delete share implementation
	}

	public void editShare(Shoppinglist sl, Share share, SyncListener listener) {
		// TODO: Share - Edit share implementation
	}
	
	public List<Share> getShares(Shoppinglist sl) {
		return new ArrayList<Share>();
		// TODO: Share - get shares implementation
	}
	
	/**
	 * Sync all shopping lists.<br>
	 * This is run at certain intervals if startSync() has been called.<br>
	 * startSync() is called if Eta.onResume() is called.
	 * @param shoppinglist update
	 */
	public void syncLists() {
		
		if (!mustSync()) return;

		ListListener<Shoppinglist> sll = new ListListener<Shoppinglist>() {
			
			public void onComplete(int statusCode, List<Shoppinglist> data, EtaError error) {
				if (Utils.isSuccess(statusCode)) {
					mergeLists(data, getLists());
				} else {
					Utils.logd(TAG, error.toString());
				}
			}
		};
		mEta.api().get(Endpoint.getListsByUserId(mEta.getUser().getId()), sll).execute();
	}
	
	/**
	 * Method, that will merge to sets of shopping lists and find which lists
	 * have been edited, added and deleted. Finally it will <code>notifyListUpdate()</code>
	 * @param newLists - The newly recieved items
	 * @param oldLists - The old list of items
	 */
	private void mergeLists(List<Shoppinglist> newLists, List<Shoppinglist> oldLists) {
		
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
						mDatabase.editList(tmplists.get(key));
					}
				} else {
					deleted.add(key);
					mDatabase.deleteList(key);
				}
			} else {
				added.add(key);
				mDatabase.insertList(tmplists.get(key));
			}
		}
		
		// If no changes has been registeres, ship the rest
		if (!added.isEmpty() || !deleted.isEmpty() || !edited.isEmpty()) {
			syncItems();
		}
		
		notifyListUpdate(added.isEmpty() ? null : added, deleted.isEmpty() ? null : deleted, edited.isEmpty() ? null : edited);
	}

	/**
	 * Sync all shopping list items, in all shopping lists.<br>
	 * This is run at certain intervals if startSync() has been called.<br>
	 * startSync() is called if Eta.onResume() is called.
	 * @param shoppinglist update
	 */
	public void syncItems() {

		if (!mustSync()) return;
		
		for (final Shoppinglist sl : getLists()) {
			if (!sl.isStateSynchronizing()) {
				if (!sl.isStateSynchronized()) {
					// If the list haven't synced yet, then don't ask, just do it
					// this is the case for both new lists and modified lists
					syncItems(sl);
				} else {
					// Else make a call to check when it's been modified
					JsonObjectListener cb = new JsonObjectListener() {
						
						public void onComplete(int statusCode, JSONObject data, EtaError error) {

//							Utilities.logd(TAG, "syncItems()", statusCode, data, error);
							
							if (Utils.isSuccess(statusCode)) {
								// If callback says the list has been modified, then sync items
								long oldModified = sl.getModified();
								sl.setModifiedFromJSON(data);
								if (oldModified < sl.getModified())
									syncItems(sl);
							} else {
								Utils.logd(TAG, error.toString());
							}
						}
					};
					
					mEta.api().get(Endpoint.getListModifiedById(mEta.getUser().getId(), sl.getId()), cb).execute();
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
	public void syncItems(final Shoppinglist sl) {
		
		sl.setState(Shoppinglist.STATE_SYNCHRONIZING);
		mDatabase.editList(sl);
		
		JsonArrayListener cb = new JsonArrayListener() {
			
			public void onComplete(int statusCode, JSONArray data, EtaError error) {
				
				if (Utils.isSuccess(statusCode)) {
					mDatabase.deleteItems(sl.getId(), null);
					mDatabase.addItems(ShoppinglistItem.fromJSON(data, sl.getId()));
					sl.setState(Shoppinglist.STATE_SYNCHRONIZED);
				} else {
					Utils.logd(TAG, error.toString());
					sl.setState(Shoppinglist.STATE_ERROR);
				}
				mDatabase.editList(sl);
				notifyItemUpdate(sl.getId());
			}
		};
		
		mEta.api().get(Endpoint.getItemByListId(mEta.getUser().getId(), sl.getId()), cb).execute();
		
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
	
	public void onResume() {
		mDatabase.openDB();
		startSync();
	}
	
	public void onPause() {
		stopSync();
		mDatabase.closeDB();
	}
	
	/**
	 * Set the synchronization intervals for the shoppinglists, and their items.<br>
	 * The synchronization of items will be the time specified, and the list
	 * synchronization will be a factor three of that time, as the lists themselves
	 * are less subjected to change. Also time must be 3000 milliseconds or more.
	 * @param time in milliseconds
	 */
	public void setSyncInterval(int time) {
		if (time < 3000) {
			mItemSyncInterval = 3000;
		} else {
			mItemSyncInterval = time;
		}
		mListSyncInterval = mItemSyncInterval*3;
	}
	
	/**
	 * Checks if a user is logged in, and are able to sync items.
	 * @return true if we can sync, else false.
	 */
	private boolean mustSync() {

		if (!mEta.getUser().isLoggedIn()) {
			Utils.logd(TAG, "No user loggedin, cannot sync shoppinglists");
			stopSync();
			return false;
		}
		return true;
	}
	
	public ShoppinglistManager subscribe(ShoppinglistManagerListener listener) {
		if (!mSubscribers.contains(listener)) {
			mSubscribers.add(listener);
		}
		return this;
	}

	public boolean unsubscribe(ShoppinglistManagerListener listener) {
		return mSubscribers.remove(listener);
	}

	/**
	 * Wrapper method to make notification when adding a new shoppinglist.<br>
	 * This will call
	 * @param id
	 * @return
	 */
	public ShoppinglistManager notifyListAdd(String id) {
		List<String> list = new ArrayList<String>();
		list.add(id);
		notifyListUpdate( list , null, null);
		return this;
	}

	public ShoppinglistManager notifyListDelete(String id) {
		List<String> list = new ArrayList<String>();
		list.add(id);
		notifyListUpdate(null, list, null);
		return this;
	}

	public ShoppinglistManager notifyListEdit(String id) {
		List<String> list = new ArrayList<String>();
		list.add(id);
		notifyListUpdate(null, null, list);
		return this;
	}
	
	public ShoppinglistManager notifyListUpdate(List<String> added, List<String> deleted, List<String> edited) {
//		Utilities.logd(TAG, "Sending LIST notifications - " + String.valueOf(mSubscribers.size()));
		for (ShoppinglistManagerListener s : mSubscribers) {
			try {
				s.onListUpdate(added, deleted, edited);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return this;
	}

	public ShoppinglistManager notifyItemUpdate(String shoppinglistId) {
//		Utilities.logd(TAG, "Sending ITEM notifications - " + String.valueOf(mSubscribers.size()));
		for (ShoppinglistManagerListener s : mSubscribers) {
			try {
				s.onItemUpdate(shoppinglistId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this;
	}
	
	public interface ShoppinglistManagerListener {
		public void onListUpdate(List<String> addedIds, List<String> deletedIds, List<String> editedIds);
		public void onItemUpdate(String shoppinglistId);
	}
	
	public interface SyncListener {
		public void onComplete(int statucCode, String data);
	}
	
}
